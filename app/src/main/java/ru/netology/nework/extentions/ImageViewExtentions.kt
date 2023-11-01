package ru.netology.nework.extentions

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import java.io.File

fun ImageView.loadCircle(url: String) {
    Glide.with(this)
        .load(url)
        .circleCrop()
        .placeholder(R.drawable.loading_48)
        .error(R.drawable.loading_error_48)
        .timeout(10_000)
        .into(this)
}

fun ImageView.loadCircleFromLocalStorage(uri: Uri) {
    Glide.with(this)
        .load(uri)
        .circleCrop()
        .placeholder(R.drawable.loading_48)
        .error(R.drawable.loading_error_48)
        .timeout(5_000)
        .into(this)
}

fun ImageView.load(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.loading_48)
        .error(R.drawable.loading_error_48)
        .timeout(10_000)
        .into(this)
}

fun ImageView.loadWallAvatar(url: String) {
    Glide.with(this)
        .load(url)
        .circleCrop()
        .placeholder(R.drawable.loading_128)
        .error(R.drawable.loading_error_128)
        .timeout(10_000)
        .into(this)
}
