package org.gradiant.jenkins.slack

import groovy.time.*

class SlackMessageData {
  public String allStages = ""
  Date previousStageCompletedDate = null
  String currentStage = ""
  String nodeName = ""
  JenkinsStatus status = null
  String errorInfo = ""
}