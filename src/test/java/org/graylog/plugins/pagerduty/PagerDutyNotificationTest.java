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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.EventNotificationException;
import org.graylog.plugins.pagerduty.client.ClientFactory;
import org.graylog.plugins.pagerduty.client.PagerDuty;
import org.graylog.plugins.pagerduty.dto.PagerDutyResponse;
import org.graylog2.streams.StreamService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Edgar Molina
 *
 */
public class PagerDutyNotificationTest {
    @Mock
    private StreamService streamServiceMock;
    @Mock
    private ClientFactory clientFactoryMock;
    @Mock
    private EventNotificationContext contextMock;
    @Mock
    private PagerDutyNotificationConfig configMock;
    @Mock
    private PagerDuty clientMock;
    @Mock
    private PagerDutyResponse responseMock;

    private PagerDutyNotification sut;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(contextMock.notificationConfig()).thenReturn(configMock);
        when(clientFactoryMock.create(streamServiceMock, configMock)).thenReturn(clientMock);
        sut = new PagerDutyNotification(streamServiceMock, clientFactoryMock);
    }

    @Test
    public void testSuccessfullNotificationTrigger() throws EventNotificationException {
        // Setup
        when(clientMock.trigger(contextMock)).thenReturn(responseMock);
        when(responseMock.getErrors()).thenReturn(null);

        // Execute
        sut.execute(contextMock);

        // Assert
        verify(clientMock).trigger(contextMock);
        verify(responseMock).getErrors();
    }

    @Test
    public void testSuccessfulNotificationTriggerWithoutErrors() throws EventNotificationException {
        // Setup
        when(clientMock.trigger(contextMock)).thenReturn(responseMock);
        when(responseMock.getErrors()).thenReturn(new ArrayList<>());

        // Execute
        sut.execute(contextMock);

        // Assert
        verify(clientMock).trigger(contextMock);
        verify(responseMock).getErrors();
    }

    @Test (expected = IllegalStateException.class)
    public void testFailedNotificationTriggerWithErrors() throws EventNotificationException {
        // Setup
        final List<String> errorsReported = Arrays.asList("first error", "second error");
        when(clientMock.trigger(contextMock)).thenReturn(responseMock);
        when(responseMock.getErrors()).thenReturn(errorsReported);

        try {
            // Execute
            sut.execute(contextMock);
        }
        catch (IllegalStateException e) {
            // Assert
            assertEquals(
                "Wrong Exception Message",
                "There was an error triggering the PagerDuty event, details: "
                    + "[first error, second error]",
                e.getMessage());
            throw e;
        }
    }

    @Test (expected = IllegalStateException.class)
    public void testFailedIOExceptionCreatingTheClient()
    throws EventNotificationException, IOException {
        // Setup
        when(clientMock.trigger(contextMock)).thenReturn(responseMock);
        when(responseMock.getErrors()).thenReturn(null);
        doThrow(IOException.class).when(clientMock).close();

        try {
            // Execute
            sut.execute(contextMock);
        }
        catch (IllegalStateException e) {
            // Assert
            assertEquals(
                "Wrong Exception Message",
                "There was an exception triggering the PagerDuty event.",
                e.getMessage());
            throw e;
        }
    }
}
