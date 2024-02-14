package com.example.applicationquestionnairesgmu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button button_anketa = (Button)findViewById(R.id.btn_anketa);
        Button button_exit = (Button)findViewById(R.id.btn_exit);
        Animation animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        button_anketa.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animAlpha);

                Intent intent = new Intent(MenuActivity.this, QuestionnaireActivity.class);
                startActivity(intent);
            }
        });

        button_exit.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                // выход из аккаунта
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MenuActivity.this, EmailPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


    }
}