package com.example.kotlin_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity()
{


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this)
    }
    public fun auth(view:View) {
        val email: EditText = findViewById(R.id.User_Name);
        val password: EditText = findViewById(R.id.Pass_World);
        val auth = FirebaseAuth.getInstance();
        Toast.makeText(this.applicationContext,"Hello",Toast.LENGTH_SHORT).show();
        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
        .addOnCompleteListener(this.mainExecutor)
        {
            task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "createUserWithEmail:success")
//                    val user = auth.currentUser
//                    updateUI(user)
                Toast.makeText(this.applicationContext,"Success to log In",Toast.LENGTH_SHORT).show();
            } else {
                // If sign in fails, display a message to the user.
//                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
//                    Toast.makeText(
//                        baseContext,
//                        "Authentication failed.",
//                        Toast.LENGTH_SHORT,
//                    ).show()
                Toast.makeText(this.applicationContext,"Fail to log In",Toast.LENGTH_SHORT).show();
//                    updateUI(null)
            }
            Toast.makeText(this.applicationContext,"I'm In",Toast.LENGTH_SHORT).show();
        }
    }
}