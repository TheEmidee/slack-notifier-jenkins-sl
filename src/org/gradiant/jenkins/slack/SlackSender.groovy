package org.gradiant.jenkins.slack


void send(blocks) {
  return slackSend( channel: /*env.SLACK_CHANNEL*/ "test-jenkinscactus", teamDomain: env.SLACK_DOMAIN, tokenCredentialId: env.SLACK_CREDENTIALS, blocks: blocks )
  //slackSend options
}


def getOptions(String message = '', String color = '') {
  def obj = [
    message: message
  ]

  if (color) {
    obj.color = color
  }

  if (env.SLACK_CHANNEL) {
    obj.channel = env.SLACK_CHANNEL
  }

  if (env.SLACK_DOMAIN) {
    obj.teamDomain = env.SLACK_DOMAIN
  }

  if (env.SLACK_CREDENTIALS) {
    obj.tokenCredentialId = env.SLACK_CREDENTIALS
  }



  return obj
}