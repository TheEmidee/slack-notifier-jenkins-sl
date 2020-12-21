package org.gradiant.jenkins.slack

class SlackFormatter {
    
    private config = null
    
    public SlackFormatter( config ) {
        this.config = config
    }

    public String format(String content = '') {
        def helper = new JenkinsHelper()

        def project = helper.getProjectName()
        def branchName = helper.getFullBranchName()
        def buildNumber = helper.getBuildNumber()
        def url = helper.getAbsoluteUrl()
        def nodeName = helper.getNodeName()
        def description = helper.getDescription()

        String infos = "*Branch name : * ${branchName}\n*Build Number : * #${buildNumber}\n*Node name : * ${nodeName}"

        String author_name = this.config.AuthorName
        if ( author_name != null ) {
            infos += "\n*By : * ${author_name}"
        }

        infos += "\n<${url}|Open>"

        def blocks = 
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
                    "image_url": this.config.ProjectThumbnail,
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

        def project = helper.getProjectName()
        def branchName = helper.getFullBranchName()
        def buildNumber = helper.getBuildNumber()
        def url = helper.getAbsoluteUrl()
        def nodeName = helper.getNodeName()
        def description = helper.getDescription()

        String infos = "*Branch name : * ${branchName}\n*Build Number : * #${buildNumber}\n*Node name : * ${nodeName}"

        String author_name = this.config.AuthorName
        if ( author_name != null ) {
            infos += "\n*By : * ${author_name}"
        }

        infos += "\n<${url}|Open>"

        def statusMessage = status.getStatusMessage()
        def duration = helper.getDuration()

        String changes = "*Changes :*\n"
        if(this.config.ShowChangeList) {
            changes += helper.getChanges().join '\n'
        }

        if (this.config.ShowTestSummary) {
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

        def blocks = 
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
                    "image_url": this.config.ProjectThumbnail,
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

    public String formatSuccess() {
        def blocks = formatResult()
        return blocks
    }

    public String formatError( Throwable error ) {
        String extra_infos = ''

        if ( env.SLACK_CURRENT_STEP != null ) {
            extra_infos += "\nwhile executing ${env.SLACK_CURRENT_STEP}"
        }

        extra_infos += "\nError: `${error}`"

        def blocks = formatResult extra_infos
        return blocks
    }
}