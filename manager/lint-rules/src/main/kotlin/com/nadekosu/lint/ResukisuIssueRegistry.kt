package com.nadekosu.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

class NadekosuIssueRegistry : IssueRegistry() {
    override val issues = listOf(SegmentedColumnScopeConditionDetector.ISSUE)
    override val api = CURRENT_API
}
