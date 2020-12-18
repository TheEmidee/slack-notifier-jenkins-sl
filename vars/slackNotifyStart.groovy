#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( steps = null ) {
    // println( "SlackNotifier.instance.notifyStart" )
    return SlackNotifier.instance.notifyStart( steps )
}