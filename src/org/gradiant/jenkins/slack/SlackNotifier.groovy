package org.gradiant.jenkins.slack

@Singleton
class SlackNotifier {
  private slackResponse = null
  private steps = null
  private String allStages = ''
  private config = null

  public void initialize( config ) {
    this.config = config
  }

  public void notifyMessage( String custom_message ) {
    def formatter = new SlackFormatter()
    def sender = new SlackSender()

    def blocks = formatter.format custom_message
    def result = sender.sendBlocks blocks
    return result
  }

  public void notifyStart( steps = null ) {
    this.steps = steps

    def formatter = new SlackFormatter()
    def sender = new SlackSender()

    def blocks = formatter.format 'Build started...'
    this.slackResponse = sender.sendBlocks blocks

    this.allStages = ''

    return this.slackResponse
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

    if ( this.allStages != null && this.allStages != '' ) {
      this.allStages += " :heavy_check_mark: \n"
    }
    this.allStages += "* ${stage_name}"

    def blocks = formatter.format this.allStages
    sender.updateMessage( slackResponse, blocks )
  }

  public void uploadFileToMessage( filePath, String comment = '' ) {
    slackUploadFile( channel: slackResponse.channelId + ":" + slackResponse.ts, filePath: filePath, initialComment: comment )
  }

  public void notifyUsers() {
    if ( this.steps == null ) {
      println( "Impossible to notify users. You must pass ( this ) to notifyStart so the script can call slackUserIdFromEmail" )
    }

    def config = new Config()

    if(config.getNotifyUsersWithDirectMessage() == false) {
      println("SlackNotifier - No direct message will be sent to users")
      return
    }

    def status = new JenkinsStatus()
    def helper = new JenkinsHelper()
    def sender = new SlackSender()

    def status_message = status.getDirectMessage()
    def status_color = status.getStatusColor()
    def users_to_notify = helper.getUsersToNotify()

    for (int i = 0; i < users_to_notify.size(); i++) {
      def user_mail = users_to_notify[i]
      println( "Notify slack user : ${user_mail}" )

      def user_id = this.steps.slackUserIdFromEmail( user_mail )

      if ( user_id != null ) {
        sender.sendDirectMessage( "@${user_id}", status_message, status_color )
      }
    }
  }

  private boolean shouldNotNotifySuccess(statusMessage) {
    def config = new Config()
    return statusMessage == 'Success' && !config.getNotifySuccess()
  }
}