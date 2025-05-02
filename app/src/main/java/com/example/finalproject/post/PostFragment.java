package com.example.finalproject.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.finalproject.R;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostFragment extends Fragment {
    private static final String ARG_POST = "post";

    public static PostFragment newInstance(Map<String, Object> post) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, (java.io.Serializable) post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        TextView postContent = view.findViewById(R.id.post_content);

        if (getArguments() != null) {
            Map<String, Object> post = (Map<String, Object>) getArguments().getSerializable(ARG_POST);
            if (post != null) {
                String title = (String) post.get("title");
                String userName = (String) post.get("userName");
                String imageUrl = (String) post.get("imageUrl");
                Timestamp timestamp = (Timestamp) post.get("timestamp");
                Long likes = (Long) post.get("likes");
                Long dislikes = (Long) post.get("dislikes");
                List<Map<String, Object>> comments = (List<Map<String, Object>>) post.get("comments");

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
                        content.append("- ").append(commentUserId).append(": ")
                                .append(commentText).append(" (").append(commentTime).append(")\n");
                    }
                } else {
                    content.append("Chưa có comment.\n");
                }

                postContent.setText(content.toString());
            }
        }

        return view;
    }
}