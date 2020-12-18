#!/usr/bin/groovy

import org.gradiant.jenkins.slack.Notifier

def call() {
    SlackNotifier.instance.notifySuccess()
}