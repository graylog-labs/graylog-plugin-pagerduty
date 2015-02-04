/**
 * Copyright 2013-2014 TORCH GmbH
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

import com.google.common.collect.ImmutableMap;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PagerDutyAlarmCallbackTest {
    private static final Configuration VALID_CONFIGURATION = new Configuration(
            ImmutableMap.<String, Object>of(
                    "service_key", "TEST_service_keyTEST_service_key",
                    "use_custom_incident_key", true,
                    "incident_key_prefix", "Prefix/",
                    "client", "TEST_Client",
                    "client_url", "http://www.graylog.org"
            ));
    private PagerDutyAlarmCallback pagerDutyAlarmCallback;

    @Before
    public void setUp() {
        pagerDutyAlarmCallback = new PagerDutyAlarmCallback();
    }

    @Test
    public void testInitialize() throws AlarmCallbackConfigurationException {
        pagerDutyAlarmCallback.initialize(VALID_CONFIGURATION);
    }

    @Test
    public void testGetRequestedConfiguration() {
        assertThat(pagerDutyAlarmCallback.getRequestedConfiguration().asList().keySet(),
                hasItems("service_key", "use_custom_incident_key", "incident_key_prefix", "client", "client_url"));
    }

    @Test
    public void testGetAttributes() throws AlarmCallbackConfigurationException {
        pagerDutyAlarmCallback.initialize(VALID_CONFIGURATION);
        assertThat(pagerDutyAlarmCallback.getAttributes().keySet(),
                hasItems("service_key", "use_custom_incident_key", "incident_key_prefix", "client", "client_url"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        pagerDutyAlarmCallback.initialize(VALID_CONFIGURATION);
        pagerDutyAlarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfServiceKeyIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        pagerDutyAlarmCallback.initialize(new Configuration(Collections.<String, Object>emptyMap()));

        pagerDutyAlarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfServiceKeyIsInvalid()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        pagerDutyAlarmCallback.initialize(new Configuration(
                Collections.<String, Object>singletonMap("service_key", "too short")
        ));

        pagerDutyAlarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfClientUriIsInvalid()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        pagerDutyAlarmCallback.initialize(new Configuration(
                ImmutableMap.<String, Object>of(
                        "service_key", "too short",
                        "client_url", "ftp://example.com")
        ));

        pagerDutyAlarmCallback.checkConfiguration();
    }

    @Test
    public void testCall() throws AlarmCallbackConfigurationException, ConfigurationException, AlarmCallbackException {
        final PagerDutyClient client = mock(PagerDutyClient.class);
        final Stream stream = mock(Stream.class);
        final AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        pagerDutyAlarmCallback.initialize(VALID_CONFIGURATION);
        pagerDutyAlarmCallback.checkConfiguration();
        pagerDutyAlarmCallback.call(client, stream, checkResult);

        verify(client).trigger(stream, checkResult);
    }

    @Test
    public void testGetName() {
        assertThat(pagerDutyAlarmCallback.getName(), equalTo("PagerDuty alarm callback"));
    }
}