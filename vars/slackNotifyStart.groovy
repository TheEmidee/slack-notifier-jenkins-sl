#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call() {
    println( "SlackNotifier.instance.notifyStart" )
    return SlackNotifier.instance.notifyStart
}