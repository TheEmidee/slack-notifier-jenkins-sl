package org.gradiant.jenkins.slack

@Singleton
class SlackNotifier {
  private slackResponse = null
  private Script script = null
  private String allStages = ''
  private config = null
  private SlackSender slackSender = null
  private SlackFormatter slackFormatter = null
  String currentStage = ""

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
    
    this.script.echo "SlackNotifier - Update message with error"
    println("SlackNotifier - Update message with error")
    this.slackSender.updateMessage( slackResponse, blocks )

    this.script.echo "SlackNotifier - Notify users"
    println("SlackNotifier - Notify users")
    notifyUsers()
  }

  public void notifySuccess() {
    if(shouldNotNotifySuccess()) {
      this.script.echo "SlackNotifier - No notification will be send for SUCCESS result"
      println("SlackNotifier - No notification will be send for SUCCESS result")
      return
    }

    def blocks = this.slackFormatter.formatSuccess()
    
    this.script.echo "SlackNotifier - Update message with success"
    println("SlackNotifier - Update message with success")
    this.slackSender.updateMessage( slackResponse, blocks )

    this.script.echo "SlackNotifier - Notify users"
    println("SlackNotifier - Notify users")

    notifyUsers()

    // if ( status.isBackToNormal() ) {
    //   slackResponse.addReaction( "party_parrot" )
    // } else {
    //   slackResponse.addReaction( "heavy_check_mark" )
    // }
  }

  public void notifyStage( String stage_name ) {
    this.currentStage = stage_name

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
      this.script.echo  "Impossible to notify users. You must pass ( this ) to notifyStart so the script can call slackUserIdFromEmail" 
      println( "Impossible to notify users. You must pass ( this ) to notifyStart so the script can call slackUserIdFromEmail" )
    }

    if(this.config.NotifyUsersWithDirectMessage == false) {
      this.script.echo "SlackNotifier - No direct message will be sent to users"
      println("SlackNotifier - No direct message will be sent to users")
      return
    }

    def status = new JenkinsStatus()
    def helper = new JenkinsHelper()
    def status_message = status.getDirectMessage()

    if(this.shouldNotSendDirectMessageOnSuccess()) {
      this.script.echo "SlackNotifier - No direct message will be sent to users when the build is successful"
      println("SlackNotifier - No direct message will be sent to users when the build is successful")
      return
    }

    def status_color = status.getStatusColor()
    def users_to_notify = this.getUsersToNotify()

    this.script.echo  "Notify ${users_to_notify.size()} users" 
    println( "Notify ${users_to_notify.size()} users" )

    for (int i = 0; i < users_to_notify.size(); i++) {
      def user_mail = users_to_notify[i]
      this.script.echo  "Notify slack user : ${user_mail}" 
      println( "Notify slack user : ${user_mail}" )

      def user_id = this.script.slackUserIdFromEmail( user_mail )

      if ( user_id != null ) {
        this.slackSender.sendDirectMessage( "@${user_id}", status_message, status_color )
      } else {
        this.script.echo  "Impossible to get a slack user for ${user_mail}"
        println( "Impossible to get a slack user for ${user_mail}")
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
    
    def authors = helper.getChangesAuthorEmails()

    if ( authors.size() == 0 ) {
        def fallback = this.config.FallbackContactEmail
        authors.add( fallback )
    }

    return authors
}
}