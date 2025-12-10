package com.example.kickkick

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kickkick.databinding.ItemStandingBinding

class StandingsAdapter(private var tableList: List<TableEntry>) :
    RecyclerView.Adapter<StandingsAdapter.ViewHolder>() {

    var myTeamId: Int = -1

    class ViewHolder(val binding: ItemStandingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStandingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = tableList[position]

        // 1. 데이터 바인딩
        holder.binding.tvRank.text = entry.position.toString()

        // ★ 풀네임 사용 (기존 shortName 삭제)
        holder.binding.tvTeamName.text = entry.team.name

        // ★ 승/무/패/승점 각각 연결
        holder.binding.tvWon.text = entry.won.toString()
        holder.binding.tvDraw.text = entry.draw.toString()
        holder.binding.tvLost.text = entry.lost.toString()
        holder.binding.tvPoints.text = entry.points.toString()

        // 2. 로고 로드 (전역 로더 사용)
        holder.binding.ivTeamLogo.load(entry.team.crestUrl) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }

        // 3. MyTeam 하이라이팅
        if (entry.team.id == myTeamId) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF59D")) // 연한 노란색
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount() = tableList.size

    fun updateData(newList: List<TableEntry>) {
        tableList = newList
        notifyDataSetChanged()
    }
}