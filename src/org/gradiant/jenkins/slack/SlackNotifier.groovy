package org.gradiant.jenkins.slack

@Singleton
class SlackNotifier {
  private slackResponse = null

  public void notifyMessage( custom_message ) {
    def formatter = new SlackFormatter()
    def sender = new SlackSender()

    def blocks = formatter.format custom_message
    
    def result = sender.sendBlocks blocks
    return result
  }

  public void notifyStart() {
    def formatter = new SlackFormatter()
    def sender = new SlackSender()

    def blocks = formatter.format 'Build started...'
    this.slackResponse = sender.sendBlocks blocks

    env.SLACK_ALL_STAGES = ''
  }

  public void notifyError( Throwable err) {
    def formatter = new SlackFormatter()
    def sender = new SlackSender()
    def helper = new JenkinsHelper()

    def blocks = formatter.formatError err
    sender.updateMessage( slackResponse, blocks )

    notifyUsers()
  }

  public void notifySuccess() {
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

  public void notifyStage( String stage_name ) {
    def formatter = new SlackFormatter()
    def sender = new SlackSender()

    if ( env.SLACK_ALL_STAGES != null && env.SLACK_ALL_STAGES != '' ) {
      env.SLACK_ALL_STAGES += " :heavy_check_mark: \n"
    }
    env.SLACK_ALL_STAGES += "* ${stage_name}"

    def blocks = formatter.format env.SLACK_ALL_STAGES
    sender.updateMessage( slackResponse, blocks )
  }

  public void uploadFileToMessage( filePath, String comment = '' ) {
    slackUploadFile( channel: slackResponse.channelId + ":" + slackResponse.ts, filePath: filePath, initialComment: comment )
  }

  public void notifyUsers() {
    def config = new Config()

    if(config.getNotifyUsersWithDirectMessage() == false) {
      println("SlackNotifier - No direct message will be sent to users")
      return
    }

    def status = new JenkinsStatus()
    def helper = new JenkinsHelper()

    def status_message = status.getDirectMessage()
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

  private boolean shouldNotNotifySuccess(statusMessage) {
    def config = new Config()
    return statusMessage == 'Success' && !config.getNotifySuccess()
  }
}