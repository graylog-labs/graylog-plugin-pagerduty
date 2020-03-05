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

package org.graylog.plugins.pagerduty.client;

import org.graylog.plugins.pagerduty.PagerDutyNotificationConfig;
import org.graylog2.streams.StreamService;

/**
 * Factory class for the PagerDuty client.
 *
 * @author Edgar Molina
 *
 */
public class ClientFactory {
    public PagerDuty create(StreamService streamService, PagerDutyNotificationConfig config) {
        return new PagerDuty(streamService, config);
    }
}
