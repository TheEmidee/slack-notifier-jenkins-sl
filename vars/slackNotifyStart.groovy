#!/usr/bin/groovy

import org.gradiant.jenkins.slack

def call() {
    def notifier = new org.gradiant.jenkins.slack.SlackNotifier()
    def slack_response = notifier.notifyStart()
    return slack_response
}