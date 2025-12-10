package com.example.kickkick

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kickkick.databinding.ItemFriendBinding

class FriendsAdapter(private var friendList: List<Friend>) :
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friendList[position]

        holder.binding.tvFriendNickname.text = friend.nickname
        holder.binding.tvFriendTeamName.text = friend.myTeamName

        // 로고 로딩 (전역 설정 덕분에 간단합니다!)
        holder.binding.ivFriendTeamLogo.load(friend.myTeamLogoUrl) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }
    }

    override fun getItemCount() = friendList.size

    fun updateData(newList: List<Friend>) {
        friendList = newList
        notifyDataSetChanged()
    }
}