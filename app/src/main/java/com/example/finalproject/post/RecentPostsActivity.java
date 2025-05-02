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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecentPostsActivity extends AppCompatActivity {
    private static final String TAG = "RecentPostsActivity";
    private static final int PAGE_SIZE = 3;
    private FirebaseFirestore db;
    private ViewPager2 viewPager;
    private PostAdapter postAdapter;
    private List<Map<String, Object>> posts = new ArrayList<>();
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_posts);

        db = FirebaseFirestore.getInstance();
        viewPager = findViewById(R.id.view_pager);
        postAdapter = new PostAdapter(this);
        viewPager.setAdapter(postAdapter);

        // Tải 3 bài post đầu tiên
        loadPosts();

        // Listener để tải thêm khi kéo đến cuối
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= posts.size() - 1 && !isLoading) {
                    loadPosts();
                }
            }
        });
    }

    private void loadPosts() {
        isLoading = true;
        Query query = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            posts.add(document.getData());
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Đã tải hết bài post", Toast.LENGTH_SHORT).show();
                    }
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading posts: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi tải bài post", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
    }

    private class PostAdapter extends FragmentStateAdapter {
        public PostAdapter(@NonNull RecentPostsActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return PostFragment.newInstance(posts.get(position));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }
    }
}