#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( String stage_name ) {
    return SlackNotifier.instance.notifyStage( stage_name )
}