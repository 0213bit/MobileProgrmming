package com.example.kickkick

import com.google.gson.annotations.SerializedName

// 전체 응답
data class MatchResponse(
    @SerializedName("matches") val matches: List<Match>
)

// 개별 경기 정보
data class Match(
    @SerializedName("id") val id: Int,
    @SerializedName("utcDate") val utcDate: String,
    @SerializedName("status") val status: String, // FINISHED, SCHEDULED 등
    @SerializedName("homeTeam") val homeTeam: Team,
    @SerializedName("awayTeam") val awayTeam: Team,
    @SerializedName("score") val score: Score
)

// 팀 정보
data class Team(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("tla") val shortName: String?, // 3글자 약어 (MUN, CHE 등)
    @SerializedName("crest") val crestUrl: String? // 로고 이미지 주소
)

// 점수 정보
data class Score(
    @SerializedName("fullTime") val fullTime: FullTimeScore
)

data class FullTimeScore(
    @SerializedName("home") val home: Int?,
    @SerializedName("away") val away: Int?
)

data class StandingsResponse(
    @SerializedName("standings") val standings: List<Standing>
)

data class Standing(
    @SerializedName("type") val type: String, // "TOTAL", "HOME", "AWAY"
    @SerializedName("table") val table: List<TableEntry>
)

data class TableEntry(
    @SerializedName("position") val position: Int,
    @SerializedName("team") val team: Team,
    @SerializedName("playedGames") val playedGames: Int,
    @SerializedName("won") val won: Int,
    @SerializedName("draw") val draw: Int,
    @SerializedName("lost") val lost: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("goalsFor") val goalsFor: Int,
    @SerializedName("goalsAgainst") val goalsAgainst: Int,
    @SerializedName("goalDifference") val goalDifference: Int
)