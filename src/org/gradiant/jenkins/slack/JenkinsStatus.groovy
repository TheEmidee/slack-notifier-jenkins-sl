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