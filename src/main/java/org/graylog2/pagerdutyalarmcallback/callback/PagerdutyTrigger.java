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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import org.graylog2.plugin.alarms.Alarm;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.json.simple.JSONObject;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class PagerdutyTrigger {
    
    private final String serviceKey;
    
    private final int SUCCESS_RESPONSE_CODE = 200;
    private final String API_URL = "https://events.pagerduty.com/generic/2010-04-15/create_event.json"; // date seems to be the api version
    
    public PagerdutyTrigger(String serviceKey) {
        this.serviceKey = serviceKey;
    }
    
    public void trigger(Alarm alarm) throws AlarmCallbackException {
        Writer writer = null;

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(buildParametersFromAlarm(alarm));
            writer.flush();

            if (conn.getResponseCode() != SUCCESS_RESPONSE_CODE) {
                throw new AlarmCallbackException("Could not POST event trigger. Expected HTTP response code "
                        + "<" + SUCCESS_RESPONSE_CODE + "> but got <" + conn.getResponseCode() + ">.");
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST event trigger. IOException");
        } finally {
            // Always close the writer.
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) { /* Gnah. */ }
            }
        }
    }
    
    private String buildParametersFromAlarm(Alarm alarm) {
        JSONObject obj = new JSONObject();
        
        obj.put("service_key", serviceKey);
        obj.put("event_type", "trigger");
        obj.put("description", alarm.getDescription());
        
        return obj.toJSONString();
    }
    
}
