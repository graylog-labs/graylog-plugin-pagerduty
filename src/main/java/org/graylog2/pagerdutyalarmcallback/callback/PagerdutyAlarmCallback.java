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
package org.graylog2.pagerdutyalarmcallback.callback;

import java.util.Map;
import org.graylog2.plugin.alarms.Alarm;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class PagerdutyAlarmCallback implements AlarmCallback {

    public static final String NAME = "PagerDuty alarm trigger";
    
    private String serviceKey;
    
    public void initialize(Map<String, String> config) throws AlarmCallbackConfigurationException {
        if (config == null || !config.containsKey("service_key")) {
            throw new AlarmCallbackConfigurationException("Required config parameter service_key is missing.");
        }
        
        this.serviceKey = config.get("service_key");
    }

    public void call(Alarm alarm) throws AlarmCallbackException {
        PagerdutyTrigger trigger = new PagerdutyTrigger(serviceKey);
        trigger.trigger(alarm);
    }

    public String getName() {
        return NAME;
    }

}
