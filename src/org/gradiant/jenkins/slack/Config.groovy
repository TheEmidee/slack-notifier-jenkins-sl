package org.gradiant.jenkins.slack

Boolean getNotifySuccess() {
  if(env.NOTIFY_SUCCESS) return new Boolean(env.NOTIFY_SUCCESS)
  return false
}

Boolean getChangeList() {
  if(env.CHANGE_LIST) return new Boolean(env.CHANGE_LIST)
  return false
}

Boolean getTestSummary() {
  if(env.TEST_SUMMARY) return new Boolean(env.TEST_SUMMARY)
  return false
}

Boolean getNotifyUsersWithDirectMessage() {
  if(env.SLACK_NOTIFY_USERS_WITH_DIRECT_MESSAGE) return new Boolean(env.SLACK_NOTIFY_USERS_WITH_DIRECT_MESSAGE)
  return false
}

Boolean getNotifyUsersWithDirectMessageOnSuccess() {
  if(env.SLACK_NOTIFY_USERS_WITH_DIRECT_MESSAGE_ON_SUCCESS) return new Boolean(env.SLACK_NOTIFY_USERS_WITH_DIRECT_MESSAGE_ON_SUCCESS)
  return false
}