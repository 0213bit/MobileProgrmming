package com.example.kickkick

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.example.kickkick.databinding.ItemStandingBinding

class StandingsAdapter(private var tableList: List<TableEntry>) :
    RecyclerView.Adapter<StandingsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStandingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStandingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    var myTeamId: Int = -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = tableList[position]

        holder.binding.tvRank.text = entry.position.toString()
        holder.binding.tvTeamName.text = entry.team.shortName ?: entry.team.name
        holder.binding.tvPlayed.text = entry.playedGames.toString()
        holder.binding.tvPoints.text = entry.points.toString()

        // ★ 로고 로드
        holder.binding.ivTeamLogo.load(entry.team.crestUrl) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }

        // ★ 하이라이팅 로직
        if (entry.team.id == myTeamId) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF59D"))
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