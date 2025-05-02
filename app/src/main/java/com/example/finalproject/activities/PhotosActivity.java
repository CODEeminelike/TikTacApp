package com.example.finalproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.API.ImageUploadActivity;
import com.example.finalproject.R;
import com.example.finalproject.post.CreatePostActivity;

public class PhotosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        Button openCameraButton = findViewById(R.id.open_camera_button);
        Button openGalleryButton = findViewById(R.id.open_gallery_button);
        Button openPostButton = findViewById(R.id.open_post_button);

        openCameraButton.setOnClickListener(v -> {
            // Tạo Intent để mở CameraActivity
            Intent intent = new Intent(PhotosActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        openGalleryButton.setOnClickListener(v -> {
            // Tạo Intent để mở GalleryActivity
            Intent intent = new Intent(PhotosActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        openPostButton.setOnClickListener(v -> {
            // Tạo Intent để mở GalleryActivity
            Intent intent = new Intent(PhotosActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });
    }
}