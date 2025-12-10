package com.example.kickkick

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FootballApi {
    // EPL 경기 정보 가져오기
    @GET("competitions/PL/matches")
    fun getMatches(
        @Header("X-Auth-Token") apiKey: String,
        @Query("status") status: String
    ): Call<MatchResponse>

    @GET("competitions/PL/standings")
    fun getStandings(@Header("X-Auth-Token") apiKey: String): Call<StandingsResponse>
}