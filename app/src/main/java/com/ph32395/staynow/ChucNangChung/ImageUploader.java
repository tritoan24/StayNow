package com.ph32395.staynow.ChucNangChung;

import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageUploader {
    private StorageReference mStorageRef;

    public ImageUploader() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public void uploadImage(Uri imageUri, String userId, UploadCallback callback) {
        // Tạo đường dẫn để lưu ảnh
        StorageReference fileReference = mStorageRef.child("avatars/" + userId + ".jpg");

        // Tải ảnh lên
        UploadTask uploadTask = fileReference.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Lấy URL của ảnh sau khi tải lên thành công
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                callback.onSuccess(imageUrl); // Gọi callback với URL ảnh
            });
        }).addOnFailureListener(e -> {
            callback.onFailure(e); // Gọi callback khi có lỗi
        });
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }
}
