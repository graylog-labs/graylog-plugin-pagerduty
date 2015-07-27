# Graylog PagerDuty alarm callback

An alarm callback plugin for integrating [PagerDuty](http://pagerduty.com/) into [Graylog](https://www.graylog.org/).

![Screenshot: Overview](https://s3.amazonaws.com/graylog2public/images/plugin-pagerduty-ac-1.png)

[![Build Status](https://travis-ci.org/Graylog2/graylog-plugin-alarmcallback-pagerduty.svg)](https://travis-ci.org/Graylog2/graylog-plugin-alarmcallback-pagerduty)

## Instructions

#### Step 1: Installing the plugin

Copy the `.jar` file that you received to your Graylog plugin directory which is configured in your `graylog.conf` configuration file using the `plugin_dir` variable. Restart your `graylog-server` process to load the plugin.

Note that you should do this for every `graylog-server` instance you are running.

#### Step 2: Configuring the plugin

The only thing you need to do in your PagerDuty interface is to add a new service called *Graylog*. Click *Services* in the main menu and then hit the *Add new service* button.

![Screenshot: Adding a new service in PagerDuty](https://s3.amazonaws.com/graylog2public/images/plugin-pagerduty-ac-2.png)

Give the new service any name you’d like, for example *graylog* and select an escalation policy. Select *Graylog* from the *Integration type* dropdown box and click *Add Service*.

On the next page you will see the *Service API Key* that Graylog needs to notify PagerDuty about alerts. Copy it.

![Screenshot: Copying the Service Key from PagerDuty](https://s3.amazonaws.com/graylog2public/images/plugin-pagerduty-ac-3.png)

The last thing to do is to copy that token into the alarm callback configuration in Graylog.

![Screenshot: Configuring the PagerDuty alarm callback in Graylog](https://s3.amazonaws.com/graylog2public/images/plugin-pagerduty-ac-4.png)

Click *Add alert destination* and you are done. Your PagerDuty account will now receive alerts of this stream.

## Build

This project is using Maven and requires Java 7 or higher.

* Clone this repository
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively. 
* Copy generated jar file in target directory to your Graylog server plugin directory
* Restart Graylog server
* Create a new PagerDuty Alarm Callback for a stream in the Graylog web interface
