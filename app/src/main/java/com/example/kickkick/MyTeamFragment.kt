package com.example.kickkick

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.example.kickkick.databinding.FragmentMyTeamBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTeamFragment : Fragment() {

    private var _binding: FragmentMyTeamBinding? = null
    private val binding get() = _binding!!

    // API에서 가져온 팀 정보를 저장해둘 리스트
    private var teamList: List<TableEntry> = emptyList()

    // Firebase 인스턴스 (초기화는 onCreate에서)
    private lateinit var db: FirebaseFirestore
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FirebaseApp이 초기화된 이후에 Firestore 사용
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Firebase 익명 로그인 (이미 로그인되어 있으면 건너뜀)
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { Log.d("Auth", "로그인 성공 UID: ${it.user?.uid}") }
                .addOnFailureListener { Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show() }
        }

        // 2. 저장된 데이터가 있으면 먼저 보여주기
        loadSavedData()

        // 3. 스피너에 팀 목록 채우기 (API 호출)
        loadTeamsForSpinner()

        // 4. 저장 버튼 클릭 리스너
        binding.btnSave.setOnClickListener {
            saveMyTeam()
        }
    }

    private fun loadTeamsForSpinner() {
        val apiKey = "1a743eac431241578674eebe837499ed" // ★ 본인의 API 키 입력 필수

        RetrofitClient.apiService.getStandings(apiKey).enqueue(object : Callback<StandingsResponse> {
            override fun onResponse(call: Call<StandingsResponse>, response: Response<StandingsResponse>) {
                if (response.isSuccessful) {
                    // 전체 순위(TOTAL) 데이터만 가져옴
                    val totalStanding = response.body()?.standings?.find { it.type == "TOTAL" }
                    teamList = totalStanding?.table ?: emptyList()

                    // 스피너에는 팀 이름만 보여줌
                    val teamNames = teamList.map { it.team.name }
                    val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, teamNames)
                    binding.spinnerTeams.adapter = spinnerAdapter
                }
            }
            override fun onFailure(call: Call<StandingsResponse>, t: Throwable) {
                Toast.makeText(context, "팀 목록 로드 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveMyTeam() {
        val user = auth.currentUser
        val nickname = binding.etNickname.text.toString()
        val selectedPosition = binding.spinnerTeams.selectedItemPosition

        if (user == null) {
            Toast.makeText(context, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (nickname.isEmpty()) {
            Toast.makeText(context, "별명을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (teamList.isEmpty()) {
            Toast.makeText(context, "팀 목록을 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 선택된 팀 정보 가져오기 (API 데이터인 TableEntry에는 순위, 승무패가 다 들어있습니다)
        val selectedEntry = teamList[selectedPosition]
        val teamData = selectedEntry.team

        // A. Firestore 저장 (서버에는 주요 정보만 저장)
        val userData = hashMapOf(
            "uid" to user.uid,
            "nickname" to nickname,
            "myTeamId" to teamData.id,
            "myTeamName" to teamData.name,
            "myTeamLogo" to teamData.crestUrl
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "저장 완료!", Toast.LENGTH_SHORT).show()

                // B. SharedPreferences 저장 (★ 여기에 상세 스탯 추가!)
                val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("myTeamId", teamData.id)
                    putString("myNickname", nickname)
                    putString("myTeamName", teamData.name)
                    putString("myTeamLogo", teamData.crestUrl)

                    // ★ 추가된 부분: 순위와 성적 저장
                    putInt("myTeamRank", selectedEntry.position)
                    putInt("myTeamWon", selectedEntry.won)
                    putInt("myTeamDraw", selectedEntry.draw)
                    putInt("myTeamLost", selectedEntry.lost)

                    apply()
                }

                // C. 화면 갱신 (데이터 전달)
                updateMyTeamUI(
                    teamData.name,
                    nickname,
                    teamData.crestUrl,
                    selectedEntry.position,
                    selectedEntry.won,
                    selectedEntry.draw,
                    selectedEntry.lost
                )
            }
            .addOnFailureListener {
                Toast.makeText(context, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSavedData() {
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val savedTeamName = sharedPref.getString("myTeamName", null)
        val savedNickname = sharedPref.getString("myNickname", null)
        val savedLogoUrl = sharedPref.getString("myTeamLogo", null)

        // ★ 추가된 부분: 저장된 스탯 불러오기 (기본값 0)
        val savedRank = sharedPref.getInt("myTeamRank", 0)
        val savedWon = sharedPref.getInt("myTeamWon", 0)
        val savedDraw = sharedPref.getInt("myTeamDraw", 0)
        val savedLost = sharedPref.getInt("myTeamLost", 0)

        if (savedTeamName != null && savedNickname != null) {
            updateMyTeamUI(
                savedTeamName,
                savedNickname,
                savedLogoUrl,
                savedRank,
                savedWon,
                savedDraw,
                savedLost
            )
        }
    }

    // 인자에 rank, won, draw, lost 추가
    private fun updateMyTeamUI(
        teamName: String,
        nickname: String,
        logoUrl: String?,
        rank: Int,
        won: Int,
        draw: Int,
        lost: Int
    ) {
        binding.cardMyTeam.visibility = View.VISIBLE
        binding.tvMyTeamName.text = teamName
        binding.tvMyNickname.text = "별명: $nickname"

        // ★ 실제 데이터로 텍스트 설정 (예: "4위 | 15승 5무 3패")
        binding.tvTeamStats.text = "${rank}위 | ${won}승 ${draw}무 ${lost}패"

        // 로고 이미지 로드 (전역 설정 사용)
        binding.ivTeamLogo.load(logoUrl) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
