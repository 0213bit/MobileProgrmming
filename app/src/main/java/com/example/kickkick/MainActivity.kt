package com.example.kickkick

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.kickkick.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. ViewBinding 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. NavController 가져오기
        // FragmentContainerView를 사용할 때는 supportFragmentManager를 통해 찾아야 안전합니다.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 3. BottomNavigationView와 NavController 연결
        // 이 코드가 있으면 탭 클릭 시 자동으로 화면이 전환됩니다.
        binding.bottomNav.setupWithNavController(navController)
    }
}