#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( Throwable err, slackResponse = null ) {
    SlackNotifier.instance.notifyError( err, slackResponse )
}