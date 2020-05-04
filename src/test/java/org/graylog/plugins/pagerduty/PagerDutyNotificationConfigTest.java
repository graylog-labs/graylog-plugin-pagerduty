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

import static org.junit.Assert.assertEquals;
import org.graylog2.plugin.rest.ValidationResult;
import org.junit.Test;

/**
 * @author Edgar Molina
 *
 */
public class PagerDutyNotificationConfigTest {
    PagerDutyNotificationConfig.Builder sutBuilder = PagerDutyNotificationConfig.Builder.create();

    @Test
    public void testValidConfigurationWithHttp() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestName")
                .clientUrl("http://test/")
                .build()
                .validate();
        assertEquals("Error count", 0, result.getErrors().size());
    }

    @Test
    public void testValidConfigurationWithHttps() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestName")
                .clientUrl("https://test/")
                .build()
                .validate();
        assertEquals("Error count", 0, result.getErrors().size());
    }

    @Test
    public void testEmptyRoutingKeyValidation() {
        ValidationResult result =
            sutBuilder
                .routingKey("")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestClient")
                .clientUrl("http://test/")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{routing_key=[Routing Key cannot be empty.]}",
            result.getErrors().toString());
    }

    @Test
    public void testTooShortRoutingKey() {
        ValidationResult result =
            sutBuilder
                .routingKey("TestRouting")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestClient")
                .clientUrl("http://test/")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{routing_key=[Routing Key must be 32 characters long.]}",
            result.getErrors().toString());
    }

    @Test
    public void testTooLongRoutingKey() {
        ValidationResult result =
            sutBuilder
                .routingKey("0123456789012345678901234567890123456789")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestClient")
                .clientUrl("http://test/")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{routing_key=[Routing Key must be 32 characters long.]}",
            result.getErrors().toString());
    }

    @Test
    public void testEmptyKeyPrefix() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("")
                .clientName("TestClient")
                .clientUrl("http://test/")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{key_prefix=[Incident Key Prefix cannot be empty.]}",
            result.getErrors().toString());
    }

    @Test
    public void testEmptyClientName() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("")
                .clientUrl("http://test/")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{client_name=[Client Name cannot be empty.]}",
            result.getErrors().toString());
    }

    @Test
    public void testEmptyClientURL() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestName")
                .clientUrl("")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{client_url=[Client URL cannot be empty.]}",
            result.getErrors().toString());
    }

    @Test
    public void testWrongClientURLFormat() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestName")
                .clientUrl("te\\st")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{client_url=[Couldn't parse Client URL correctly.]}",
            result.getErrors().toString());
    }

    @Test
    public void testWrongClientURLProtocol() {
        ValidationResult result =
            sutBuilder
                .routingKey("01234567890123456789012345678901")
                .customIncident(false)
                .keyPrefix("TestPrefix")
                .clientName("TestName")
                .clientUrl("git://test/")
                .build()
                .validate();
        assertEquals("Error count", 1, result.getErrors().size());
        assertEquals(
            "Error message",
            "{client_url=[Client URL must be a valid HTTP or HTTPS URL.]}",
            result.getErrors().toString());
    }
}
