#!/usr/bin/groovy

import org.gradiant.jenkins.slack

def call( Throwable err ) {
    def notifier = new org.gradiant.jenkins.slack.SlackNotifier()
    notifier.notifyError( slack_response, err )
}