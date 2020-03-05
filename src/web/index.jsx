import webpackEntry from 'webpack-entry';

import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import PagerDutyNotificationForm from './PagerDutyNotificationForm';
import PagerDutyNotificationSummary from './PagerDutyNotificationSummary';


PluginStore.register(new PluginManifest({}, {

  eventNotificationTypes: [
    {
      type: 'pagerduty-notification-v1',
      displayName: 'PagerDuty Notification',
      formComponent: PagerDutyNotificationForm,
      summaryComponent: PagerDutyNotificationSummary,
      defaultConfig: {
        routing_key: '',
        custom_incident: true,
        key_prefix: 'Graylog/',
        client_name: 'Graylog',
        client_url: '',
      },
    }
  ],
}));
