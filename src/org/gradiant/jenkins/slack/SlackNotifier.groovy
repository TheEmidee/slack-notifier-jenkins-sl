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

@NonCPS
def myMethod() {
    echo "changeset raw"
  def changeLogSets = currentBuild.rawBuild.changeSets
  for (int i = 0; i < changeLogSets.size(); i++) {
      def entries = changeLogSets[i].items
      for (int j = 0; j < entries.length; j++) {
          def entry = entries[j]
          echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
          def files = new ArrayList(entry.affectedFiles)
          for (int k = 0; k < files.size(); k++) {
              def file = files[k]
              echo "  ${file.editType.name} ${file.path}"
          }
      }
  }
}

void notifyError( slackResponse, Throwable err) {
  def formatter = new SlackFormatter()
  def sender = new SlackSender()

  def blocks = formatter.formatError err
  sender.updateMessage( slackResponse, blocks )

  echo "changeset"
  println(currentBuild.changeSets) 

  myMethod()

  def userIds = slackUserIdsFromCommitters()

  echo "userIds"
  println( userIds )

  def userIdsString = userIds.collect { "<@$it>" }.join(' ')

  echo "userIdsString"
  println( userIdsString )
  //slackResponse.addReaction( "x" )
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