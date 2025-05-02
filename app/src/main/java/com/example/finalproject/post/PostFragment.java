package com.example.finalproject.post;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.finalproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PostFragment extends Fragment {
    private static final String ARG_POST = "post";
    private static final String ARG_POST_ID = "post_id";
    private static final String TAG = "PostFragment";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String postId;
    private Map<String, Object> postData;
    private TextView postContent;
    private EditText commentEditText;
    private Button likeButton;
    private Button dislikeButton;
    private Button commentButton;
    private Map<String, String> userIdToNameCache = new ConcurrentHashMap<>();

    public static PostFragment newInstance(Map<String, Object> post, String postId) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, (java.io.Serializable) post);
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        postContent = view.findViewById(R.id.post_content);
        likeButton = view.findViewById(R.id.like_button);
        dislikeButton = view.findViewById(R.id.dislike_button);
        commentEditText = view.findViewById(R.id.comment_edit_text);
        commentButton = view.findViewById(R.id.comment_button);

        if (getArguments() != null) {
            postData = (Map<String, Object>) getArguments().getSerializable(ARG_POST);
            postId = getArguments().getString(ARG_POST_ID);
            updatePostContent();
        }

        // Xử lý nút Like
        likeButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để tương tác", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("posts").document(postId)
                    .update("likes", FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> {
                        postData.put("likes", ((Long) postData.get("likes")) + 1);
                        updatePostContent();
                        Toast.makeText(getContext(), "Đã thích!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating likes: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Lỗi khi thích bài post", Toast.LENGTH_SHORT).show();
                    });
        });

        // Xử lý nút Dislike
        dislikeButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để tương tác", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("posts").document(postId)
                    .update("dislikes", FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> {
                        postData.put("dislikes", ((Long) postData.get("dislikes")) + 1);
                        updatePostContent();
                        Toast.makeText(getContext(), "Đã không thích!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating dislikes: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Lỗi khi không thích bài post", Toast.LENGTH_SHORT).show();
                    });
        });

        // Xử lý nút Comment
        commentButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
                return;
            }
            String commentText = commentEditText.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> comment = new HashMap<>();
            comment.put("userId", user.getUid());
            comment.put("commentText", commentText);
            comment.put("timestamp", Timestamp.now());

            db.collection("posts").document(postId)
                    .update("comments", FieldValue.arrayUnion(comment))
                    .addOnSuccessListener(aVoid -> {
                        List<Map<String, Object>> comments = (List<Map<String, Object>>) postData.get("comments");
                        comments.add(comment);
                        // Cache userName của người dùng hiện tại
                        db.collection("users").document(user.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String userName = documentSnapshot.getString("name");
                                        if (userName != null) {
                                            userIdToNameCache.put(user.getUid(), userName);
                                        }
                                    }
                                    commentEditText.setText("");
                                    updatePostContent();
                                    Toast.makeText(getContext(), "Đã thêm bình luận!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error fetching userName for comment: " + e.getMessage(), e);
                                    commentEditText.setText("");
                                    updatePostContent();
                                    Toast.makeText(getContext(), "Đã thêm bình luận nhưng lỗi khi lấy tên người dùng", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding comment: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Lỗi khi thêm bình luận", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private void updatePostContent() {
        if (postData == null) return;

        String title = (String) postData.get("title");
        String userName = (String) postData.get("userName");
        String imageUrl = (String) postData.get("imageUrl");
        Timestamp timestamp = (Timestamp) postData.get("timestamp");
        Long likes = (Long) postData.get("likes");
        Long dislikes = (Long) postData.get("dislikes");
        List<Map<String, Object>> comments = (List<Map<String, Object>>) postData.get("comments");

        // Định dạng thời gian
        String timeString = timestamp != null
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(timestamp.toDate())
                : "N/A";

        // Xây dựng nội dung text
        StringBuilder content = new StringBuilder();
        content.append("Tiêu đề: ").append(title).append("\n")
                .append("Người đăng: ").append(userName).append("\n")
                .append("URL ảnh: ").append(imageUrl).append("\n")
                .append("Thời gian: ").append(timeString).append("\n")
                .append("Likes: ").append(likes != null ? likes : 0).append("\n")
                .append("Dislikes: ").append(dislikes != null ? dislikes : 0).append("\n")
                .append("Comments:\n");

        if (comments != null && !comments.isEmpty()) {
            for (Map<String, Object> comment : comments) {
                String commentText = (String) comment.get("commentText");
                String commentUserId = (String) comment.get("userId");
                Timestamp commentTimestamp = (Timestamp) comment.get("timestamp");
                String commentTime = commentTimestamp != null
                        ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(commentTimestamp.toDate())
                        : "N/A";

                // Kiểm tra cache trước
                String commentUserName = userIdToNameCache.get(commentUserId);
                if (commentUserName != null) {
                    content.append("- ").append(commentUserName).append(": ")
                            .append(commentText).append(" (").append(commentTime).append(")\n");
                } else {
                    // Truy vấn userName từ Firestore
                    db.collection("users").document(commentUserId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String userNameFromDb = documentSnapshot.getString("name");
                                    if (userNameFromDb != null) {
                                        userIdToNameCache.put(commentUserId, userNameFromDb);
                                    }
                                }
                                // Cập nhật lại nội dung sau khi lấy được userName
                                updatePostContent();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching userName: " + e.getMessage(), e);
                                userIdToNameCache.put(commentUserId, "Unknown");
                                updatePostContent();
                            });
                    // Hiển thị tạm thời nếu chưa có userName
                    content.append("- ").append("Loading...").append(": ")
                            .append(commentText).append(" (").append(commentTime).append(")\n");
                }
            }
        } else {
            content.append("Chưa có comment.\n");
        }

        postContent.setText(content.toString());
    }
}