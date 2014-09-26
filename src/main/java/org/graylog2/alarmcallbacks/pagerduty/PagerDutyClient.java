package org.graylog2.alarmcallbacks.pagerduty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
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

    private PagerDutyEvent buildPagerDutyEvent(final Stream stream, final AlertCondition.CheckResult checkResult) {
        final String incidentKey;
        if (customIncidentKey) {
            incidentKey = nullToEmpty(incidentKeyPrefix) + stream.getId() + "/" + checkResult.getTriggeredCondition().getId();
        } else {
            incidentKey = "";
        }

        return new PagerDutyEvent(
                serviceKey, "trigger", checkResult.getResultDescription(), incidentKey, clientName, clientUrl,
                ImmutableMap.<String, Object>of(
                        "stream_id", stream.getId(),
                        "stream_title", stream.getTitle(),
                        "backlog", checkResult.getTriggeredCondition().getBacklog(),
                        "search_hits", checkResult.getTriggeredCondition().getSearchHits().size(),
                        "alert_description", checkResult.getTriggeredCondition().getDescription()
                )
        );
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

        public PagerDutyEvent(String serviceKey,
                              String eventType,
                              String description,
                              String incidentKey,
                              String client,
                              String clientUrl,
                              Map<String, Object> details) {
            this.serviceKey = serviceKey;
            this.eventType = eventType;
            this.description = description;
            this.incidentKey = incidentKey;
            this.client = client;
            this.clientUrl = clientUrl;
            this.details = details;
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
