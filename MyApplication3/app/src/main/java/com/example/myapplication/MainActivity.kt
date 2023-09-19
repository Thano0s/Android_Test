package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.Layout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val ParentLayout: ConstraintSet.Layout = findViewById(R.id.ParentLayout);
        val SearchBar:View = findViewById(R.id.SearchBar)
//        val heightPL = ParentLayout.height;
//        SearchBar.setOnClickListener{
//
//        }
//        println(ParentLayout.height);
        Log.d("Than0s","just check")
//        Log.d("height",SearchBar.layout);
    }

}