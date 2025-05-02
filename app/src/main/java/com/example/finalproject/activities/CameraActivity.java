package com.example.finalproject.activities;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera; // Thêm import này
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View; // Thêm import này
import android.widget.Button;
import android.widget.ImageButton; // Thêm import này
import android.widget.Toast;

import com.example.finalproject.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO // Giữ lại nếu bạn dự định quay video
            // WRITE_EXTERNAL_STORAGE sẽ được thêm động nếu cần
    };

    private ActivityResultLauncher<String[]> activityResultLauncher;

    private PreviewView viewFinder;
    private Button imageCaptureButton;
    private ImageButton switchCameraButton; // Thêm nút chuyển camera
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    // Biến lưu trữ CameraProvider và CameraSelector hiện tại
    private ProcessCameraProvider cameraProvider;
    private CameraSelector currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA; // Bắt đầu với camera sau

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        viewFinder = findViewById(R.id.viewFinder);
        imageCaptureButton = findViewById(R.id.image_capture_button);
        switchCameraButton = findViewById(R.id.switch_camera_button); // Tìm nút chuyển camera

        cameraExecutor = Executors.newSingleThreadExecutor();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean permissionGranted = true;
                    // ... (Giữ nguyên logic kiểm tra quyền) ...
                    for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                        // Kiểm tra các quyền CỐT LÕI
                        if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
                            permissionGranted = false;
                            break;
                        }
                        // Kiểm tra quyền WRITE_EXTERNAL_STORAGE nếu được yêu cầu (Android <= 9)
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                                entry.getKey().equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                !entry.getValue()) {
                            permissionGranted = false;
                            break;
                        }
                    }


                    if (!permissionGranted) {
                        Toast.makeText(this,
                                "Quyền truy cập bị từ chối.",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        setupCamera(); // Thay vì startCamera trực tiếp, gọi setupCamera
                    }
                });

        checkAndRequestPermissions();

        imageCaptureButton.setOnClickListener(v -> takePhoto());

        // Thiết lập listener cho nút chuyển camera
        switchCameraButton.setOnClickListener(v -> switchCamera());
    }

    private void checkAndRequestPermissions() {
        if (allPermissionsGranted()) {
            setupCamera(); // Gọi setupCamera nếu đã có quyền
        } else {
            requestPermissions();
        }
    }

    // --- Phần xử lý quyền (Giữ nguyên) ---
    private void requestPermissions() {
        // ... (Giữ nguyên code) ...
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(REQUIRED_PERMISSIONS));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            Log.i(TAG, "Requesting permissions: " + permissionsToRequest);
            activityResultLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    private boolean allPermissionsGranted() {
        // ... (Giữ nguyên code) ...
        boolean basePermissionsGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                basePermissionsGranted = false;
                break;
            }
        }
        boolean storagePermissionGranted = true;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return basePermissionsGranted && storagePermissionGranted;
    }
    // --- Hết phần xử lý quyền ---


    // --- Phần CameraX ---

    // Hàm mới để khởi tạo CameraProvider
    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                // Sau khi có CameraProvider, bind các use case
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Không thể lấy CameraProvider", e);
                Toast.makeText(this, "Lỗi khởi tạo camera.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Hàm mới để bind (hoặc re-bind) các use case vào camera
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            Log.e(TAG, "CameraProvider chưa sẵn sàng.");
            return;
        }

        // --- Tạo Preview Use Case ---
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        // --- Tạo ImageCapture Use Case ---
        // Khởi tạo lại nếu chưa có hoặc khi bind lại (để đảm bảo cấu hình đúng)
        imageCapture = new ImageCapture.Builder().build();

        // --- Unbind tất cả trước khi bind lại ---
        cameraProvider.unbindAll();

        try {
            // --- Bind use cases vào lifecycle với CameraSelector hiện tại ---
            Camera camera = cameraProvider.bindToLifecycle(
                    (LifecycleOwner) this, currentCameraSelector, preview, imageCapture);

            Log.d(TAG, "CameraX bind thành công với selector: " + currentCameraSelector);

            // (Tùy chọn) Kiểm tra xem camera trước/sau có thực sự tồn tại không
            // và ẩn/hiện nút chuyển đổi nếu cần
            updateSwitchButtonVisibility();

        } catch (Exception e) {
            Log.e(TAG, "Không thể bind camera use cases", e);
            Toast.makeText(this, "Không thể khởi động camera đã chọn.", Toast.LENGTH_SHORT).show();
            // Có thể thử quay lại camera mặc định nếu lỗi xảy ra khi chuyển đổi
            if (currentCameraSelector != CameraSelector.DEFAULT_BACK_CAMERA) {
                Log.w(TAG, "Thử quay lại camera sau...");
                currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                bindCameraUseCases(); // Thử bind lại với camera sau
            } else {
                finish(); // Nếu camera sau cũng lỗi thì thoát
            }
        }
    }

    // Hàm xử lý việc chuyển đổi camera
    private void switchCamera() {
        Log.d(TAG, "Chuyển đổi camera");
        if (cameraProvider == null) {
            Log.e(TAG, "CameraProvider chưa sẵn sàng để chuyển đổi.");
            return; // Chưa thể chuyển nếu provider chưa có
        }

        // Xác định camera selector mới
        CameraSelector newSelector;
        if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            newSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        } else {
            newSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        }

        // Kiểm tra xem thiết bị có camera tương ứng không
        try {
            if (cameraProvider.hasCamera(newSelector)) {
                currentCameraSelector = newSelector; // Cập nhật selector hiện tại
                bindCameraUseCases(); // Bind lại use cases với selector mới
            } else {
                Log.w(TAG, "Thiết bị không có " + (newSelector == CameraSelector.DEFAULT_FRONT_CAMERA ? "camera trước." : "camera sau."));
                Toast.makeText(this, "Không tìm thấy camera để chuyển.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) { // CameraInfoUnavailableException có thể xảy ra
            Log.e(TAG, "Lỗi khi kiểm tra camera có sẵn", e);
            Toast.makeText(this, "Lỗi khi chuyển camera.", Toast.LENGTH_SHORT).show();
        }
    }

    // (Tùy chọn) Cập nhật trạng thái hiển thị của nút chuyển camera
    private void updateSwitchButtonVisibility() {
        if (cameraProvider == null) {
            switchCameraButton.setVisibility(View.GONE);
            return;
        }
        try {
            boolean hasBackCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
            boolean hasFrontCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
            // Chỉ hiển thị nút chuyển nếu có cả camera trước và sau
            switchCameraButton.setVisibility(hasBackCamera && hasFrontCamera ? View.VISIBLE : View.GONE);
        } catch (Exception e) { // CameraInfoUnavailableException
            Log.e(TAG, "Không thể kiểm tra camera có sẵn", e);
            switchCameraButton.setVisibility(View.GONE); // Ẩn đi nếu có lỗi
        }
    }

    // --- Hàm chụp ảnh (Giữ nguyên) ---
    private void takePhoto() {
        // ... (Giữ nguyên code) ...
        if (imageCapture == null) {
            Log.w(TAG, "ImageCapture use case is not initialized.");
            return;
        }
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourAppName");
        }
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Lấy URI ảnh đã lưu
                        Uri savedUri = outputFileResults.getSavedUri();

                        // Thông báo và log
                        String msg = "Ảnh đã được lưu: " + savedUri;
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);


                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Lỗi khi chụp ảnh: " + exception.getMessage(), exception);
                        Toast.makeText(getBaseContext(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    // --- Hết hàm chụp ảnh ---


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }
}