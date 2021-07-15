package com.myphuoc.noteapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.myphuoc.noteapp.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoApp;
    private TextView sloganApp;

    private Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initView();
        initAnimation();
    }

    private void initAnimation() {
        topAnim = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.splash_top_animation);
        bottomAnim = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.splash_bottom_animation);

    }

    private void initView() {
        logoApp = findViewById(R.id.imageLogo);
        sloganApp = findViewById(R.id.textSlogan);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int SPLASH_TIMER = 3500;

        // Set Animation
        logoApp.setAnimation(topAnim);
        sloganApp.setAnimation(bottomAnim);

        new Handler().postDelayed(()->{
            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }, SPLASH_TIMER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}