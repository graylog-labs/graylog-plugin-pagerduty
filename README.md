# Graylog PagerDuty alarm callback
[![Build Status](https://travis-ci.org/Graylog2/graylog2-alarmcallback-pagerduty.svg)](https://travis-ci.org/Graylog2/graylog2-alarmcallback-pagerduty)

An alarm callback plugin for integrating [PagerDuty](http://pagerduty.com/) into [Graylog](https://www.graylog.org/).

## Getting started for users

This project is using Maven and requires Java 7 or higher.

* Clone this repository
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively. 
* Copy generated jar file in target directory to your Graylog server plugin directory
* Restart Graylog server
* Create a new PagerDuty Alarm Callback for a stream in the Graylog web interface