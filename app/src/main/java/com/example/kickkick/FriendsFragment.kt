package com.example.kickkick

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kickkick.databinding.FragmentFriendsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }

    private lateinit var adapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 내 UID 표시 및 복사 기능
        val myUid = auth.currentUser?.uid
        if (myUid != null) {
            binding.tvMyUid.text = myUid
            binding.tvMyUid.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("Kickkick Code", myUid)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
            }
        }
        // 2. 리사이클러뷰 설정
        adapter = FriendsAdapter(emptyList())
        binding.rvFriends.adapter = adapter
        binding.rvFriends.layoutManager = LinearLayoutManager(context)

        // 3. 친구 목록 불러오기
        if (myUid != null) {
            loadFriendsList(myUid)
        }

        // 4. 친구 추가 버튼
        binding.btnAddFriend.setOnClickListener {
            val friendCode = binding.etFriendCode.text.toString().trim()
            if (myUid != null && friendCode.isNotEmpty()) {
                if (myUid == friendCode) {
                    Toast.makeText(context, "자기 자신은 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    addFriend(myUid, friendCode)
                }
            }
        }
    }

    private fun addFriend(myUid: String, friendUid: String) {
        // 친구 ID가 실제로 존재하는지 먼저 확인
        db.collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // ★ 양방향 등록 시작 (Batch 사용)
                    val batch = db.batch()

                    // 내 친구 목록에 상대방 추가 (빈 데이터라도 문서를 만듭니다)
                    val myFriendRef = db.collection("users").document(myUid)
                        .collection("friends").document(friendUid)
                    batch.set(myFriendRef, hashMapOf("createdAt" to FieldValue.serverTimestamp()))

                    // 상대방 친구 목록에 나 추가
                    val friendRef = db.collection("users").document(friendUid)
                        .collection("friends").document(myUid)
                    batch.set(friendRef, hashMapOf("createdAt" to FieldValue.serverTimestamp()))

                    // 실행
                    batch.commit().addOnSuccessListener {
                        Toast.makeText(context, "친구 등록 완료! 서로 연결되었습니다.", Toast.LENGTH_SHORT).show()
                        binding.etFriendCode.setText("")
                        loadFriendsList(myUid) // 목록 새로고침
                    }.addOnFailureListener {
                        Toast.makeText(context, "친구 등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "존재하지 않는 코드입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "오류 발생: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFriendsList(myUid: String) {
        // 내 'friends' 서브 컬렉션 조회
        db.collection("users").document(myUid).collection("friends")
            .get()
            .addOnSuccessListener { result ->
                val friendIds = result.documents.map { it.id }

                if (friendIds.isEmpty()) {
                    adapter.updateData(emptyList())
                    return@addOnSuccessListener
                }

                // 친구들의 상세 정보(별명, 팀)를 'users' 컬렉션에서 가져옴
                val friendList = mutableListOf<Friend>()
                var loadCount = 0

                for (fid in friendIds) {
                    db.collection("users").document(fid).get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val nickname = doc.getString("nickname") ?: "알 수 없음"
                            val teamName = doc.getString("myTeamName") ?: "-"
                            val logoUrl = doc.getString("myTeamLogoUrl") // 여기도 저장했었는지 확인 필요

                            friendList.add(Friend(fid, nickname, teamName, logoUrl))
                        }

                        loadCount++
                        // 모든 친구 정보를 다 가져왔으면 UI 업데이트
                        if (loadCount == friendIds.size) {
                            adapter.updateData(friendList)
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}