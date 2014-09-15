package org.graylog2.alarmcallbacks.pagerduty;

import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class PagerDutyAlarmCallbackTest {
    private PagerDutyAlarmCallback pagerDutyAlarmCallback;

    @Before
    public void setUp() {
        pagerDutyAlarmCallback = new PagerDutyAlarmCallback();
    }

    @Test
    public void testInitialize() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(
                Collections.<String, Object>singletonMap("service_key", "TEST_service_key")
        );
        pagerDutyAlarmCallback.initialize(configuration);
    }

    @Test
    public void testGetRequestedConfiguration() {
        assertThat(pagerDutyAlarmCallback.getRequestedConfiguration().asList().keySet(), hasItems("service_key"));
    }

    @Test
    public void testGetAttributes() throws AlarmCallbackConfigurationException {
        pagerDutyAlarmCallback.initialize(new Configuration(
                Collections.<String, Object>singletonMap("service_key", "TEST_service_key")
        ));
        assertThat(pagerDutyAlarmCallback.getAttributes().keySet(), hasItems("service_key"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        pagerDutyAlarmCallback.initialize(new Configuration(
                Collections.<String, Object>singletonMap("service_key", "TEST_service_key")
        ));

        pagerDutyAlarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfServiceKeyIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        pagerDutyAlarmCallback.initialize(new Configuration(Collections.<String, Object>emptyMap()));

        pagerDutyAlarmCallback.checkConfiguration();
    }

    @Test
    public void testGetName() {
        assertThat(pagerDutyAlarmCallback.getName(), equalTo("PagerDuty alarm callback"));
    }
}