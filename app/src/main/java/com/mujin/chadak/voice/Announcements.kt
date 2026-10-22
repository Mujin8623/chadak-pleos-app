package com.mujin.chadak.voice

import com.mujin.chadak.triage.TriageLevel

object Announcements {
    fun prompt(level: TriageLevel): String = when (level) {
        TriageLevel.STOP_NOW ->
            "즉시 정차하세요. 안전한 곳에 세운 뒤 정비소를 경유지로 안내할까요?"
        TriageLevel.SERVICE_THIS_WEEK ->
            "이번 주 안에 정비가 필요합니다. 가까운 정비소를 경유지로 추가할까요?"
        TriageLevel.OBSERVE ->
            "살펴볼 항목이 있습니다. 가까운 정비소를 경유지로 추가할까요?"
    }
}
