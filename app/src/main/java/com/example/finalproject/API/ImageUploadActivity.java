package com.example.finalproject.API;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalproject.R;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUploadActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String TAG = "ImageUploadActivity";
    private static final String IMGBB_API_KEY = "624193eae674616c29162ec371d7b54a";
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Uri> imageUris = new ArrayList<>();
    private TextView uploadResultText;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        recyclerView = findViewById(R.id.recycler_view);
        uploadResultText = findViewById(R.id.upload_result_text);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        imageAdapter = new ImageAdapter();
        recyclerView.setAdapter(imageAdapter);

        // Kiểm tra quyền truy cập bộ nhớ
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            loadImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImages();
        } else {
            Toast.makeText(this, "Cần quyền truy cập bộ nhớ để hiển thị ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImages() {
        imageUris.clear();
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ? OR " +
                MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = {"Pictures/YourAppName%", "DCIM/Camera%"};

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    imageUris.add(imageUri);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading images: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi tải danh sách ảnh", Toast.LENGTH_SHORT).show();
        }

        imageAdapter.notifyDataSetChanged();
    }

    private void uploadImage(Uri imageUri) {
        executorService.execute(() -> {
            try {
                // Lấy đường dẫn file từ URI
                String filePath = getRealPathFromURI(imageUri);
                if (filePath == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Không thể lấy đường dẫn ảnh", Toast.LENGTH_SHORT).show());
                    return;
                }

                File file = new File(filePath);
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", file.getName(),
                                RequestBody.create(file, MediaType.parse("image/*")))
                        .addFormDataPart("key", IMGBB_API_KEY)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.imgbb.com/1/upload")
                        .post(requestBody)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getBoolean("success")) {
                        String imageUrl = json.getJSONObject("data").getString("url");
                        runOnUiThread(() -> {
                            uploadResultText.setText("URL: " + imageUrl);
                            Toast.makeText(this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Tải lên thất bại", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            } catch (IOException | org.json.JSONException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi khi tải lên: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Upload error: " + e.getMessage(), e);
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting real path: " + e.getMessage(), e);
        }
        return null;
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
            }
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Uri imageUri = imageUris.get(position);
            Glide.with(ImageUploadActivity.this)
                    .load(imageUri)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.error)
                            .error(R.drawable.error))
                    .thumbnail(0.25f)
                    .centerCrop()
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> uploadImage(imageUri));
        }

        @Override
        public int getItemCount() {
            return imageUris.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}