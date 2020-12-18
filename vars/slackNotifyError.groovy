#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call( Throwable err ) {
    SlackNotifier.instance.notifyError( err )
}