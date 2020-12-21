package org.gradiant.jenkins.slack

@Singleton
class SlackNotifier {
  private slackResponse = null
  private Script script = null
  private String allStages = ''
  private config = null
  private SlackSender slackSender = null
  private SlackFormatter slackFormatter = null

  public void initialize( config, Script script ) {
    this.config = config
    this.script = script
    this.slackSender = new SlackSender( this.config, this.script )
    this.slackFormatter = new SlackFormatter( this.config )
  }

  public void notifyMessage( String custom_message ) {
    def blocks = this.slackFormatter.format custom_message
    def result = this.slackSender.sendBlocks blocks
    return result
  }

  public void notifyStart() {
    def blocks = this.slackFormatter.format 'Build started...'
    this.slackResponse = this.slackSender.sendBlocks blocks

    this.allStages = ''

    return this.slackResponse
  }

  public void notifyError( Throwable err) {
    def blocks = this.slackFormatter.formatError err
    this.slackSender.updateMessage( slackResponse, blocks )

    notifyUsers()
  }

  public void notifySuccess() {
    if(shouldNotNotifySuccess()) {
      println("SlackNotifier - No notification will be send for SUCCESS result")
      return
    }

    def blocks = this.slackFormatter.formatSuccess()
    this.slackSender.updateMessage( slackResponse, blocks )

    notifyUsers()

    // if ( status.isBackToNormal() ) {
    //   slackResponse.addReaction( "party_parrot" )
    // } else {
    //   slackResponse.addReaction( "heavy_check_mark" )
    // }
  }

  public void notifyStage( String stage_name ) {
    if ( this.allStages != null && this.allStages != '' ) {
      this.allStages += " :heavy_check_mark: \n"
    }
    this.allStages += "* ${stage_name}"

    def blocks = this.slackFormatter.format this.allStages
    this.slackSender.updateMessage( slackResponse, blocks )
  }

  public void uploadFileToMessage( filePath, String comment = '' ) {
    slackUploadFile( channel: slackResponse.channelId + ":" + slackResponse.ts, filePath: filePath, initialComment: comment )
  }

  public void notifyUsers() {
    if ( this.script == null ) {
      println( "Impossible to notify users. You must pass ( this ) to notifyStart so the script can call slackUserIdFromEmail" )
    }

    if(this.config.NotifyUsersWithDirectMessage == false) {
      println("SlackNotifier - No direct message will be sent to users")
      return
    }

    def status = new JenkinsStatus()
    def helper = new JenkinsHelper()
    def status_message = status.getDirectMessage()

    if(this.shouldNotSendDirectMessageOnSuccess()) {
      println("SlackNotifier - No direct message will be sent to users when the build is successful")
      return
    }

    def status_color = status.getStatusColor()
    def users_to_notify = this.getUsersToNotify()

    for (int i = 0; i < users_to_notify.size(); i++) {
      def user_mail = users_to_notify[i]
      println( "Notify slack user : ${user_mail}" )

      def user_id = this.script.slackUserIdFromEmail( user_mail )

      if ( user_id != null ) {
        this.slackSender.sendDirectMessage( "@${user_id}", status_message, status_color )
      }
    }
  }

  private boolean shouldNotNotifySuccess() {
    def status = new JenkinsStatus()
    return status.hasBeenSuccessful() && !this.config.NotifySuccess
  }

  private boolean shouldNotSendDirectMessageOnSuccess() {
    def status = new JenkinsStatus()
    return status.hasBeenSuccessful() && !this.config.NotifyUsersWithDirectMessageOnSuccess
  }

  private List<String> getUsersToNotify() {
    def helper = new JenkinsHelper()
    
    def authors = helpder.getChangesAuthorEmails()

    if ( authors.size() == 0 ) {
        def fallback = this.config.FallbackContactEmail
        authors.add( fallback )
    }

    return authors
}
}