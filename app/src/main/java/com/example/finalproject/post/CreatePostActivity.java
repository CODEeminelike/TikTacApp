package com.example.finalproject.post;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.finalproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userNameText;
    private EditText imageUrlEditText;
    private Button postButton;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameText = findViewById(R.id.user_name_text);
        imageUrlEditText = findViewById(R.id.image_url_edit_text);
        postButton = findViewById(R.id.post_button);

        // Lấy thông tin người dùng
        fetchUserInfo();

        postButton.setOnClickListener(v -> createPost());
    }

    private void fetchUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng bài", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("name");
                        userNameText.setText("Đăng bởi: " + userName);
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user info: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void createPost() {
        String imageUrl = imageUrlEditText.getText().toString().trim();

        if (imageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập URL ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName == null) {
            Toast.makeText(this, "Đang tải thông tin người dùng, thử lại sau", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo dữ liệu bài post
        Map<String, Object> postData = new HashMap<>();
        postData.put("userName", userName);
        postData.put("imageUrl", imageUrl);
        postData.put("timestamp", Timestamp.now());
        postData.put("likes", 0);
        postData.put("dislikes", 0);
        postData.put("comments", new ArrayList<Map<String, Object>>());

        // Lưu bài post vào Firestore
        db.collection("posts")
                .add(postData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi đăng bài: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}