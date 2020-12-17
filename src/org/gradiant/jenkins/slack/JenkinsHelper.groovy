package org.gradiant.jenkins.slack

String getNodeName() {
    String result = env.NODE_NAME
    if ( result == null ) {
        result = "";
    }
    return result
}

String getFallbackEmailUserToNotify() {
    return env.SLACK_FALLBACK_USER_TO_NOTIFY
}

void setFallbackEmailUserToNotify( String user_email ) {
  env.SLACK_FALLBACK_USER_TO_NOTIFY = user_email
}

String getCurrentStep() {
    return env.SLACK_CURRENT_STEP
}

void setCurrentStep( String current_step ) {
  env.SLACK_CURRENT_STEP = current_step
}

String getAuthorName() {
    return env.SLACK_AUTHOR_NAME
}

void setAuthorName( String author_name ) {
  env.SLACK_AUTHOR_NAME = author_name
}

String getAuthorAvatarURL() {
    return env.SLACK_AUTHOR_AVATAR_URL
}

void setAuthorAvatarURL( String author_avatar_url ) {
  env.SLACK_AUTHOR_AVATAR_URL = author_avatar_url
}

String getDescription() {
    if ( currentBuild.description == null ) {
        return "No Description"
    }
    return currentBuild.description
}

String getBranchName() {
    String result = ""

    if (env.BRANCH_NAME != null) {
        result = env.BRANCH_NAME
    }

    if ( result == "" ) {
        result = env.GIT_BRANCH
    }
    
    return result
}

String getFullBranchName() {
    String result = ""

    if ( env.PULL_REQUEST_TITLE != null ) {
        result = env.PULL_REQUEST_TITLE + " | "
    }

    result += getBranchName()

    return result
}

boolean isMultibranch() {
    return getBranchName() != null
}

int getBuildNumber() {
    return currentBuild.number
}

String getAbsoluteUrl() {
    return currentBuild.absoluteUrl
}

String getConsoleLogsUrl() {
    return getAbsoluteUrl() + "consoleFull"
}

String getTestsResultUrl() {
    return getAbsoluteUrl() + "testReport/"
}

String getArtifactsUrl() {
    return getAbsoluteUrl() + "artifact/"
}

String getLastBuildUrl() {
    def result = getAbsoluteUrl()
    def build_number_str = "${getBuildNumber()}/"

    result = result.substring( 0, result.length() - build_number_str.length() )

    result = result + "lastBuild"

    return result
}

String getRebuildUrl() {
    def result = getLastBuildUrl()

    return "${result}/rebuild"
}

String getGitHubPRNumber() {
    def result = ""

    def branch_name = getBranchName()

    if (branch_name != null && branch_name.startsWith( "PR-" ) ) {
        result = branch_name.substring( 3 )
    }

    return result
}

String getGitHubPRUrl() {
    return "https://github.com/FishingCactus/${getProjectName()}/pull/${getGitHubPRNumber()}"
}

String getProjectName() {
    if(isMultibranch()) return getMultiBranchProjectName()

    return env.JOB_NAME
}

String getMultiBranchProjectName() {
    String[] entries = env.JOB_NAME.split('/')
    def len = entries.length

    if (len == 1 || len == 2) return entries[0]

    entries = entries.take(len - 1)
    return entries.join('/')
}

List<String> getChanges() {
    List<String> messages = []
    for (int i = 0; i < currentBuild.changeSets.size(); i++) {
        def entries = currentBuild.changeSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            messages.add("\t- ${entry.msg} [${entry.author}]")
        }
    }

    return messages
}

@NonCPS
List<String> getChangesAuthorEmails() {
    List<String> authors = []
    for (int i = 0; i < currentBuild.changeSets.size(); i++) {
        def entries = currentBuild.changeSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def author = entry.author.toString()
            def email = "${author}@${env.SLACK_MAIL_DOMAIN}"
            if ( !authors.contains( email ) ) {
                authors.add( email )
            }
        }
    }

    return authors
}

String getDuration() {
    return currentBuild.durationString.replace(' and counting', '')
}

String getCurrentStatus() {
    return currentBuild.currentResult
}

String getPreviousStatus() {
    def prev = currentBuild.previousBuild?.currentResult

    if (!prev) {
        return 'SUCCESS'
    }

    return prev
}

List<String> getUsersToNotify() {
    def authors = getChangesAuthorEmails()

    if ( authors.size() == 0 ) {
        def fallback = getFallbackEmailUserToNotify()
        authors.add( fallback )
    }

    return authors
}