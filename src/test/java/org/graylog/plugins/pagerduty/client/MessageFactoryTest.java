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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.graylog.events.event.EventDto;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.plugins.pagerduty.PagerDutyNotificationConfig;
import org.graylog.plugins.pagerduty.dto.Link;
import org.graylog.plugins.pagerduty.dto.PagerDutyMessage;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Edgar Molina
 *
 */
public class MessageFactoryTest {
    private static final String TEST_EVENT_TITLE = "Test title";
    private static final String CLIENT_NAME = "ClientName";
    private static final String KEY_PREFIX = "KeyPrefix";
    private static final String ROUTING_KEY = "RoutingKey";
    private static final String TEST_STREAM_TITLE = "Test Stream Title";
    private static final String CLIENT_URL = "https://test";
    private static final String STREAM_ID = "0001";

    @Mock
    private StreamService streamServiceMock;
    @Mock
    private PagerDutyNotificationConfig configMock;
    @Mock
    private EventDto eventMock;
    @Mock
    private EventNotificationContext ctxMock;
    @Mock
    private EventDefinitionDto eventDefinitionMock;
    @Mock
    private Stream streamMock;
    @Mock
    private AggregationEventProcessorConfig eventDefinitionConfigMock;
    private final Set<String> sourceStreams = new HashSet<>();
    private final Set<Stream> streams = new HashSet<>();
    private Optional<EventDefinitionDto> eventDefinition;

    private MessageFactory sut;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        eventDefinition = Optional.of(eventDefinitionMock);
        when(configMock.customIncident()).thenReturn(true);
        when(configMock.keyPrefix()).thenReturn(KEY_PREFIX);
        when(configMock.routingKey()).thenReturn(ROUTING_KEY);
        when(configMock.clientName()).thenReturn(CLIENT_NAME);
        when(configMock.clientUrl()).thenReturn(CLIENT_URL);
        when(streamMock.getId()).thenReturn(STREAM_ID);
        when(streamMock.getTitle()).thenReturn(TEST_STREAM_TITLE);
        when(eventMock.sourceStreams()).thenReturn(sourceStreams);
        when(eventMock.message()).thenReturn("Test Event Message");
        when(eventMock.eventTimestamp()).thenReturn(new DateTime(1));
        when(streamServiceMock.loadByIds(sourceStreams)).thenReturn(streams);
        when(ctxMock.event()).thenReturn(eventMock);
        when(eventDefinitionConfigMock.query()).thenReturn("Test=Query");
        when(eventDefinitionMock.title()).thenReturn(TEST_EVENT_TITLE);
        when(eventDefinitionMock.priority()).thenReturn(3);
        when(eventDefinitionMock.config()).thenReturn(eventDefinitionConfigMock);
        when(ctxMock.eventDefinition()).thenReturn(eventDefinition);

        sourceStreams.add(STREAM_ID);
        streams.add(streamMock);

        sut = new MessageFactory(streamServiceMock, configMock);
    }

    @Test
    public void testCriticalMessageWithAllSettingSet() {
        // Set up
        when(eventDefinitionMock.priority()).thenReturn(3);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[" + STREAM_ID + "]/" + TEST_EVENT_TITLE,
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search?q=Test=Query",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=critical, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test
    public void testWarningMessageWithAllSettingSet() {
        // Set up
        when(eventDefinitionMock.priority()).thenReturn(2);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[" + STREAM_ID + "]/" + TEST_EVENT_TITLE,
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search?q=Test=Query",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=warning, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test
    public void testInfoMessageWithAllSettingSet() {
        // Set up
        when(eventDefinitionMock.priority()).thenReturn(1);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[" + STREAM_ID + "]/" + TEST_EVENT_TITLE,
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search?q=Test=Query",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=info, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test
    public void testNegativeSeverityMessage() {
        // Set up
        when(eventDefinitionMock.priority()).thenReturn(-1);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[" + STREAM_ID + "]/" + TEST_EVENT_TITLE,
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search?q=Test=Query",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=info, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test
    public void testOutOfRangeSeverityMessage() {
        // Set up
        when(eventDefinitionMock.priority()).thenReturn(-1);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[" + STREAM_ID + "]/" + TEST_EVENT_TITLE,
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search?q=Test=Query",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=info, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test
    public void testNoCustomIncident() {
        // Set up
        when(configMock.customIncident()).thenReturn(false);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals("Wrong DedupKey", "", result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search?q=Test=Query",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=critical, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test
    public void testNoSourceStreams() {
        // Set up
        sourceStreams.clear();
        streams.clear();

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[]/" + TEST_EVENT_TITLE,
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 0, result.getLinks().size());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=critical, "
            + "component=GraylogAlerts, source=Graylog:[], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[]}",
            result.getPayload().toString());
    }

    @Test
    public void testNoEventDefinition() {
        // Set up
        eventDefinition = Optional.empty();
        when(ctxMock.eventDefinition()).thenReturn(eventDefinition);

        // Execute
        PagerDutyMessage result = sut.createTriggerMessage(ctxMock);

        // Assert
        assertEquals("Wrong RoutingKey", ROUTING_KEY, result.getRoutingKey());
        assertEquals("Wrong Event Action", "trigger", result.getEventAction());
        assertEquals(
            "Wrong DedupKey",
            KEY_PREFIX + "/[" + STREAM_ID + "]/Undefined",
            result.getDedupKey());
        assertEquals("Wrong ClientName", CLIENT_NAME, result.getClient());
        assertEquals("Wrong ClientUrl", CLIENT_URL, result.getClientUrl());
        assertEquals("Wrong Stream Links Count", 1, result.getLinks().size());
        Link streamLink = result.getLinks().get(0);
        assertEquals(
            "Wrong Link Href",
            "https://test/streams/0001/search",
            streamLink.getHref().toString());
        assertEquals("Wrong Link Text", TEST_STREAM_TITLE, streamLink.getText());
        assertEquals(
            "Wrong Payload",
            "{summary=Test Event Message, "
            + "severity=info, "
            + "component=GraylogAlerts, source=Graylog:[0001], "
            + "class=alerts, "
            + "timestamp=1969-12-31T18:00:00.001-06:00, "
            + "group=[0001]}",
            result.getPayload().toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testMalformedClientUrl() {
        // Set up
        when(configMock.clientUrl()).thenReturn("Test\\Wrong\\URL");

        // Execute
        sut.createTriggerMessage(ctxMock);
    }
}
