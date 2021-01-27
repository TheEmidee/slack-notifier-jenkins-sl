package org.gradiant.jenkins.slack

class SlackFormatter {
    
    private config = null
    
    public SlackFormatter( config ) {
        this.config = config
    }

    public String formatSimple(String content = '') {
        def description_block = getOptionalDescriptionBlock()

        def content_block = [
            "type": "section",
            "text": 
            [
                "type": "mrkdwn",
                "text": content
            ]
        ]

        def blocks = [
            getHeaderBlock(),
            getProjectInfoBlock(),
            getDividerBlock()
        ]

        if (description_block != null) {
            blocks.add(description_block)
            blocks.add(getDividerBlock())
        }

        blocks.add(content_block)

        return blocks
    }

    public String formatMultipleNodes(data) {
        def blocks = [
            getHeaderBlock(),
            getProjectInfoBlock(false),
            getDividerBlock()
        ]

        def description_block = getOptionalDescriptionBlock()

        if (description_block != null) {
            blocks.add(description_block)
            blocks.add(getDividerBlock())
        }

        def finished = data.every { id, node ->
            node.status != null
        }

        if (finished) {
            blocks.addAll([
                getRelevantLinksBlock(),
                getDividerBlock(),
                getResultChangesBlock()
            ])
        } else {
            data.each { id, node ->
                def content_block = [
                    "type": "section",
                    "text": 
                    [
                        "type": "mrkdwn",
                        "text": node.allStages
                    ]
                ]
                blocks.add(content_block)
                
                blocks.add(getDividerBlock())
            }
        }

        return blocks
    }

    public String formatError( Throwable error, data ) {
        def helper = new JenkinsHelper()
        def node = data[helper.getNodeName()]
        String extra_infos = ''

        String current_stage = node.currentStage

        if ( current_stage != "" ) {
            extra_infos += "\nwhile executing ${current_stage}"
        }

        extra_infos += "\nError: `${error}`"

        node.errorInfo = extra_infos
        data[node.nodeName] = node

        def blocks = formatMultipleNodes data
        return blocks
    }

    private String getStatusMessage() {
        def status = new JenkinsStatus()
        return status.getStatusMessage(config)
    }

    private getHeaderBlock() {
        def helper = new JenkinsHelper()
        def project = helper.getProjectName()
        return [
            "type": "header",
            "text": 
            [
                "type": "plain_text",
                "text": project,
                "emoji": true
            ]
        ]
    }

    private getProjectInfoBlock(boolean include_node_name = true) {
        def helper = new JenkinsHelper()

        def branchName = helper.getBranchName()
        def buildNumber = helper.getBuildNumber()
        def nodeName = helper.getNodeName()
        def url = helper.getAbsoluteUrl()

        String infos = "*Branch name:* ${branchName}\n*Build Number:* #${buildNumber}"

        if ( include_node_name ) {
            infos += "\n*Node name:* ${nodeName}"
        }

        String author_name = this.config.AuthorName
        if ( author_name != null ) {
            infos += "\n*By: * ${author_name}"
        }

        infos += "\n<${url}|Open>"

        def result = 
        [
            "type": "section",
            "text": [
                "type": "mrkdwn",
                "text": infos
            ]
        ]

        if ( this.config.ProjectThumbnail != null ) {
            result = result + [
                "accessory": [
                    "type": "image",
                    "image_url": this.config.ProjectThumbnail,
                    "alt_text": "project thumbnail"
                ]
            ]
        }
        
        return result
    }

    private getDividerBlock() {
        return [
            "type": "divider"
        ]
    }

    private getOptionalDescriptionBlock() {
        def helper = new JenkinsHelper()
        def description = helper.getDescription()
        if (description != null) {
            return [
                "type": "section",
                "text": 
                [
                    "type": "mrkdwn",
                    "text": "```${description}```"
                ]
            ]
        }
        return null
    }

    private getResultContentBlock( SlackMessageData node_data ) {
        def helper = new JenkinsHelper()
        def duration = helper.getDuration()
        def statusMessage = node_data.status.getStatusMessage(config)

        def content = "*Node name: * ${node_data.nodeName}\n"
        content += "${statusMessage} after ${duration}"
        content = content + node_data.errorInfo
        return [
                "type": "section",
                "text": [
                    "type": "mrkdwn",
                    "text": content
                ]
        ]
    }

    private getRelevantLinksBlock() {
        def helper = new JenkinsHelper()
        def logsUrl = helper.getConsoleLogsUrl()
        def githubPRUrl = helper.getGitHubPRUrl()
        def rebuildUrl = helper.getRebuildUrl()

        String relevant_links = "[ <${logsUrl}|Console Log> ]"

        // be permissive. On by default unless false.
        if (this.config.Links.TestResults != false) {
            def testsUrl = helper.getTestsResultUrl()
            relevant_links +=  "- [ <${testsUrl}|Test Result> ]"
        }

        if (this.config.Links.Artifacts != false) {
            def artifactsUrl = helper.getArtifactsUrl()
            relevant_links += " - [ <${artifactsUrl}|Artifacts> ]"
        }

        if (githubPRUrl != null) {
            relevant_links += " - [ <${githubPRUrl}|GitHub PR> ]"
        }

        relevant_links += " - [ <${rebuildUrl}|Rebuild Job> ]"

        return [
            "type": "section",
            "text": 
            [
                "type": "mrkdwn",
                "text": relevant_links
            ]
        ]
    }

    private getResultChangesBlock() {
        def helper = new JenkinsHelper()
        String changes = "*Changes :*\n"
        if(this.config.ShowChangeList) {
            changes += helper.getChanges().join '\n'
        }

        if (this.config.ShowTestSummary == true) {
            JenkinsTestsSummary jenkinsTestsSummary = new JenkinsTestsSummary()
            changes += "\n\n" + jenkinsTestsSummary.getTestSummary()
        }

        return [
            "type": "section",
            "text": 
            [
                "type": "mrkdwn",
                "text": changes,
            ]
        ]
    }
}