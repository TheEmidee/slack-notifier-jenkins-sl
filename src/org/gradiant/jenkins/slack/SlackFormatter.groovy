package org.gradiant.jenkins.slack


String format(String title = '', String message = '', String testSummary = '') {
    def helper = new JenkinsHelper()

    def project = helper.getProjectName()
    def branchName = helper.getBranchName()
    def buildNumber = helper.getBuildNumber()
    def url = helper.getAbsoluteUrl()

    def result = "*${project}*"

    if (branchName != null) result = "${result} >> `${branchName}`"

    result = "${result} - #${buildNumber} ${title.trim()} (<${url}|Open>)"
    if (message) result = result + "\nChanges:\n\t ${message.trim()}"
    if (testSummary) result = result + "\n ${testSummary}"

    return result
}

String formatResult(String title = '', String message = '', String testSummary = '') {
    def helper = new JenkinsHelper()

    def project = helper.getProjectName()
    def branchName = helper.getBranchName()
    def buildNumber = helper.getBuildNumber()
    def url = helper.getAbsoluteUrl()

    def logsUrl = helper.getConsoleLogsUrl()
    def testsUrl = helper.getTestsResultUrl()
    def artifactsUrl = helper.getArtifactsUrl()

    def result = "*${project}*"

    if (branchName != null) result = "${result} >> `${branchName}`"

    result = "${result} - #${buildNumber} ${title.trim()} (<${url}|Open>)"
    if (message) result = result + "\nChanges:\n\t ${message.trim()}"
    if (testSummary) result = result + "\n ${testSummary}"

    result = result += "\n<${logsUrl}|ConsoleLog>"
    result = result += "\n<${testsUrl}|Test Result>"
    result = result += "\n<${artifactsUrl}|Artifacts>"

    return result
}