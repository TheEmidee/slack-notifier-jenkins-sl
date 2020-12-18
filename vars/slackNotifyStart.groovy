#!/usr/bin/groovy

import org.gradiant.jenkins.slack.Notifier

def call() {
    return SlackNotifier.instance.notifyStart
}