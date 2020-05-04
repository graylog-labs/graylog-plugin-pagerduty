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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.plugins.pagerduty.PagerDutyNotificationConfig;
import org.graylog.plugins.pagerduty.dto.PagerDutyResponse;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Pager Duty REST client implementation class compatible with events V2. For more information
 * about the event structure please see
 * <a href="https://v2.developer.pagerduty.com/v2/docs/send-an-event-events-api-v2">the api</a>.
 *
 * This class is heavily based on the work commited by Jochen, James and Dennis
 * <a href="https://github.com/graylog-labs/graylog-plugin-pagerduty/">here</a>.
 *
 * @author Jochen Schalanda
 * @author James Carr
 * @author Dennis Oelkers
 * @author Padma Liyanage
 * @author Edgar Molina
 */
public class PagerDuty implements AutoCloseable {
    private static final String PAGER_DUTY_NOTIFICATION_PLUGIN = "PagerDutyNotificationPlugin";
    private static final String API_URL = "https://events.pagerduty.com/v2/enqueue";
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final MessageFactory messageFactory;

    public PagerDuty(final StreamService streamService, final PagerDutyNotificationConfig config) {
        this(
            streamService,
            config,
            HttpClients.createDefault(),
            new ObjectMapper(),
            new MessageFactory(streamService, config),
            LoggerFactory.getLogger(PAGER_DUTY_NOTIFICATION_PLUGIN));
    }

    @VisibleForTesting
    PagerDuty(
        final StreamService streamService,
        final PagerDutyNotificationConfig config,
        final CloseableHttpClient httpClient,
        final ObjectMapper objectMapper,
        final MessageFactory messageFactory,
        final Logger logger) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.messageFactory = messageFactory;
        this.logger = logger;
    }

    public PagerDutyResponse trigger(EventNotificationContext ctx) {
        try {
            final String payloadString = objectMapper.writeValueAsString(
                messageFactory.createTriggerMessage(ctx));
            final StringEntity payloadEntity = new StringEntity(payloadString);
            final HttpPost httpPost = new HttpPost(API_URL);

            logger.debug("Triggering event in PagerDuty with context: {}", ctx);
            logger.debug("Request Payload: {}", payloadString);
            httpPost.setEntity(payloadEntity);
            try (CloseableHttpResponse response = httpClient.execute(httpPost))
            {
                return objectMapper.readValue(
                    response.getEntity().getContent(), PagerDutyResponse.class);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException(
                "There was an error sending the notification event.", e);
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

}
