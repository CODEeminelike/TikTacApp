package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Set listeners for buttons
        loginButton.setOnClickListener(v -> navigateToLogin());
        registerButton.setOnClickListener(v -> navigateToRegister());
    }

    // Navigate to LoginActivity
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, com.example.finalproject.activites.LoginActivity.class);
        startActivity(intent);
    }

    // Navigate to RegisterActivity
    private void navigateToRegister() {
        Intent intent = new Intent(MainActivity.this, com.example.finalproject.activites.RegisterActivity.class);
        startActivity(intent);
    }
}
