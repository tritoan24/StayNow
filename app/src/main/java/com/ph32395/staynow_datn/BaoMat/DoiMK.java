package com.ph32395.staynow_datn.BaoMat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ph32395.staynow_datn.R;

public class DoiMK extends AppCompatActivity {

    private ImageButton buttonBack;
    private ImageView doimkImage;
    private TextInputEditText textInputMK;
    private TextInputEditText editNewPass;
    private TextInputEditText confirmPass;
    private Button buttonNewPass;
    private FirebaseAuth mAuth;

    private int incorrectPasswordAttempts = 0;  // Biến theo dõi số lần nhập sai mật khẩu

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doi_mk);

        // Khởi tạo view
        doimkImage = findViewById(R.id.doimk_image);
        textInputMK = findViewById(R.id.editTextCurrentPassword);
        editNewPass = findViewById(R.id.editTextNewPassword);
        confirmPass = findViewById(R.id.editTextConfirmNewPassword);
        buttonNewPass = findViewById(R.id.buttonChangePassword);
        buttonBack = findViewById(R.id.button_backDoiMK);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();

        // Lấy thông tin người dùng hiện tại
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Xử lý nút quay lại
        buttonBack.setOnClickListener(view -> {
            onBackPressed();
        });

        // Xử lý đổi mật khẩu
        // Xử lý đổi mật khẩu
        buttonNewPass.setOnClickListener(view -> {
            String currentPassword = textInputMK.getText().toString().trim();
            String newPassword = editNewPass.getText().toString().trim();
            String confirmPassword = confirmPass.getText().toString().trim();

            // Kiểm tra xem mật khẩu có hợp lệ không
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(DoiMK.this, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this,"Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.matches("^(?=.*[A-Z])(?=.*[0-9]).{6,}$")) {
                Toast.makeText(this,"Mật khẩu phải có ít nhất 1 chữ hoa và 1 chữ số",Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this,"Mật khẩu không trùng khớp",Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu mới không giống mật khẩu cũ
            if (currentPassword.equals(newPassword)) {
                Toast.makeText(DoiMK.this, "Mật khẩu mới không thể giống mật khẩu cũ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xử lý xác thực lại người dùng và cập nhật mật khẩu
            if (currentUser != null) {
                String email = currentUser.getEmail();

                // Xác thực lại người dùng với mật khẩu hiện tại
                AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

                // Yêu cầu người dùng đăng nhập lại
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Nếu xác thực thành công, thay đổi mật khẩu
                                currentUser.updatePassword(newPassword)
                                        .addOnCompleteListener(passwordTask -> {
                                            if (passwordTask.isSuccessful()) {
                                                Log.d("DoiMK", "Mật khẩu đã được cập nhật.");
                                            } else {
                                                Log.e("DoiMK", "Cập nhật mật khẩu thất bại: " + passwordTask.getException());
                                            }
                                        });
                            } else {
                                Log.e("DoiMK", "Xác thực lại thất bại: " + task.getException());
                            }
                        });
            } else {
                Log.e("DoiMK", "Người dùng chưa đăng nhập.");
            }

            // Kiểm tra số lần nhập mật khẩu cũ sai
            // Kiểm tra mật khẩu cũ
            if (currentUser != null) {
                String userId = currentUser.getUid();
                // Giả sử mật khẩu cũ là "Mật khẩu thật sự cũ"
                if (!currentPassword.equals("Mật khẩu thật sự cũ")) {
                    incorrectPasswordAttempts++;

                    // Hiển thị Toast nếu số lần nhập mật khẩu sai dưới 5 lần
                    if (incorrectPasswordAttempts < 5) {
                        Toast.makeText(this, "Mật khẩu của bạn bị sai. Lần " + incorrectPasswordAttempts, Toast.LENGTH_SHORT).show();
                    } else {
                        // Hiển thị AlertDialog khi đã nhập sai 5 lần
                        Toast.makeText(this, "Mật khẩu của bạn không đúng", Toast.LENGTH_SHORT).show();

                        // Hiển thị dialog gợi ý quên mật khẩu với bo góc tròn
                        AlertDialog.Builder builder = new AlertDialog.Builder(DoiMK.this);

                        // Đặt background bo góc tròn cho dialog
                        builder.setMessage("Bạn quên mật khẩu? Bạn có cần hỗ trợ lấy lại mật khẩu không?")
                                .setPositiveButton("Có", (dialog, which) -> {
                                    startActivity(new Intent(DoiMK.this, QuenMK.class));
                                })
                                .setNegativeButton("Không", null);

                        // Tạo một AlertDialog và áp dụng background bo tròn
                        AlertDialog dialog = builder.create();
                        dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_edittext);  // Sử dụng drawable bo tròn
                        dialog.show();
                    }
                }
            }

        });
    }
}