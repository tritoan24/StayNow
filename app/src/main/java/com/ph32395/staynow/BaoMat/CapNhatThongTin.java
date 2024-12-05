package com.ph32395.staynow.BaoMat;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ph32395.staynow.DangKiDangNhap.DangKy;
import com.ph32395.staynow.MainActivity;
import com.ph32395.staynow.R;
import com.ph32395.staynow.fragment.home.HomeFragment;

import android.text.TextUtils;

public class CapNhatThongTin extends AppCompatActivity {
    private ImageView update_avatar;
    private EditText update_ten;
    private EditText update_sdt;
    private EditText update_email;
    private Spinner spiner_gioitinh;
    private Button btnUpdateInfor;
    private ImageButton backUpdate;
    private ImageButton choseImg;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cap_nhat_thong_tin);

        update_avatar = findViewById(R.id.update_avatar);
        update_ten = findViewById(R.id.update_ten);
        update_sdt = findViewById(R.id.update_sdt);
        update_email = findViewById(R.id.update_email);
        spiner_gioitinh = findViewById(R.id.spinner_gender);
        btnUpdateInfor = findViewById(R.id.btnUpdateInfor);
        backUpdate = findViewById(R.id.backScreen);
        choseImg = findViewById(R.id.choseImg);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        Log.d("UID", "UID: " + userId);

        // Lấy thông tin người dùng từ Firebase nếu có UID
        if (userId != null) {
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("ho_ten").getValue(String.class);
                        String phone = snapshot.child("sdt").getValue(String.class);
                        String img = snapshot.child("anh_daidien").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String gender = snapshot.child("gioi_tinh").getValue(String.class);

                        // Hiển thị dữ liệu vào EditText
                        update_ten.setText(name);
                        update_sdt.setText(phone);
                        update_email.setText(email);

                        // Kiểm tra và hiển thị "Chưa cập nhật" nếu số điện thoại là "ChuaCo"
                        if ("ChuaCo".equals(phone)) {
                            update_sdt.setText("Chưa cập nhật");
                        } else {
                            update_sdt.setText(phone);
                        }

                        // Gắn ảnh đại diện nếu có
                        if (img != null && !img.isEmpty()) {
                            Glide.with(CapNhatThongTin.this)
                                    .load(img)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_user)
                                    .into(update_avatar);
                        }

                        // Cập nhật Spinner giới tính (ví dụ: "Nam", "Nữ")
                        if ("Nam".equals(gender)) {
                            spiner_gioitinh.setSelection(0); // Giới tính là Nam
                        } else if ("Nữ".equals(gender)) {
                            spiner_gioitinh.setSelection(1); // Giới tính là Nữ
                        } else {
                            spiner_gioitinh.setSelection(2); // Giới tính khác (nếu có)
                        }
                    } else {
                        Log.d("CapNhatThongTin", "Người dùng không tồn tại");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("CapNhatThongTin", "Lỗi khi lấy dữ liệu người dùng: " + error.getMessage());
                }
            });
        }

        btnUpdateInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capNhatThongTin();
            }
        });

        choseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(CapNhatThongTin.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });
        backUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // Thêm phương thức để cập nhật giới tính trong Firebase khi người dùng thay đổi
    private void capNhatThongTin() {
        String name = update_ten.getText().toString().trim();
        String phone = update_sdt.getText().toString().trim();
        String email = update_email.getText().toString().trim();
        String gender = spiner_gioitinh.getSelectedItem().toString(); // Lấy giới tính người dùng chọn

        // Validate dữ liệu
        if (TextUtils.isEmpty(name)) {
            update_ten.setError("Vui lòng nhập tên!");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            update_sdt.setError("Vui lòng nhập số điện thoại!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            update_email.setError("Vui lòng nhập email!");
            return;
        }
        if (!isValidPhoneNumber(phone)) {
            update_sdt.setError("Số điện thoại không hợp lệ!");
            return;
        }
        if (!isValidEmail(email)) {
            update_email.setError("Email không hợp lệ!");
            return;
        }

        // Tiến hành cập nhật dữ liệu sau khi kiểm tra hợp lệ
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId != null) {
            mDatabase.child("NguoiDung").child(userId).child("ho_ten").setValue(name);
            mDatabase.child("NguoiDung").child(userId).child("sdt").setValue(phone);
            mDatabase.child("NguoiDung").child(userId).child("email").setValue(email);
            mDatabase.child("NguoiDung").child(userId).child("gioi_tinh").setValue(gender)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CapNhatThongTin.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CapNhatThongTin.this, "Cập nhật thông tin thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Nếu có chọn ảnh mới, upload ảnh lên Firebase Storage
            if (imageUri != null) {
                com.ph32395.staynow.Utils.ImageUploader imageUploader = new com.ph32395.staynow.Utils.ImageUploader();

                imageUploader.uploadImage(imageUri, userId, new com.ph32395.staynow.Utils.ImageUploader.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        // Lưu thông tin người dùng với URL ảnh
                        mDatabase.child("NguoiDung").child(userId).child("anh_daidien").setValue(imageUrl)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CapNhatThongTin.this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(CapNhatThongTin.this, "Cập nhật ảnh đại diện thất bại!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(CapNhatThongTin.this, "Tải ảnh lên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(CapNhatThongTin.this, "Không có ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Kiểm tra tính hợp lệ của số điện thoại (ví dụ: chỉ nhận số có 10 chữ số)
    private boolean isValidPhoneNumber(String phone) {
        return phone.length() == 10 && phone.matches("[0-9]+");
    }

    // Kiểm tra tính hợp lệ của email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Kiểm tra xem có phải là kết quả chọn ảnh không
        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData(); // Lưu đường dẫn hình ảnh
            Toast.makeText(this,"ImgURI:" + imageUri,Toast.LENGTH_SHORT).show();
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(update_avatar);
            update_avatar.setImageURI(imageUri); // Hiển thị hình ảnh lên ImageView
        }
    }
}
