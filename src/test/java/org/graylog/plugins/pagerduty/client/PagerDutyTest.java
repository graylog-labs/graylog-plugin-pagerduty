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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.plugins.pagerduty.PagerDutyNotificationConfig;
import org.graylog.plugins.pagerduty.dto.PagerDutyMessage;
import org.graylog.plugins.pagerduty.dto.PagerDutyResponse;
import org.graylog2.streams.StreamService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

/**
 * @author Edgar Molina
 *
 */
public class PagerDutyTest
{
    @Mock
    private StreamService streamServiceMock;
    @Mock
    private PagerDutyNotificationConfig configMock;
    @Mock
    private CloseableHttpClient httpClientMock;
    @Mock
    private ObjectMapper objectMapperMock;
    @Mock
    private MessageFactory messageFactoryMock;
    @Mock
    private Logger loggerMock;
    @Mock
    private PagerDutyMessage messageMock;
    @Mock
    private EventNotificationContext contextMock;
    @Mock
    private CloseableHttpResponse httpResponseMock;
    @Mock
    private HttpEntity entityMock;
    @Mock
    private InputStream contentMock;
    @Mock
    private PagerDutyResponse pagerDutyResponseMock;


    private PagerDuty sut;

    @Before
    public void setUp() throws ClientProtocolException, IOException
    {
        MockitoAnnotations.initMocks(this);
        when(messageFactoryMock.createTriggerMessage(contextMock)).thenReturn(messageMock);
        when(objectMapperMock.writeValueAsString(messageMock)).thenReturn("{test='json'}");
        when(httpClientMock.execute(any(HttpPost.class))).thenReturn(httpResponseMock);
        when(entityMock.getContent()).thenReturn(contentMock);
        when(httpResponseMock.getEntity()).thenReturn(entityMock);
        when(objectMapperMock.readValue(contentMock, PagerDutyResponse.class))
            .thenReturn(pagerDutyResponseMock);
        sut =
            new PagerDuty(
                streamServiceMock,
                configMock,
                httpClientMock,
                objectMapperMock,
                messageFactoryMock,
                loggerMock);
    }

    @Test
    public void testSuccessfulTriggerAttempt() throws IOException
    {
        // Execute
        PagerDutyResponse result = sut.trigger(contextMock);

        // Assert
        assertEquals("Wrong Response Object", pagerDutyResponseMock, result);
        verify(httpResponseMock).close();
        verify(loggerMock).debug("Triggering event in PagerDuty with context: {}", contextMock);
        verify(loggerMock).debug("Request Payload: {}", "{test='json'}");
    }

    @Test
    public void testSuccessfulClose() throws IOException
    {
        // Execute
        sut.close();

        // Assert
        verify(httpClientMock, atMostOnce()).close();
    }

    @Test (expected = IOException.class)
    public void testExceptionWhenClosing() throws IOException
    {
        // Set up
        doThrow(IOException.class).when(httpClientMock).close();

        // Execute
        sut.close();
    }

    @Test (expected = IllegalStateException.class)
    public void testTriggerAttemptWithException() throws IOException
    {
        // Setup
        when(httpClientMock.execute(any(HttpPost.class))).thenThrow(IOException.class);

        // Execute
        sut.trigger(contextMock);
    }
}
