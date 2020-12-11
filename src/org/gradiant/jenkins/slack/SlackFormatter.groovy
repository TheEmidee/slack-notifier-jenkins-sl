package org.gradiant.jenkins.slack


String format(String title = '') {
    def helper = new JenkinsHelper()

    def project = helper.getProjectName()
    def branchName = helper.getFullBranchName()
    def buildNumber = helper.getBuildNumber()
    def url = helper.getAbsoluteUrl()
    def nodeName = helper.getNodeName()

    blocks = 
    [
        {
            "type": "header",
            "text": {
                "type": "plain_text",
                "text": project,
                "emoji": true
            }
        },
        {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*Branch name : * ${branchName}"
            }
        },
        {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*Build Number : * #${buildNumber}"
            }
        },
        {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*Node name : * ${nodeName}"
            }
        },
		{
			"type": "divider"
		},
		{
			"type": "section",
			"text": {
				"type": "plain_text",
				"text": title,
				"emoji": true
			}
		},
		{
			"type": "divider"
		},
		{
			"type": "actions",
			"elements": [
				{
					"type": "button",
					"text": {
						"type": "plain_text",
						"text": "Open",
						"emoji": true
					},
					"value": "open_job_page",
					"url": url,
					"action_id": "open_job_page"
				}
			]
		}
    ]

    return blocks

    // def result = "*${project}*\n"

    // if (branchName != null) result = "${result} [${branchName}] "

    // result = result + "[#${buildNumber}] [${nodeName}]"
    // result = result + "\n(<${url}|Open>)"
    // result = result + "\n\n${title.trim()}"

    // return result
}

String formatResult(String title = '', String message = '', String testSummary = '') {
    def helper = new JenkinsHelper()

    def logsUrl = helper.getConsoleLogsUrl()
    def testsUrl = helper.getTestsResultUrl()
    def artifactsUrl = helper.getArtifactsUrl()
    def githubPRUrl = helper.getGitHubPRUrl()
    def rebuildUrl = helper.getRebuildUrl()

    def result = format title

    result = result + "\n\n(<${logsUrl}|ConsoleLog>) - (<${testsUrl}|Test Result>) - (<${artifactsUrl}|Artifacts>) - (<${githubPRUrl}|GitHub PR>) - (<${rebuildUrl}|Rebuild Job>)"
    if (message) result = result + "\n\nChanges:\n\t ${message.trim()}"
    if (testSummary) result = result + "\n ${testSummary}"

    return result
}