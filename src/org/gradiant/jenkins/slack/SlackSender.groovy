package org.gradiant.jenkins.slack

class SlackSender {

  private config = null

  public SlackSender( config ) {
    this.config = config
  }

  public void sendBlocks( blocks ) {
    return this.send( this.config.SlackChannel, blocks )
  }

  public void updateMessage( slackResponse, blocks ) {
    this.send( slackResponse.channelId, blocks, slackResponse.ts )
  }

  public void sendDirectMessage( String user_id, String message, String color ) {
    slackSend( channel: user_id, color: color, message: message )
  }

  private void send( channel_id, blocks, timestamp = null ) {
    def options = [
      channel: channel_id, 
      teamDomain: this.config.SlackDomain, 
      tokenCredentialId: this.config.SlackCredentials, 
      blocks: blocks, 
    ]

    if ( timestamp != null ) {
      options.timestamp = timestamp
    }

    def response = slackSend options
    return response
  }
}