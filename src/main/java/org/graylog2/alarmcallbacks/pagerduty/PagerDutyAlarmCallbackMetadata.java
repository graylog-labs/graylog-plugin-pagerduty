package org.graylog2.alarmcallbacks.pagerduty;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.Version;

import java.net.URI;

public class PagerDutyAlarmCallbackMetadata implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return PagerDutyAlarmCallback.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "PagerDuty alarm callback";
    }

    @Override
    public String getAuthor() {
        return "TORCH GmbH";
    }

    @Override
    public URI getURL() {
        return URI.create("http://www.torch.sh");
    }

    @Override
    public Version getVersion() {
        return new Version(0, 90, 0);
    }

    @Override
    public String getDescription() {
        return "Alarm callback plugin that sends all stream alerts to a PagerDuty.";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(0, 90, 0);
    }
}
