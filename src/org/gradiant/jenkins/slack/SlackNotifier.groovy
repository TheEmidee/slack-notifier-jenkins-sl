package org.gradiant.jenkins.slack

import groovy.time.*

@Singleton
class SlackNotifier {
  private slackResponse = null
  private Script script = null
  private config = null
  private SlackSender slackSender = null
  private SlackFormatter slackFormatter = null
  // added this because slackResponse could be async, thus still calling notifyStart more than once.
  private boolean isInitialized = false
  def messageData = [:]

  public void initialize( config, Script script ) {
    this.config = config
    this.script = script
    this.slackSender = new SlackSender( this.config, this.script )
    this.slackFormatter = new SlackFormatter( this.config )
  }

  public void notifyMessage( String custom_message ) {
    def blocks = this.slackFormatter.formatSimple custom_message
    def result = this.slackSender.sendBlocks blocks
    return result
  }

  public void notifyStart() {
    println("Slack Sender: Notify start")
    def helper = new JenkinsHelper()
    def message_data = new SlackMessageData()
    message_data.nodeName = helper.getNodeName()
    messageData.put(message_data.nodeName, message_data)

    if (isInitialized == true) {
      return this.slackResponse
    }
    isInitialized = true    
    def blocks = this.slackFormatter.formatSimple 'Build started...'
    this.slackResponse = this.slackSender.sendBlocks blocks

    return this.slackResponse
  }

  public void notifyError( Throwable err ) {

    def blocks = this.slackFormatter.formatError( err, messageData )
    
    this.script.echo "SlackNotifier - Update message with error"
    println("SlackNotifier - Update message with error")
    
    this.slackSender.updateMessage( this.slackResponse, blocks )

    this.script.echo "SlackNotifier - Notify users"
    println("SlackNotifier - Notify users")
    // :TODO: only notify user when all builds are done?
    notifyUsers()
  }

  public void notifySuccess() {

    if(shouldNotNotifySuccess()) {
      this.script.echo "SlackNotifier - No notification will be send for SUCCESS result"
      println("SlackNotifier - No notification will be send for SUCCESS result")
      return
    }
    def helper = new JenkinsHelper()
    def data = messageData[helper.getNodeName()]
    data.status = new JenkinsStatus()
    messageData[data.nodeName] = data

    def blocks = this.slackFormatter.formatMultipleNodes messageData
    
    this.script.echo "SlackNotifier - Update message with success"
    println("SlackNotifier - Update message with success")
    this.slackSender.updateMessage( this.slackResponse, blocks )

    this.script.echo "SlackNotifier - Notify users"
    println("SlackNotifier - Notify users")

    // :TODO: only notify user when all builds are done?
    notifyUsers()

    // if ( status.isBackToNormal() ) {
    //   slackResponse.addReaction( "party_parrot" )
    // } else {
    //   slackResponse.addReaction( "heavy_check_mark" )
    // }
  }

  public void notifyStage( String stage_name ) {
    def helper = new JenkinsHelper()

    def data = messageData[helper.getNodeName()]
    data.currentStage = stage_name

    if ( data.allStages != null && data.allStages != '' ) {
      def currentStageCompletedDate = new Date()
      TimeDuration duration = TimeCategory.minus(currentStageCompletedDate, data.previousStageCompletedDate)
      data.previousStageCompletedDate = currentStageCompletedDate
      data.allStages += " ($duration) :heavy_check_mark: \n"
    } else {
      data.previousStageCompletedDate = new Date()
      data.allStages += "*Node name: * ${data.nodeName}\n"
    }
    data.allStages += "* ${stage_name}"
    messageData[data.nodeName] = data

    def blocks = this.slackFormatter.formatMultipleNodes messageData
    this.slackSender.updateMessage( this.slackResponse, blocks )
  }

  public void uploadFileToMessage( filePath, String comment = '' ) {
    slackUploadFile( channel: this.slackResponse.channelId + ":" + this.slackResponse.ts, filePath: filePath, initialComment: comment )
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
    def status_message = this.getDirectMessage()

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

  private String getDirectMessage() {
    def helper = new JenkinsHelper()
    def status = new JenkinsStatus()

    def full_branch_name = helper.getBranchName()
    def job_url = helper.getAbsoluteUrl()
    def mrkdwn = "<${job_url}|${full_branch_name}>"

    if (status.isBackToNormal()) {
        return sprintf( this.config.DirectMessages.BackToNormal, mrkdwn )
    }

    if (status.stillFailing()) {
        return sprintf( this.config.DirectMessages.StillFailing, mrkdwn )
    }

    if (status.hasFailed()) {
        return sprintf( this.config.DirectMessages.Failed, mrkdwn )
    }

    if (status.hasBeenSuccessful()) {
        return sprintf( this.config.DirectMessages.Successful, mrkdwn )
    }

    if (status.isUnstable()) {
        return sprintf( this.config.DirectMessages.Unstable, mrkdwn )
    }

    return ''
  }

  private boolean shouldNotNotifySuccess() {
    def status = new JenkinsStatus()
    return status.hasBeenSuccessful() && this.config.NotifySuccess != true
  }

  private boolean shouldNotSendDirectMessageOnSuccess() {
    def status = new JenkinsStatus()
    return status.hasBeenSuccessful() && this.config.NotifyUsersWithDirectMessageOnSuccess != true
  }

  private List<String> getUsersToNotify() {
    def helper = new JenkinsHelper()
    
    def authors = this.getChangesAuthorEmails()

    if ( authors.size() == 0 ) {
        def fallback = this.config.FallbackContactEmail
        authors.add( fallback )
    }

    return authors
  }

  @NonCPS
  private List<String> getChangesAuthorEmails() {
    List<String> authors = []
    for (int i = 0; i < this.script.currentBuild.changeSets.size(); i++) {
        def entries = this.script.currentBuild.changeSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def author = entry.author.toString()
            def email = "${author}@${this.config.MailDomain}"
            if ( !authors.contains( email ) ) {
                authors.add( email )
            }
        }
    }

    return authors
  }
}