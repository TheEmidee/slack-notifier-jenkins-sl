package org.gradiant.jenkins.slack

boolean isBackToNormal() {
  def current = currentBuild.currentResult
  def previous = currentBuild.previousBuild?.currentResult

  return current == 'SUCCESS' && (previous == 'FAILURE' || previous == 'UNSTABLE')
}

boolean stillFailing() {
  def current = currentBuild.currentResult
  def previous = currentBuild.previousBuild?.currentResult

  return current == 'FAILURE' && previous == 'UNSTABLE'
}

boolean hasFailed() {
  return currentBuild.currentResult == 'FAILURE'
}

boolean isUnstable() {
  return currentBuild.currentResult == 'UNSTABLE'
}

boolean hasBeenSuccessful() {
  return currentBuild.currentResult == 'SUCCESS'
}

String getStatusMessage() {
  if (isBackToNormal()) {
    return ':party_parrot: *Back to normal* :party_parrot:'
  }

  if (stillFailing()) {
    return ':finnadie: *Still failing* :finnadie:'
  }

  if (hasFailed()) {
    return ':x: *Failure* :x:'
  }

  if (hasBeenSuccessful()) {
    return ':tada: *Success* :tada:'
  }

  if (isUnstable()) {
    return ':heavy_multiplication_x: *Unstable* :heavy_multiplication_x:'
  }

  return ''
}

String getStatusColor() {
  def result = currentBuild.currentResult
  def colors = new Color()

  if (result == 'SUCCESS') {
    return colors.green()
  }

  if (result == 'FAILURE') {
    return colors.red()
  }

  return colors.yellow()
}

String getDirectMessage() {
  def helper = new JenkinsHelper()
  //def config = new Config()

  def full_branch_name = helper.getFullBranchName()
  def job_url = helper.getAbsoluteUrl()
  def mrkdwn = "<${job_url}|${full_branch_name}>"

  if (isBackToNormal()) {
    return "Yay ! The Jenkins job ${mrkdwn} is now back to normal ! :star-struck:"
  }

  if (stillFailing()) {
    return "I'm really sorry but the Jenkins job ${mrkdwn} is still failing :scream:"
  }

  if (hasFailed()) {
    return "Psssst ! Sorry to disturb you...\nI'm just letting you know that the Jenkins job ${mrkdwn} has failed :cry:\n(Don't worry, that stays between us :shushing_face:)"
  }

  if (hasBeenSuccessful() /*&& config.getNotifyUsersWithDirectMessageOnSuccess()*/ ) {
    return "Good job buddy !\nThe Jenkins job ${mrkdwn} has been successful ! :sunglasses:"
  }

  if (isUnstable()) {
    return "Psssst ! Sorry to disturb you.\nI'm just letting you know that the Jenkins job ${mrkdwn} is unstable.\nI thought you might want to have a look :wink:"
  }

  return ''
}