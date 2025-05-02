package com.example.finalproject.post;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPostsActivity extends AppCompatActivity {
    private static final String TAG = "UserPostsActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ViewPager2 viewPager;
    private PostAdapter postAdapter;
    private List<Map<String, Object>> posts = new ArrayList<>();
    private List<String> postIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        viewPager = findViewById(R.id.view_pager);
        postAdapter = new PostAdapter(this);
        viewPager.setAdapter(postAdapter);

        // Tải bài post của người dùng
        loadUserPosts();
    }

    private void loadUserPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem bài post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        if (userName == null || userName.isEmpty()) {
                            Toast.makeText(this, "Thông tin người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        db.collection("posts")
                                .whereEqualTo("userName", userName)
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        Map<String, Object> postData = new HashMap<>(document.getData());
                                        posts.add(postData);
                                        postIds.add(document.getId());
                                    }
                                    postAdapter.notifyDataSetChanged();
                                    if (posts.isEmpty()) {
                                        Toast.makeText(this, "Bạn chưa có bài post nào", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading user posts: " + e.getMessage(), e);
                                    if (e instanceof FirebaseFirestoreException) {
                                        FirebaseFirestoreException ffe = (FirebaseFirestoreException) e;
                                        if (ffe.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                                            Toast.makeText(this, "Truy vấn yêu cầu chỉ mục Firestore. Vui lòng kiểm tra logcat để tạo chỉ mục.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "Lỗi khi tải bài post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "Lỗi khi tải bài post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user info: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private class PostAdapter extends FragmentStateAdapter {
        public PostAdapter(@NonNull UserPostsActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return PostFragment.newInstance(posts.get(position), postIds.get(position));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }
}