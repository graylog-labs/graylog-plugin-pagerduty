/**
 * Copyright 2013-2014 TORCH GmbH, 2015 Graylog, Inc.
 *
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.alarmcallbacks.pagerduty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

public class PagerDutyClient {
    private static final Logger LOG = LoggerFactory.getLogger(PagerDutyClient.class);
    private static final String API_URL = "https://events.pagerduty.com/generic/2010-04-15/create_event.json";

    private final String serviceKey;
    private final boolean customIncidentKey;
    private final String incidentKeyPrefix;
    private final String clientName;
    private final String clientUrl;
    private final ObjectMapper objectMapper;

    @VisibleForTesting
    PagerDutyClient(final String serviceKey,
                    final boolean customIncidentKey,
                    final String incidentKeyPrefix,
                    final String clientName,
                    final String clientUrl,
                    final ObjectMapper objectMapper) {
        this.serviceKey = serviceKey;
        this.customIncidentKey = customIncidentKey;
        this.incidentKeyPrefix = incidentKeyPrefix;
        this.clientName = clientName;
        this.clientUrl = clientUrl;
        this.objectMapper = objectMapper;
    }

    public PagerDutyClient(final String serviceKey,
                           final boolean customIncidentKey,
                           final String incidentKeyPrefix,
                           final String clientName,
                           final String clientUrl) {
        this(serviceKey, customIncidentKey, incidentKeyPrefix, clientName, clientUrl, new ObjectMapper());
    }

    public void trigger(final Stream stream, final AlertCondition.CheckResult checkResult) throws AlarmCallbackException {
        final URL url;
        try {
            url = new URL(API_URL);
        } catch (MalformedURLException e) {
            throw new AlarmCallbackException("Malformed URL for PagerDuty API.", e);
        }

        final HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
        } catch (IOException e) {
            throw new AlarmCallbackException("Error while opening connection to PagerDuty API.", e);
        }

        try (final OutputStream requestStream = conn.getOutputStream()) {
            final PagerDutyEvent event = buildPagerDutyEvent(stream, checkResult);
            requestStream.write(objectMapper.writeValueAsBytes(event));
            requestStream.flush();

            final InputStream responseStream;
            if (conn.getResponseCode() == 200) {
                responseStream = conn.getInputStream();
            } else {
                responseStream = conn.getErrorStream();
            }

            final PagerDutyResponse response = objectMapper.readValue(responseStream, PagerDutyResponse.class);
            if ("success".equals(response.status)) {
                LOG.debug("Successfully sent event to PagerDuty with incident key {}", response.incidentKey);
            } else {
                LOG.warn("Error while creating event at PagerDuty: {} ({})", response.message, response.errors);
                throw new AlarmCallbackException("Error while creating event at PagerDuty: " + response.message);
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST event trigger to PagerDuty API.", e);
        }
    }
    private String buildStreamLink(String baseUrl, Stream stream) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl + "streams/" + stream.getId() + "/messages?q=*&rangetype=relative&relative=3600";
    }

    private PagerDutyEvent buildPagerDutyEvent(final Stream stream, final AlertCondition.CheckResult checkResult) {
        final String incidentKey;
        if (customIncidentKey) {
            incidentKey = nullToEmpty(incidentKeyPrefix) + stream.getId() + "/" + checkResult.getTriggeredCondition().getId();
        } else {
            incidentKey = "";
        }
        final String alertDescription = checkResult.getTriggeredCondition().getDescription();
        final String description = "[ " + stream.getTitle() + " ] " + checkResult.getResultDescription() + " - "
                                    + buildStreamLink(clientUrl, stream);

        return new PagerDutyEvent(
                serviceKey, "trigger", description, incidentKey, clientName, buildStreamLink(clientUrl, stream),
                ImmutableMap.<String, Object>of(
                        "stream_id", stream.getId(),
                        "stream_title", stream.getTitle(),
                        "backlog", checkResult.getTriggeredCondition().getBacklog(),
                        "search_hits", getAlarmBacklog(checkResult).size(),
                        "alert_description", alertDescription
                ),
                ImmutableList.<Object>of(
                        ImmutableMap.<String, Object>of(
                                "type", "link",
                                "href", buildStreamLink(clientUrl, stream)
                        )

                )
        );
    }

    protected List<Message> getAlarmBacklog(AlertCondition.CheckResult result) {
        final AlertCondition alertCondition = result.getTriggeredCondition();
        final List<MessageSummary> matchingMessages = result.getMatchingMessages();

        final int effectiveBacklogSize = Math.min(alertCondition.getBacklog(), matchingMessages.size());

        if (effectiveBacklogSize == 0) {
            return Collections.emptyList();
        }

        final List<MessageSummary> backlogSummaries = matchingMessages.subList(0, effectiveBacklogSize);

        final List<Message> backlog = Lists.newArrayListWithCapacity(effectiveBacklogSize);

        for (MessageSummary messageSummary : backlogSummaries) {
            backlog.add(messageSummary.getRawMessage());
        }

        return backlog;
    }

    // See http://developer.pagerduty.com/documentation/integration/events/trigger
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class PagerDutyEvent {
        @JsonProperty("service_key")
        public String serviceKey;
        @JsonProperty("event_type")
        public String eventType;
        @JsonProperty
        public String description;
        @JsonProperty("incident_key")
        public String incidentKey;
        @JsonProperty
        public String client;
        @JsonProperty("client_url")
        public String clientUrl;
        @JsonProperty
        public Map<String, Object> details;
        @JsonProperty
        public List<Object> contexts;

        public PagerDutyEvent(String serviceKey,
                              String eventType,
                              String description,
                              String incidentKey,
                              String client,
                              String clientUrl,
                              Map<String, Object> details,
                              List<Object> contexts) {
            this.serviceKey = serviceKey;
            this.eventType = eventType;
            this.description = description;
            this.incidentKey = incidentKey;
            this.client = client;
            this.clientUrl = clientUrl;
            this.details = details;
            this.contexts = contexts;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PagerDutyResponse {
        @JsonProperty
        public String status;
        @JsonProperty
        public String message;
        @JsonProperty("incident_key")
        public String incidentKey;
        @JsonProperty
        public List<String> errors;
    }
}
