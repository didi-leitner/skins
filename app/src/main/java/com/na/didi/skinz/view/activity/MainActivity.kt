package com.na.didi.skinz.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.na.didi.skinz.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


}