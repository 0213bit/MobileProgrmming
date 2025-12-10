package com.example.kickkick

import coil.load
import coil.decode.SvgDecoder
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kickkick.databinding.ItemMatchBinding

class MatchAdapter(private var matches: List<Match>) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    // ★ 핵심: 외부에서 설정할 내 팀 ID (기본값 -1)
    var myTeamId: Int = -1

    class MatchViewHolder(val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]

        // 기본 데이터 연결
        holder.binding.tvHomeTeam.text = match.homeTeam.shortName ?: match.homeTeam.name
        holder.binding.tvAwayTeam.text = match.awayTeam.shortName ?: match.awayTeam.name
        holder.binding.tvStatus.text = match.status.replace("_", " ")

        // ★ 점수 표시 로직 강화
        val homeScore = match.score.fullTime.home
        val awayScore = match.score.fullTime.away
        if (homeScore != null && awayScore != null) {
            holder.binding.tvScore.text = "$homeScore : $awayScore"
            holder.binding.tvScore.setTextColor(Color.BLACK) // 글자색 검정 강제 지정
        } else {
            val time = match.utcDate.substring(11, 16)
            holder.binding.tvScore.text = time
            holder.binding.tvScore.setTextColor(Color.GRAY) // 회색으로 표시
        }

        // ★ Coil로 이미지 로드 (SVG 지원)
        holder.binding.ivHomeLogo.load(match.homeTeam.crestUrl) {
            error(R.mipmap.ic_launcher) // 에러나면 기본 이미지
            placeholder(R.mipmap.ic_launcher) // 로딩 중 기본 이미지
        }
        holder.binding.ivAwayLogo.load(match.awayTeam.crestUrl) {
            error(R.mipmap.ic_launcher)
            placeholder(R.mipmap.ic_launcher)
        }

        // ★★★ 하이라이팅 로직 ★★★
        // 홈팀이나 원정팀 ID가 내 팀 ID와 같으면 노란색 배경
        if (match.homeTeam.id == myTeamId || match.awayTeam.id == myTeamId) {
            holder.binding.rootLayout.setBackgroundColor(Color.parseColor("#FFF59D"))
        } else {
            holder.binding.rootLayout.setBackgroundColor(Color.WHITE)
        }

        // ★ 클릭 이벤트 추가
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, MatchDetailActivity::class.java).apply {
                putExtra("matchId", match.id) // 경기 ID 전달
                putExtra("homeTeam", match.homeTeam.shortName ?: match.homeTeam.name)
                putExtra("awayTeam", match.awayTeam.shortName ?: match.awayTeam.name)

                // 점수 전달
                val scoreText = if (match.score.fullTime.home != null) {
                    "${match.score.fullTime.home} : ${match.score.fullTime.away}"
                } else {
                    "- : -"
                }
                putExtra("score", scoreText)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = matches.size

    fun updateData(newMatches: List<Match>) {
        matches = newMatches
        notifyDataSetChanged()
    }
}