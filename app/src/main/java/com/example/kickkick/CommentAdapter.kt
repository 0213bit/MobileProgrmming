package com.example.kickkick

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kickkick.databinding.ItemCommentBinding

class CommentsAdapter(
    private var comments: List<Comment>,
    private val myUid: String,
    private val friendUids: List<String> // 친구들 UID 목록
) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        holder.binding.tvWriterName.text = comment.nickname
        holder.binding.tvCommentText.text = comment.text

        // 로고 로드
        holder.binding.ivWriterLogo.load(comment.myTeamLogo) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }

        // ★ 배경색 구분 로직 (친구 강조)
        if (comment.uid == myUid) {
            // 내가 쓴 글: 약간 노란색
            holder.binding.commentRoot.setBackgroundColor(Color.parseColor("#FFFDE7"))
            holder.binding.tvWriterName.text = "${comment.nickname} (나)"
        } else if (friendUids.contains(comment.uid)) {
            // 친구가 쓴 글: 약간 파란색 (강조!)
            holder.binding.commentRoot.setBackgroundColor(Color.parseColor("#E3F2FD"))
            holder.binding.tvWriterName.text = "${comment.nickname} (친구)"
            holder.binding.tvWriterName.setTextColor(Color.BLUE)
        } else {
            // 남이 쓴 글: 기본 배경
            holder.binding.commentRoot.setBackgroundColor(Color.WHITE)
            holder.binding.tvWriterName.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = comments.size

    fun updateData(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}