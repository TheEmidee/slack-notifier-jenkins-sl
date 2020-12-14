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