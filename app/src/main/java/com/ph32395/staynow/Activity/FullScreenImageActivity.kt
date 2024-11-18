package com.ph32395.staynow.Activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.bumptech.glide.request.transition.Transition
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.github.chrisbanes.photoview.PhotoView
import com.ph32395.staynow.R

class FullScreenImageActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val photoView: PhotoView = findViewById(R.id.photoView)
        val imageUrl = intent.getStringExtra("image_url")
        val iconBack: ImageView = findViewById(R.id.iconBackFullImage)

        if (imageUrl != null) {
            // Tải ảnh với Glide và điều chỉnh tỷ lệ
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Tính toán chiều cao dựa trên chiều rộng màn hình
                        val screenWidth = resources.displayMetrics.widthPixels
                        val aspectRatio = resource.height.toFloat() / resource.width
                        val newHeight = (screenWidth * aspectRatio).toInt()

                        // Cập nhật chiều cao PhotoView
                        val layoutParams = photoView.layoutParams
                        layoutParams.width = screenWidth
                        layoutParams.height = newHeight
                        photoView.layoutParams = layoutParams

                        photoView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Không cần xử lý trong trường hợp này
                    }
                })
        }

        // Đóng Activity khi người dùng nhấn nút Back
        iconBack.setOnClickListener {
            finish()
        }

    }
}