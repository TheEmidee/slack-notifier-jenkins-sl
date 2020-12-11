package org.gradiant.jenkins.slack

void notifyMessage( custom_message ) {
  SlackFormatter formatter = new SlackFormatter()
  SlackSender sender = new SlackSender()
  JenkinsStatus status = new JenkinsStatus()

  def blocks = formatter.format custom_message
  
  return sender.send( blocks )
}

void notifyStart() {
  SlackFormatter formatter = new SlackFormatter()
  SlackSender sender = new SlackSender()
  JenkinsStatus status = new JenkinsStatus()

  def blocks = formatter.format 'Build started...'

  return sender.sendBlocks( blocks )
}


void notifyError(Throwable err) {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()

  def message = formatter.format ":interrobang: An error occurred "

  if ( env.CURRENT_STEP != null ) {
    message += "\nwhile executing ${env.CURRENT_STEP}"
  }

  message += "\nError: `${err}`"

  return sender.sendMessage message
}

boolean shouldNotNotifySuccess(statusMessage) {
  Config config = new Config()
  return statusMessage == 'Success' && !config.getNotifySuccess()
}

void notifyResult() {
  JenkinsHelper helper = new JenkinsHelper()
  JenkinsStatus status = new JenkinsStatus()
  SlackFormatter formatter = new SlackFormatter()
  SlackSender sender = new SlackSender()
  Config config = new Config()

  def statusMessage = status.getStatusMessage()

  if(shouldNotNotifySuccess(statusMessage)) {
    println("SlackNotifier - No notification will be send for SUCCESS result")
    return
  }

  def duration = helper.getDuration()

  String changes = null
  if(config.getChangeList()) changes = helper.getChanges().join '\n'

  String testSummary = null
  if (config.getTestSummary()) {
    JenkinsTestsSummary jenkinsTestsSummary = new JenkinsTestsSummary()
    testSummary = jenkinsTestsSummary.getTestSummary()
  }

  def message = formatter.formatResult "${statusMessage} after ${duration}", changes, testSummary

  return sender.sendMessage message
}

void notifyResultFull() {
  env.TEST_SUMMARY = true
  env.CHANGE_LIST = true
  env.NOTIFY_SUCCESS = true
  return notifyResult()
}