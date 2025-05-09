package com.example.finalproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.post.RecentPostsActivity;

public class DashboardActivity extends AppCompatActivity {

    private Button homeButton, addVideoButton, profileButton, messagesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Khởi tạo các nút
        homeButton = findViewById(R.id.homeButton);
        addVideoButton = findViewById(R.id.addVideoButton);
        profileButton = findViewById(R.id.profileButton);
        messagesButton = findViewById(R.id.messagesButton);

        // Gắn sự kiện click cho các nút
        homeButton.setOnClickListener(v -> navigateToHome());
        addVideoButton.setOnClickListener(v -> navigateToAddVideo());
        profileButton.setOnClickListener(v -> navigateToProfile());
        messagesButton.setOnClickListener(v -> navigateToMessages());
    }

    // Chuyển đến trang chủ (Home)
    private void navigateToHome() {
        Intent intent = new Intent(DashboardActivity.this, RecentPostsActivity.class); // Giả sử bạn có một Activity cho Trang Chủ
        startActivity(intent);
    }

    // Chuyển đến màn hình thêm video
    private void navigateToAddVideo() {
        Intent intent = new Intent(DashboardActivity.this, PhotosActivity.class); // Giả sử bạn có một Activity cho Thêm Video
        startActivity(intent);
    }

    // Chuyển đến màn hình hồ sơ (Profile)
    private void navigateToProfile() {
        Intent intent = new Intent(DashboardActivity.this, UserProfileActivity.class); // Giả sử bạn có một Activity cho Hồ Sơ
        startActivity(intent);
    }

    // Chuyển đến màn hình hộp thư (Messages)
    private void navigateToMessages() {
        Intent intent = new Intent(DashboardActivity.this, MessagesActivity.class); // Giả sử bạn có một Activity cho Hộp Thư
        startActivity(intent);
    }
}
