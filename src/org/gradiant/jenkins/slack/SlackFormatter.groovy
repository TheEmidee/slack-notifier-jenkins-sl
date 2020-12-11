package org.gradiant.jenkins.slack


String format(String title = '') {
    def helper = new JenkinsHelper()

    def project = helper.getProjectName()
    def branchName = helper.getFullBranchName()
    def buildNumber = helper.getBuildNumber()
    def url = helper.getAbsoluteUrl()
    def nodeName = helper.getNodeName()

    def result = "*${project}*"

    if (branchName != null) result = "${result} >> `${branchName}`"

    result = result + " on [${nodeName}]"

    result = "${result} - #${buildNumber} ${title.trim()} (<${url}|Open>)"

    return result
}

String formatResult(String title = '', String message = '', String testSummary = '') {
    def helper = new JenkinsHelper()

    def logsUrl = helper.getConsoleLogsUrl()
    def testsUrl = helper.getTestsResultUrl()
    def artifactsUrl = helper.getArtifactsUrl()
    def githubPRUrl = helper.getGitHubPRUrl()
    def rebuildUrl = helper.getRebuildUrl()
    def description = helper.getBuildDescription()

    def result = format title

    result = result + "\n(<${logsUrl}|ConsoleLog>) - (<${testsUrl}|Test Result>) - (<${artifactsUrl}|Artifacts>) - (<${githubPRUrl}|GitHub PR>) - (<${rebuildUrl}|Rebuild Job>)"
    if (message) result = result + "\nChanges:\n\t ${message.trim()}"
    if (testSummary) result = result + "\n ${testSummary}"

    result = result + "\n\n *Description*:\n${description}"

    return result
}