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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class PagerDutyAlarmCallback implements AlarmCallback {
    private static final String CK_SERVICE_KEY = "service_key";
    private static final String CK_CUSTOM_INCIDENT_KEY = "use_custom_incident_key";
    private static final String CK_INCIDENT_KEY_PREFIX = "incident_key_prefix";
    private static final String CK_CLIENT = "client";
    private static final String CK_CLIENT_URL = "client_url";

    private Configuration configuration;

    @Override
    public void initialize(final Configuration config) throws AlarmCallbackConfigurationException {
        this.configuration = config;
    }

    @Override
    public void call(final Stream stream, final AlertCondition.CheckResult result) throws AlarmCallbackException {
        call(new PagerDutyClient(
                configuration.getString(CK_SERVICE_KEY),
                configuration.getBoolean(CK_CUSTOM_INCIDENT_KEY),
                configuration.getString(CK_INCIDENT_KEY_PREFIX),
                configuration.getString(CK_CLIENT),
                configuration.getString(CK_CLIENT_URL)), stream, result);
    }

    @VisibleForTesting
    void call(final PagerDutyClient client, final Stream stream, final AlertCondition.CheckResult result)
            throws AlarmCallbackException {
        client.trigger(stream, result);
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        final ConfigurationRequest configurationRequest = new ConfigurationRequest();

        configurationRequest.addField(new TextField(
                CK_SERVICE_KEY, "PagerDuty service key", "", "PagerDuty service key",
                ConfigurationField.Optional.NOT_OPTIONAL));
        configurationRequest.addField(new BooleanField(
                CK_CUSTOM_INCIDENT_KEY, "Use custom incident key", true,
                "Generate a custom incident key based on the Stream and the Alert Condition."));
        configurationRequest.addField(new TextField(
                CK_INCIDENT_KEY_PREFIX, "Incident key prefix", "Graylog/",
                "Identifies the incident.",
                ConfigurationField.Optional.OPTIONAL));
        configurationRequest.addField(new TextField(
                CK_CLIENT, "Client name", "Graylog",
                "The name of the Graylog system that is triggering the PagerDuty event.",
                ConfigurationField.Optional.OPTIONAL));
        configurationRequest.addField(new TextField(
                CK_CLIENT_URL, "Client URL", "",
                "The URL of the Graylog system that is triggering the PagerDuty event.",
                ConfigurationField.Optional.OPTIONAL));

        return configurationRequest;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Maps.transformEntries(configuration.getSource(), new Maps.EntryTransformer<String, Object, Object>() {
            @Override
            public Object transformEntry(String key, Object value) {
                if (CK_SERVICE_KEY.equals(key)) {
                    return "****";
                }
                return value;
            }
        });
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        if (!configuration.stringIsSet(CK_SERVICE_KEY)) {
            throw new ConfigurationException(CK_SERVICE_KEY + " is mandatory and must be not be null or empty.");
        }

        if (configuration.getString(CK_SERVICE_KEY).length() != 32) {
            throw new ConfigurationException(CK_SERVICE_KEY + " must be 32 characters long.");
        }

        if (configuration.stringIsSet(CK_CLIENT_URL)) {
            try {
                final URI clientUri = new URI(configuration.getString(CK_CLIENT_URL));

                if (!"http".equals(clientUri.getScheme()) && !"https".equals(clientUri.getScheme())) {
                    throw new ConfigurationException(CK_CLIENT_URL + " must be a valid HTTP or HTTPS URL.");
                }
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Couldn't parse " + CK_CLIENT_URL + " correctly.", e);
            }
        }
    }

    @Override
    public String getName() {
        return "PagerDuty alarm callback";
    }
}
