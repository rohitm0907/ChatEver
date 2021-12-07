package com.rohit.chitForChat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rohit.chitForChat.adapters.HomeTabApapter
import com.rohit.chitForChat.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
   lateinit var binding:ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myViewPager.adapter=HomeTabApapter(supportFragmentManager)
        binding.myTablayout.setupWithViewPager(binding.myViewPager)


    }
}