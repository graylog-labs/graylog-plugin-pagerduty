PagerDuty Plugin for Graylog
============================

[![Build Status](https://travis-ci.org/Graylog2/graylog-plugin-pagerduty.svg)](https://travis-ci.org/Graylog2/graylog-plugin-pagerduty)

An alarm callback plugin for integrating [PagerDuty](http://pagerduty.com/) into [Graylog](https://www.graylog.org/).

![Screenshot: Overview](https://s3.amazonaws.com/graylog2public/images/plugin-pagerduty-ac-1.png)

**Required Graylog version:** 1.0 and later

## Installation

[Download the plugin](https://github.com/Graylog2/graylog-plugin-pagerduty/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

## Usage

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

You can build a plugin (JAR) with `mvn package`.

DEB and RPM packages can be build with `mvn jdeb:jdeb` and `mvn rpm:rpm` respectively.

## Plugin Release

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. TravisCI will build the release artifacts and upload to GitHub automatically.
