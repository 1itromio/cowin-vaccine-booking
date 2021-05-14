package dev.romio.cowinvaccinebook.util

import android.util.Base64
import android.view.View
import com.squareup.moshi.Moshi
import dev.romio.cowinvaccinebook.data.model.ErrorResponse
import retrofit2.HttpException
import java.security.MessageDigest

fun HttpException.toErrorResponse(): ErrorResponse? {
    return try {
        this.response()?.errorBody()?.source()?.let {
            val moshiAdapter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
            moshiAdapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}

fun String.md5(): String {
    return hashString(this, "MD5")
}

fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun String.decodeBase64String(): String {
    val byteArray = Base64.decode(this, Base64.DEFAULT)
    return String(byteArray)
}
