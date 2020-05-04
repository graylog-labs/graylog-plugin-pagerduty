/**
 * This file is part of Graylog PagerDuty plugin.
 *
 * Graylog PagerDuty Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog PagerDuty Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog PagerDuty Plugin.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.graylog.plugins.pagerduty;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.graylog.events.notifications.EventNotification;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.EventNotificationException;
import org.graylog.plugins.pagerduty.client.ClientFactory;
import org.graylog.plugins.pagerduty.client.PagerDuty;
import org.graylog.plugins.pagerduty.dto.PagerDutyResponse;
import org.graylog2.streams.StreamService;

/**
 * Main class that focuses on event notifications that should be send to PagerDuty.
 * 
 * @author Edgar Molina
 *
 */
public class PagerDutyNotification implements EventNotification
{
    private final StreamService streamService;
    private final ClientFactory clientFactory;

    @Inject
    PagerDutyNotification(StreamService streamService) {
        this(streamService, new ClientFactory());
    }

    PagerDutyNotification(StreamService streamService, ClientFactory clientFactory) {
        this.streamService = streamService;
        this.clientFactory = clientFactory;
    }

    public interface Factory extends EventNotification.Factory {
        @Override
        PagerDutyNotification create();
    }

    @Override
    public void execute(EventNotificationContext ctx) throws EventNotificationException {
        final PagerDutyNotificationConfig config =
            (PagerDutyNotificationConfig) ctx.notificationConfig();

        try (PagerDuty client = clientFactory.create(streamService, config)) {
            PagerDutyResponse response = client.trigger(ctx);
            List<String> errors = response.getErrors();
            if (errors != null && errors.size() > 0) {
                throw new IllegalStateException(
                    "There was an error triggering the PagerDuty event, details: " + errors);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException(
                "There was an exception triggering the PagerDuty event.", e);
        }
    }

}
