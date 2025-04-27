package com.example.finalproject.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;

public class ProfileActivity extends AppCompatActivity {

    private Button personalInfoButton, videoButton, photosButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo các nút
        personalInfoButton = findViewById(R.id.personalInfoButton);
        videoButton = findViewById(R.id.videoButton);
        photosButton = findViewById(R.id.photosButton);

        // Gắn sự kiện click cho các nút
        personalInfoButton.setOnClickListener(v -> navigateToPersonalInfo());
        videoButton.setOnClickListener(v -> navigateToVideo());
        photosButton.setOnClickListener(v -> navigateToPhotos());
    }

    // Chuyển đến màn hình Thông tin cá nhân
    private void navigateToPersonalInfo() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class); // Giả sử bạn có một Activity cho Thông tin cá nhân
        startActivity(intent);
    }

    // Chuyển đến màn hình Video
    private void navigateToVideo() {
        Intent intent = new Intent(ProfileActivity.this, VideoActivity.class); // Giả sử bạn có một Activity cho Video
        startActivity(intent);
    }

    // Chuyển đến màn hình Ảnh
    private void navigateToPhotos() {
        Intent intent = new Intent(ProfileActivity.this, PhotosActivity.class); // Giả sử bạn có một Activity cho Ảnh
        startActivity(intent);
    }
}
