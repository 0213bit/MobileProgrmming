package com.example.kickkick

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kickkick.databinding.FragmentResultsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MatchAdapter

    // ★ 원본 데이터를 보관할 리스트 (필터 해제 시 복구용)
    private var allMatches: List<Match> = emptyList()

    // 내 팀 ID 저장
    private var myTeamId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 내 팀 ID 가져오기
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        myTeamId = sharedPref.getInt("myTeamId", -1)

        // 2. 어댑터 설정
        adapter = MatchAdapter(emptyList())
        adapter.myTeamId = myTeamId // 하이라이팅용 ID 전달
        binding.rvMatches.adapter = adapter
        binding.rvMatches.layoutManager = LinearLayoutManager(context)

        // 3. 필터 체크박스 리스너 설정 (클릭 시 동작)
        setupFilterListener()

        // 4. 데이터 불러오기
        loadMatches()
    }

    private fun setupFilterListener() {
        binding.cbFilterMyTeam.setOnCheckedChangeListener { _, isChecked ->
            if (myTeamId == -1) {
                Toast.makeText(context, "MyTeam 설정을 먼저 해주세요!", Toast.LENGTH_SHORT).show()
                binding.cbFilterMyTeam.isChecked = false
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                // [체크 됨] 내 팀이 홈이거나 원정인 경기만 필터링
                val myMatches = allMatches.filter { match ->
                    match.homeTeam.id == myTeamId || match.awayTeam.id == myTeamId
                }

                if (myMatches.isEmpty()) {
                    Toast.makeText(context, "내 팀의 경기 기록이 없습니다.", Toast.LENGTH_SHORT).show()
                }
                adapter.updateData(myMatches)
            } else {
                // [체크 해제] 전체 목록 다시 보여주기
                adapter.updateData(allMatches)
            }
        }
    }

    private fun loadMatches() {
        val apiKey = "1a743eac431241578674eebe837499ed"

        // FINISHED 상태인 경기만 불러옴 (최신순 정렬은 아래에서)
        RetrofitClient.apiService.getMatches(apiKey, "FINISHED").enqueue(object : Callback<MatchResponse> {
            override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                if (response.isSuccessful) {
                    val matches = response.body()?.matches ?: emptyList()

                    if (matches.isNotEmpty()) {
                        // 날짜 내림차순 정렬 (최신이 위로)
                        // ★ 중요: 정렬된 전체 리스트를 변수에 저장해둡니다.
                        allMatches = matches.sortedByDescending { it.utcDate }

                        // 처음엔 전체 목록 표시
                        adapter.updateData(allMatches)
                    }
                } else {
                    Toast.makeText(context, "로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                Toast.makeText(context, "인터넷 연결 확인 필요", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}