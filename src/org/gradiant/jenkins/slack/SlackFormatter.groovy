package org.gradiant.jenkins.slack

String format(String content = '') {
    def helper = new JenkinsHelper()

    def project = helper.getProjectName()
    def branchName = helper.getFullBranchName()
    def buildNumber = helper.getBuildNumber()
    def url = helper.getAbsoluteUrl()
    def nodeName = helper.getNodeName()
    def description = helper.getDescription()

    String infos = "*Branch name : * ${branchName}\n*Build Number : * #${buildNumber}\n*Node name : * ${nodeName}"

    String author_name = helper.getAuthorName()
    if ( author_name != null ) {
        infos += "\n*By : * ${author_name}"
    }

    infos += "\n<${url}|Open>"

    blocks = 
    [
        [
            "type": "header",
            "text": [
                "type": "plain_text",
                "text": project,
                "emoji": true
            ]
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": infos
            ],
            "accessory": [
                "type": "image",
                "image_url": env.SLACK_PROJECT_THUMBNAIL,
                "alt_text": "alt text for image"
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": "```${description}```"
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": content
            ]
        ]
    ]

    return blocks
}

String formatResult( String content_extra_infos = '' ) {
    def helper = new JenkinsHelper()
    def status = new JenkinsStatus()
    def config = new Config()

    def project = helper.getProjectName()
    def branchName = helper.getFullBranchName()
    def buildNumber = helper.getBuildNumber()
    def url = helper.getAbsoluteUrl()
    def nodeName = helper.getNodeName()
    def description = helper.getDescription()

    String infos = "*Branch name : * ${branchName}\n*Build Number : * #${buildNumber}\n*Node name : * ${nodeName}"

    String author_name = helper.getAuthorName()
    if ( author_name != null ) {
        infos += "\n*By : * ${author_name}"
    }

    infos += "\n<${url}|Open>"

    def statusMessage = status.getStatusMessage()
    def duration = helper.getDuration()

    String changes = "*Changes :*\n"
    if(config.getChangeList()) {
        changes += helper.getChanges().join '\n'
    }

    if (config.getTestSummary()) {
        JenkinsTestsSummary jenkinsTestsSummary = new JenkinsTestsSummary()
        changes += "\n\n" + jenkinsTestsSummary.getTestSummary()
    }

    def content = "${statusMessage} after ${duration}"
    content = content + content_extra_infos

    def logsUrl = helper.getConsoleLogsUrl()
    def testsUrl = helper.getTestsResultUrl()
    def artifactsUrl = helper.getArtifactsUrl()
    def githubPRUrl = helper.getGitHubPRUrl()
    def rebuildUrl = helper.getRebuildUrl()

    blocks = 
    [
        [
            "type": "header",
            "text": [
                "type": "plain_text",
                "text": project,
                "emoji": true
            ]
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": infos
            ],
            "accessory": [
                "type": "image",
                "image_url": env.SLACK_PROJECT_THUMBNAIL,
                "alt_text": "alt text for image"
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": "```${description}```"
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": content
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": "[ <${logsUrl}|ConsoleLog> ] - [ <${testsUrl}|Test Result> ] - [ <${artifactsUrl}|Artifacts> ] - [ <${githubPRUrl}|GitHub PR> ] - [ <${rebuildUrl}|Rebuild Job> ]",
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": changes,
            ]
        ]
    ]

    return blocks
}

String formatSuccess() {
    def blocks = formatResult()
    return blocks
}

String formatError( Throwable error ) {
    String extra_infos = ''

    if ( env.CURRENT_STEP != null ) {
        extra_infos += "\nwhile executing ${env.CURRENT_STEP}"
    }

    extra_infos += "\nError: `${error}`"

    def blocks = formatResult extra_infos
    return blocks
}