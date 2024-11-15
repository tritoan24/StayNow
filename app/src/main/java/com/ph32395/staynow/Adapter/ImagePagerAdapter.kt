package com.ph32395.staynow.Adapter

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.ph32395.staynow.Activity.FullScreenImageActivity

class ImagePagerAdapter(
    private val viewPager: ViewPager
) : PagerAdapter() {

    private val images = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())  // Handler để chạy trên UI thread
    private var currentPosition = 0  // Vị trí hiện tại của hình ảnh

    fun setImages(newImages: List<String>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
        startAutoSlide() //Khoi chay Slider
    }

    // Phương thức để cập nhật hình ảnh hiển thị trong ViewPager
    fun setCurrentImage(imageUrl: String) {
        // Cập nhật danh sách hình ảnh nếu cần
        // Và chọn một vị trí nào đó để hiển thị
        val index = images.indexOf(imageUrl)
        if (index != -1) {
            viewPager.setCurrentItem(index, true)  // Chuyển tới hình ảnh được chọn
        }
    }

    // Hàm tự động chạy slider sau mỗi 3 giây
    private fun startAutoSlide() {
        handler.removeCallbacksAndMessages(null)  // Xóa mọi callback cũ
//        Kiem tra ds c rong khong roi mơi chay Slider
        if (images.isNotEmpty()) {
            handler.postDelayed(object : Runnable {
                override fun run() {
                    currentPosition = (currentPosition + 1) % images.size  // Chuyển sang ảnh tiếp theo
                    viewPager.setCurrentItem(currentPosition, true)
                    handler.postDelayed(this, 3000)  // Lập lịch cho 3 giây tiếp theo
                }
            }, 3000)  // Lập lịch ngay lần đầu tiên sau 3 giây
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(container.context).load(images[position]).into(imageView)

        // Sự kiện nhấn vào ảnh
        imageView.setOnClickListener {
            val context = container.context
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putExtra("image_url", images[position])
            context.startActivity(intent)
        }

        container.addView(imageView)
        return imageView
    }

    override fun getCount() = images.size
    override fun isViewFromObject(view: View, obj: Any) = view == obj
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}