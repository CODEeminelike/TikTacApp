package com.example.finalproject.activites;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText, ageEditText, occupationEditText, birthdayEditText, addressEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);  // EditText để nhập tên người dùng
        ageEditText = findViewById(R.id.ageEditText);    // EditText để nhập tuổi người dùng
        occupationEditText = findViewById(R.id.occupationEditText);  // EditText để nhập nghề nghiệp
        birthdayEditText = findViewById(R.id.birthdayEditText);  // EditText để nhập ngày sinh
        addressEditText = findViewById(R.id.addressEditText);  // EditText để nhập địa chỉ
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim(); // Lấy tên người dùng
        String ageStr = ageEditText.getText().toString().trim(); // Lấy tuổi người dùng
        String occupation = occupationEditText.getText().toString().trim(); // Lấy nghề nghiệp
        String birthday = birthdayEditText.getText().toString().trim(); // Lấy ngày sinh
        String address = addressEditText.getText().toString().trim(); // Lấy địa chỉ

        // Kiểm tra các trường không được để trống
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || ageStr.isEmpty() || occupation.isEmpty() || birthday.isEmpty() || address.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Lấy thông tin người dùng từ Firebase Authentication
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Lưu thông tin người dùng vào Firestore
                        if (user != null) {
                            String userId = user.getUid();
                            int age = Integer.parseInt(ageStr); // Chuyển tuổi sang kiểu số

                            // Tạo Map để lưu thông tin người dùng
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("age", age);
                            userData.put("occupation", occupation);
                            userData.put("birthday", birthday);
                            userData.put("address", address);
                            userData.put("email", email);

                            // Lưu thông tin vào Firestore
                            db.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
