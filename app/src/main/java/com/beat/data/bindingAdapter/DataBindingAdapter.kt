package com.beat.data.bindingAdapter

import android.graphics.drawable.Drawable
import android.text.Html
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.beat.R
import com.beat.util.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("imageUrl")
fun setImage(view: ImageView, imageUrl: String?) {
    if (imageUrl != null) {
        val circularProgressDrawable = CircularProgressDrawable(view.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        val options: RequestOptions
        options = RequestOptions()
            .fitCenter()
            .placeholder(circularProgressDrawable)
            .error(circularProgressDrawable)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .circleCrop()
            .dontAnimate()
            .dontTransform()

        Glide.with(view.context).load(imageUrl)
            .apply(options)
            .into(view)
    }
}

@BindingAdapter("imageUrl", "error")
fun setImageWithErrorCheck(view: ImageView, imageUrl: String?, error: Drawable?) {
    if (imageUrl != null && error != null) {
        val circularProgressDrawable = CircularProgressDrawable(view.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        val options: RequestOptions
        options = RequestOptions()
            .fitCenter()
            .placeholder(circularProgressDrawable)
            .error(error)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .circleCrop()
            .dontAnimate()
            .dontTransform()

        Glide.with(view.context).load(imageUrl)
            .apply(options)
            .into(view)
    }
}

@BindingAdapter("favorite")
fun setFavorite(view: ImageView, favorite: Boolean?) {
    if (favorite != null && favorite) {
        view.setImageResource(R.drawable.ic_favorite_fill_28)
    } else {
        view.setImageResource(R.drawable.ic_favorite_border_28)
    }
}

@BindingAdapter("playerFavorite")
fun setPlayerFavorite(view: ImageView, favorite: Boolean?) {
    if (favorite != null && favorite) {
        view.setImageResource(R.drawable.ic_favorite_fill)
    } else {
        view.setImageResource(R.drawable.ic_favorite_border)
    }
}

@BindingAdapter("msisdn")
fun setMsisdn(view: TextView, msisdn: String?) {
    view.text = if (msisdn != null) view.context.getString(R.string.msisdn_format, msisdn) else ""
}

@BindingAdapter("yearFromDate")
fun setYearFromDate(view: TextView, date: String?) {
    view.text = if (date != null) getYearFromDate(date) else ""
}

@BindingAdapter("copyRight")
fun setCopyRight(view: TextView, copyRight: String?) {
    view.text = if (copyRight != null) view.context.getString(
        R.string.copy_right_format,
        copyRight
    ) else ""
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int?) {
    view.text = if (duration != null) getDuration(duration) else ""
}

@BindingAdapter("trackCount")
fun setTrackCount(view: TextView, trackCount: Int?) {
    view.text = if (trackCount != null) view.context.getString(
        R.string.track_count_format,
        trackCount
    ) else ""
}

@BindingAdapter("similarArtistName")
fun setSimilarArtistName(view: TextView, similarArtistName: String?) {
    view.text = if (similarArtistName != null) view.context.getString(
        R.string.top_release_by_artist_format,
        similarArtistName
    ) else ""
}

@BindingAdapter("bio")
fun setBio(view: TextView, bio: String?) {
    if (bio != null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            view.text = Html.fromHtml(bio, Html.FROM_HTML_MODE_LEGACY)
        } else {
            view.text = Html.fromHtml(bio)
        }
    } else {
        view.text = ""
    }
}

@BindingAdapter("artistName", "duration")
fun setArtistNameWithDuration(view: TextView, artistName: String?, duration: Int?) {
    view.text = if (artistName != null && duration != null) view.context.getString(
        R.string.artist_name_with_duration,
        artistName,
        getDuration(duration)
    ) else ""
}

@BindingAdapter("releaseType")
fun setReleaseTypeIcon(view: ImageView, releaseType: String?) {
    if (releaseType != null && releaseType == Constants.VIDEO_SINGLE) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("artistName", "date")
fun setArtistNameWithYear(view: TextView, artistName: String, date: String?) {
    view.text = if (date != null) view.context.getString(
        R.string.artist_name_with_year,
        artistName,
        getYearFromDate(date)
    ) else ""
}

@BindingAdapter("artistName", "date2")
fun setArtistNameWithYear2(view: TextView, artistName: String, date: String?) {
    view.text = if (date != null) view.context.getString(
        R.string.artist_name_with_year_2,
        artistName,
        getYearFromDate(date)
    ) else ""
}

@BindingAdapter("interval")
fun setSubscriptionInterval(view: TextView, interval: String?) {
    if (interval != null) {
        val day = interval.toInt()
        view.text = String.format(
            "%02d",
            day
        )
    } else {
        view.text = view.context.getString(R.string.free)
    }
}

@BindingAdapter("intervalUnit")
fun setSubscriptionIntervalUnit(view: TextView, interval: String?) {
    if (interval != null) {
        val day = interval.toInt()
        view.text =
            if (day > 1)
                view.context.getString(R.string.days).toUpperCase(Locale.getDefault()) else
                view.context.getString(R.string.day).toUpperCase(Locale.getDefault())
    } else {
        view.text = view.context.getString(R.string.trial_plan).toUpperCase(Locale.getDefault())
    }
}

fun getDuration(duration: Int): String? {
    val str: String
    val min = duration / 60
    val sec = duration % 60
    val secString = if (sec < 10) "0$sec" else sec
    str = "$min:$secString"
    return str
}

fun getYearFromDate(str: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var date: Date? = null
    return try {
        date = formatter.parse(str)
        DateFormat.format("yyyy", date).toString()
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }
}

fun getDate(str: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
    var date: Date? = null
    return try {
        date = formatter.parse(str)
        DateFormat.format("dd MMMM", date).toString()
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }
}