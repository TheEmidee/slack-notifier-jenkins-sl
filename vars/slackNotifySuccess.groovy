#!/usr/bin/groovy

import org.gradiant.jenkins.slack.SlackNotifier

def call() {
    // println( "SlackNotifier.instance.notifySuccess" )
    SlackNotifier.instance.notifySuccess()
}