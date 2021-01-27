#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( String message ) {
    // println( "SlackNotifier.instance.notifySuccess" )
    SlackNotifier.instance.notifyMessage( message )
}