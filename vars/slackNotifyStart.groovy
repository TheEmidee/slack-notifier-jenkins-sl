#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( config, Script script ) {
    // println( "SlackNotifier.instance.notifyStart" )
    SlackNotifier.instance.initialize( config, script )
    return SlackNotifier.instance.notifyStart()
}