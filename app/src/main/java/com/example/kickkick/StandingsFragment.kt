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
import com.example.kickkick.databinding.FragmentStandingsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StandingsFragment : Fragment() {

    private var _binding: FragmentStandingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: StandingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStandingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StandingsAdapter(emptyList())
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val myTeamId = sharedPref.getInt("myTeamId", -1)

        adapter = StandingsAdapter(emptyList())
        adapter.myTeamId = myTeamId // ★ ID 전달
        binding.rvStandings.adapter = adapter
        binding.rvStandings.layoutManager = LinearLayoutManager(context)

        loadStandings()
    }

    private fun loadStandings() {
        val apiKey = "1a743eac431241578674eebe837499ed"

        RetrofitClient.apiService.getStandings(apiKey).enqueue(object : Callback<StandingsResponse> {
            override fun onResponse(call: Call<StandingsResponse>, response: Response<StandingsResponse>) {
                if (response.isSuccessful) {
                    // standings 리스트 중 type이 "TOTAL"인 것만 찾음
                    val totalStanding = response.body()?.standings?.find { it.type == "TOTAL" }
                    val table = totalStanding?.table ?: emptyList()

                    adapter.updateData(table)
                } else {
                    Log.e("StandingsFragment", "Error code: ${response.code()}") // 로그 확인용
                    Toast.makeText(context, "순위 로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StandingsResponse>, t: Throwable) {
                Log.e("StandingsFragment", "Fail: ${t.message}")
                Toast.makeText(context, "인터넷 연결 확인 필요", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}