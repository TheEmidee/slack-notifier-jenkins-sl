#!/usr/bin/groovy

import org.gradiant.jenkins.slack.Notifier

def call( Throwable err ) {
    SlackNotifier.instance.notifyError( err )
}