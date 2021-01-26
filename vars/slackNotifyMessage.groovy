#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( String message, slackResponse = null ) {
    // println( "SlackNotifier.instance.notifySuccess" )
    SlackNotifier.instance.notifyMessage( message, slackResponse )
}