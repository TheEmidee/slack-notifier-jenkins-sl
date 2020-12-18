#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call() {
    return SlackNotifier.instance.notifyStart
}