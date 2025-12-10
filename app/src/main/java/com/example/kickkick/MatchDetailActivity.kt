package com.example.kickkick

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kickkick.databinding.ActivityMatchDetailBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp

class MatchDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchDetailBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var matchId: String = "" // 경기 고유 ID
    private var friendUids: List<String> = emptyList() // 내 친구 목록

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Intent로 넘겨받은 경기 정보 표시
        matchId = intent.getIntExtra("matchId", 0).toString() // API ID는 Int지만 Firestore ID는 String으로 씁니다
        val homeTeam = intent.getStringExtra("homeTeam") ?: ""
        val awayTeam = intent.getStringExtra("awayTeam") ?: ""
        val score = intent.getStringExtra("score") ?: ""

        binding.tvDetailMatchTitle.text = "$homeTeam vs $awayTeam"
        binding.tvDetailScore.text = score

        // 2. 내 친구 목록 불러오기 (댓글 강조용)
        loadMyFriendList()

        // 3. 좋아요(공감) 기능 설정
        setupLikes()

        // 4. 댓글 전송 버튼
        binding.btnSendComment.setOnClickListener {
            sendComment()
        }
    }

    private fun loadMyFriendList() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).collection("friends").get()
            .addOnSuccessListener { result ->
                friendUids = result.documents.map { it.id }
                // 친구 목록 로드 후 댓글 리스너 시작 (그래야 친구 구분이 가능)
                setupCommentsListener(myUid)
            }
    }

    private fun setupCommentsListener(myUid: String) {
        // Firestore 실시간 업데이트 리스너 (addSnapshotListener)
        db.collection("matches").document(matchId).collection("comments")
            .orderBy("timestamp") // 시간순 정렬
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val commentList = mutableListOf<Comment>()
                for (doc in snapshots!!) {
                    val comment = doc.toObject<Comment>()
                    commentList.add(comment)
                }

                // 어댑터 연결
                val adapter = CommentsAdapter(commentList, myUid, friendUids)
                binding.rvComments.adapter = adapter
                // 최신 댓글이 보이기 위해 스크롤 맨 아래로
                if (commentList.isNotEmpty()) {
                    binding.rvComments.scrollToPosition(commentList.size - 1)
                }
            }
    }

    private fun setupLikes() {
        val myUid = auth.currentUser?.uid ?: return
        val matchRef = db.collection("matches").document(matchId)

        // A. 실시간 좋아요 감지
        matchRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) {
                binding.tvLikeCount.text = "0"
                binding.ivLike.setColorFilter(Color.GRAY)
                return@addSnapshotListener
            }

            // 'likes'라는 필드는 배열(Array)입니다.
            val likes = snapshot.get("likes") as? List<String> ?: emptyList()
            binding.tvLikeCount.text = likes.size.toString()

            if (likes.contains(myUid)) {
                // 내가 좋아요 누름 -> 빨간색
                binding.ivLike.setColorFilter(Color.RED)
                binding.ivLike.tag = "liked"
            } else {
                // 안 누름 -> 회색
                binding.ivLike.setColorFilter(Color.GRAY)
                binding.ivLike.tag = "unliked"
            }
        }

        // B. 좋아요 클릭 이벤트
        binding.ivLike.setOnClickListener {
            val currentTag = binding.ivLike.tag as? String

            if (currentTag == "liked") {
                // 이미 좋아요 -> 취소 (배열에서 제거)
                matchRef.update("likes", FieldValue.arrayRemove(myUid))
            } else {
                // 안 좋아요 -> 추가 (배열에 추가)
                // 문서가 없을 수도 있으니 set(..., merge=true) 사용이 안전
                val data = hashMapOf("likes" to FieldValue.arrayUnion(myUid))
                matchRef.set(data, com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    private fun sendComment() {
        val text = binding.etComment.text.toString()
        if (text.isEmpty()) return

        val myUid = auth.currentUser?.uid ?: return

        // 내 정보(별명, 로고) 가져오기 (SharedPrefs 이용)
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val nickname = sharedPref.getString("myNickname", "익명") ?: "익명"
        val logoUrl = sharedPref.getString("myTeamLogo", null)

        val newComment = Comment(
            uid = myUid,
            nickname = nickname,
            text = text,
            timestamp = Timestamp.now(),
            myTeamLogo = logoUrl
        )

        // Firestore에 저장
        db.collection("matches").document(matchId).collection("comments")
            .add(newComment)
            .addOnSuccessListener {
                binding.etComment.setText("") // 입력창 비우기
            }
            .addOnFailureListener {
                Toast.makeText(this, "댓글 전송 실패", Toast.LENGTH_SHORT).show()
            }
    }
}