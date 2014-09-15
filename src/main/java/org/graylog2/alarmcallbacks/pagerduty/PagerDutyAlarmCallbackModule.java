package org.graylog2.alarmcallbacks.pagerduty;

import org.graylog2.plugin.PluginModule;

public class PagerDutyAlarmCallbackModule extends PluginModule {
    @Override
    protected void configure() {
        registerPlugin(PagerDutyAlarmCallbackMetadata.class);
        addAlarmCallback(PagerDutyAlarmCallback.class);
    }
}
