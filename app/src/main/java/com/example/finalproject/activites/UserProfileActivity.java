package com.example.finalproject.activites;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText, occupationEditText, birthdayEditText, addressEditText;
    private Button updateButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Lấy UID người dùng hiện tại
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        // Khởi tạo các EditText và Button
        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        occupationEditText = findViewById(R.id.occupationEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        addressEditText = findViewById(R.id.addressEditText);
        updateButton = findViewById(R.id.updateButton);

        // Lấy thông tin người dùng từ Firestore và hiển thị lên giao diện
        loadUserData();

        // Khi người dùng nhấn nút "Update Info"
        updateButton.setOnClickListener(v -> updateUserInfo());
    }

    private void loadUserData() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        int age = documentSnapshot.getLong("age").intValue();
                        String occupation = documentSnapshot.getString("occupation");
                        String birthday = documentSnapshot.getString("birthday");
                        String address = documentSnapshot.getString("address");

                        // Hiển thị thông tin người dùng
                        nameEditText.setText(name);
                        ageEditText.setText(String.valueOf(age));
                        occupationEditText.setText(occupation);
                        birthdayEditText.setText(birthday);
                        addressEditText.setText(address);
                    } else {
                        Toast.makeText(UserProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {
        String name = nameEditText.getText().toString().trim();  // Lấy tên người dùng
        String ageStr = ageEditText.getText().toString().trim();
        String occupation = occupationEditText.getText().toString().trim();
        String birthday = birthdayEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || occupation.isEmpty() || birthday.isEmpty() || address.isEmpty()) {
            Toast.makeText(UserProfileActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        // Cập nhật thông tin người dùng
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);  // Cập nhật tên người dùng
        userData.put("age", age);
        userData.put("occupation", occupation);
        userData.put("birthday", birthday);
        userData.put("address", address);

        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfileActivity.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileActivity.this, "Error updating data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
