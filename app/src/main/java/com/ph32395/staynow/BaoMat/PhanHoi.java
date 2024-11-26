package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    private Uri selectedImageUri; // Lưu URI ảnh đã chọn
    private FirebaseFirestore db; // Đối tượng Firestore

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

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Gắn sự kiện cho nút chọn ảnh
        phanHoiImg.setOnClickListener(v -> {
            ImagePicker.with(PhanHoi.this)
                    .crop() // Cắt ảnh
                    .compress(1024) // Nén ảnh, tối đa 1MB
                    .maxResultSize(1080, 1080) // Giới hạn kích thước ảnh
                    .start();
        });

        // Gắn sự kiện cho nút gửi phản hồi
        btnPhanhoi.setOnClickListener(v -> {
            String feedback = commentFeedback.getText().toString().trim();
            String feedbackTime = phanHoiTime.getText().toString().trim();

            // Kiểm tra xem các trường có bị bỏ trống không
            if (feedback.isEmpty() || feedbackTime.isEmpty() || selectedImageUri == null) {
                Toast.makeText(PhanHoi.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                sendFeedback(feedback, feedbackTime, selectedImageUri);
            }
        });

        // Gắn sự kiện cho TextView phanHoiTime (ngày tháng và giờ)
        phanHoiTime.setOnClickListener(v -> showDateTimePickerDialog());
    }

    // Hàm hiển thị DatePickerDialog và TimePickerDialog
    private void showDateTimePickerDialog() {
        // Lấy ngày tháng hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Khởi tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(PhanHoi.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Cập nhật phanHoiTime khi chọn ngày
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;

                    // Sau khi chọn ngày, mở TimePickerDialog để chọn giờ
                    showTimePickerDialog(date);
                }, year, month, day);

        // Hiển thị dialog
        datePickerDialog.show();
    }

    // Hàm hiển thị TimePickerDialog
    private void showTimePickerDialog(String selectedDate) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(PhanHoi.this,
                (view, selectedHour, selectedMinute) -> {
                    // Cập nhật thời gian vào phanHoiTime
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    phanHoiTime.setText(selectedDate + " " + time);
                }, hour, minute, true); // true để hiển thị thời gian 24 giờ

        // Hiển thị dialog chọn giờ
        timePickerDialog.show();
    }

    // Hàm gửi phản hồi lên Firestore
    private void sendFeedback(String feedback, String feedbackTime, Uri imageUri) {
        // Kiểm tra xem URI ảnh có hợp lệ không
        if (imageUri != null) {
            // Lưu ảnh vào Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("feedback_images");
            StorageReference imageRef = storageReference.child(System.currentTimeMillis() + ".jpg");

            // Tải ảnh lên Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Lấy URL ảnh sau khi tải lên Firebase Storage thành công
                        String imageUrl = uri.toString();

                        // Gửi phản hồi lên Firestore
                        Map<String, Object> feedbackData = new HashMap<>();
                        feedbackData.put("noi_dung", feedback);
                        feedbackData.put("thoi_giangui", feedbackTime);
                        feedbackData.put("img", imageUrl); // Lưu đường dẫn ảnh
                        feedbackData.put("createdAt", System.currentTimeMillis());

                        // Gửi dữ liệu lên Firestore vào bảng PhanHoi
                        db.collection("PhanHoi")
                                .add(feedbackData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(PhanHoi.this, "Phản hồi đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                                    clearFields(); // Xóa các trường sau khi gửi
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(PhanHoi.this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(PhanHoi.this, "Có lỗi xảy ra khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu không có ảnh, chỉ gửi phản hồi mà không có ảnh
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("noi_dung", feedback);
            feedbackData.put("thoi_giangui", feedbackTime);
            feedbackData.put("img", ""); // Nếu không có ảnh, gán là chuỗi rỗng
            feedbackData.put("createdAt", System.currentTimeMillis());

            // Gửi phản hồi lên Firestore
            db.collection("PhanHoi")
                    .add(feedbackData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(PhanHoi.this, "Phản hồi đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                        clearFields(); // Xóa các trường sau khi gửi
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PhanHoi.this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Hàm xóa các trường sau khi gửi phản hồi
    private void clearFields() {
        commentFeedback.setText("");
        phanHoiTime.setText("");
        phanHoiAvatar.setImageResource(R.drawable.ic_user); // Đặt lại ảnh mặc định
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            // Lấy URI của ảnh đã chọn
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Hiển thị ảnh vào ImageView
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop() // Hiển thị ảnh dạng hình tròn
                        .placeholder(R.drawable.ic_user) // Ảnh mặc định nếu tải ảnh thất bại
                        .into(phanHoiAvatar);

                Toast.makeText(this, "Ảnh đã được chọn!", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, "Lỗi chọn ảnh: " + ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }
}
