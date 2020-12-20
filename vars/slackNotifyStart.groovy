#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( config, steps = null ) {
    // println( "SlackNotifier.instance.notifyStart" )
    SlackNotifier.instance.initialize( config )
    return SlackNotifier.instance.notifyStart( steps )
}