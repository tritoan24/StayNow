package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PhanHoi extends AppCompatActivity {

    private ImageView phanHoiAvatar;
    private ImageButton phanHoiImg;
    private EditText commentFeedback;
    private EditText phanHoiTime;
    private Button btnPhanhoi;
    private ImageButton btnbackFeed;
    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference mDatabase;

    private String maNguoiDung; // Lưu mã người dùng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phan_hoi);

        // Khởi tạo các view
        phanHoiAvatar = findViewById(R.id.phanhoi_avatar);
        phanHoiImg = findViewById(R.id.phanhoi_Img);
        commentFeedback = findViewById(R.id.commentFeedback);
        phanHoiTime = findViewById(R.id.phanhoiTime);
        btnPhanhoi = findViewById(R.id.btnPhanhoi);
        btnbackFeed = findViewById(R.id.backFeedback);
        mAuth = FirebaseAuth.getInstance();
        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Lấy mã người dùng
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        getMaNguoiDung(userId);

        // Gắn sự kiện chọn ảnh
        phanHoiImg.setOnClickListener(v -> {
            ImagePicker.with(PhanHoi.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        btnbackFeed.setOnClickListener(v -> {
            startActivity( new Intent(PhanHoi.this, MainActivity.class));
        });
        // Gắn sự kiện gửi phản hồi
        btnPhanhoi.setOnClickListener(v -> {
            String feedback = commentFeedback.getText().toString().trim();
            String feedbackTime = phanHoiTime.getText().toString().trim();

            if (feedback.isEmpty() || feedbackTime.isEmpty() || selectedImageUri == null || maNguoiDung == null) {
                Toast.makeText(PhanHoi.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                sendFeedback(feedback, feedbackTime, selectedImageUri, maNguoiDung);
            }
        });

        // Gắn sự kiện cho phanHoiTime
        phanHoiTime.setOnClickListener(v -> showDateTimePickerDialog());
    }

    // Lấy ma_nguoidung từ Firebase Realtime Database
    private void getMaNguoiDung(String userId) {
        mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    maNguoiDung = snapshot.child("ma_nguoidung").getValue(String.class);
                } else {
                    Log.e("PhanHoi", "Người dùng không tồn tại");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("PhanHoi", "Lỗi khi lấy ma_nguoidung: " + error.getMessage());
            }
        });
    }

    // Hiển thị DatePickerDialog và TimePickerDialog
    private void showDateTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(PhanHoi.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    showTimePickerDialog(date);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog(String selectedDate) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(PhanHoi.this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    phanHoiTime.setText(selectedDate + " " + time);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    // Gửi phản hồi
    private void sendFeedback(String feedback, String feedbackTime, Uri imageUri, String maNguoiDung) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("feedback_images");
        StorageReference imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    Map<String, Object> feedbackData = new HashMap<>();
                    feedbackData.put("noi_dung", feedback);
                    feedbackData.put("thoi_giangui", feedbackTime);
                    feedbackData.put("img", imageUrl);
                    feedbackData.put("ma_nguoidung", maNguoiDung);
                    feedbackData.put("createdAt", System.currentTimeMillis());

                    db.collection("PhanHoi")
                            .add(feedbackData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(PhanHoi.this, "Phản hồi đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                                clearFields();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(PhanHoi.this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(PhanHoi.this, "Có lỗi xảy ra khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        commentFeedback.setText("");
        phanHoiTime.setText("");
        phanHoiAvatar.setImageResource(R.drawable.ic_user);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user)
                        .into(phanHoiAvatar);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, "Lỗi chọn ảnh: " + ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }
}
