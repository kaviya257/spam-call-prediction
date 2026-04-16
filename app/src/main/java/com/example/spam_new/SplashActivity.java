package com.example.spam_new;

// SplashActivity.java

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new android.os.Handler().postDelayed(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, 2000);
    }
}
