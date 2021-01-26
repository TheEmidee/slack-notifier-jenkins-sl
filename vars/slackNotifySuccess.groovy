#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call(slackResponse = null) {
    println( "SlackNotifier.instance.notifySuccess" )
    SlackNotifier.instance.notifySuccess(slackResponse)
}