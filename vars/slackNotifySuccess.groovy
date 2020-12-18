#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call() {
    SlackNotifier.instance.notifySuccess()
}