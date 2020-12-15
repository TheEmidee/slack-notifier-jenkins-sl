package org.gradiant.jenkins.slack

void notifyMessage( custom_message ) {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()

  def blocks = formatter.format custom_message
  
  def result = sender.sendBlocks blocks
  return result
}

void notifyStart() {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()

  def blocks = formatter.format 'Build started...'
  def result = sender.sendBlocks blocks

  env.SLACK_ALL_STAGES = ''

  return result
}

void notifyError( slackResponse, Throwable err) {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()
  def helper = new JenkinsHelper()

  def blocks = formatter.formatError err
  sender.updateMessage( slackResponse, blocks )

  notifyUsers()
}

boolean shouldNotNotifySuccess(statusMessage) {
  def config = new Config()
  return statusMessage == 'Success' && !config.getNotifySuccess()
}

void notifySuccess( slackResponse ) {
  def helper = new JenkinsHelper()
  def formatter = new SlackFormatter()
  def sender = new SlackSender()
  def status = new JenkinsStatus()

  def statusMessage = status.getStatusMessage()

  if(shouldNotNotifySuccess(statusMessage)) {
    println("SlackNotifier - No notification will be send for SUCCESS result")
    return
  }

  def blocks = formatter.formatSuccess()
  sender.updateMessage( slackResponse, blocks )

  notifyUsers()

  // if ( status.isBackToNormal() ) {
  //   slackResponse.addReaction( "party_parrot" )
  // } else {
  //   slackResponse.addReaction( "heavy_check_mark" )
  // }
}

void notifyStage( slackResponse, String stage_name ) {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()

  if ( env.SLACK_ALL_STAGES != null && env.SLACK_ALL_STAGES != '' ) {
    env.SLACK_ALL_STAGES += " :heavy_check_mark: \n"
  }
  env.SLACK_ALL_STAGES += "* ${stage_name}"

  def blocks = formatter.format env.SLACK_ALL_STAGES
  sender.updateMessage( slackResponse, blocks )
}

void uploadFileToMessage( slackResponse, filePath, String comment = '' ) {
  slackUploadFile( channel: slackResponse.channelId + ":" + slackResponse.ts, filePath: filePath, initialComment: comment )
}

void notifyUsers() {
  def status = new JenkinsStatus()
  def helper = new JenkinsHelper()

  def status_message = status.getStatusMessage()
  def status_color = status.getStatusColor()
  def users_to_notify = helper.getUsersToNotify()

  for (int i = 0; i < users_to_notify.size(); i++) {
    def user_mail = users_to_notify[i]
    println( "Notify slack user : ${user_mail}" )

    def user_id = slackUserIdFromEmail( user_mail )

    if ( user_id != null ) {
      slackSend( channel: "@${user_id}", color: status_color, message: status_message )
    }
  }
}