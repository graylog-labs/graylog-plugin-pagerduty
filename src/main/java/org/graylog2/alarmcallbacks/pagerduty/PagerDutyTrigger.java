/**
 * Copyright 2012 Lennart Koopmann <lennart@socketfeed.com>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.alarmcallbacks.pagerduty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.ImmutableMap;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class PagerDutyTrigger {
    private static final String API_URL = "https://events.pagerduty.com/generic/2010-04-15/create_event.json";

    private final String serviceKey;
    private final ObjectMapper objectMapper;

    public PagerDutyTrigger(final String serviceKey, final ObjectMapper objectMapper) {
        this.serviceKey = serviceKey;
        this.objectMapper = objectMapper;
    }

    public PagerDutyTrigger(final String serviceKey) {
        this(serviceKey,
                new ObjectMapper()
                        .setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES));
    }

    public void trigger(final AlertCondition.CheckResult checkResult) throws AlarmCallbackException {
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

        try (final Writer writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(buildParameters(checkResult.getResultDescription()));
            writer.flush();

            if (conn.getResponseCode() != 200) {
                throw new AlarmCallbackException("Unexpected HTTP response status <" + conn.getResponseCode() + ">.");
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST event trigger to PagerDuty API.", e);
        }
    }

    private String buildParameters(final String description) throws JsonProcessingException {
        final Map<String, String> parameters = ImmutableMap.of(
                "service_key", serviceKey,
                "event_type", "trigger",
                "description", description);

        return objectMapper.writeValueAsString(parameters);
    }
}
