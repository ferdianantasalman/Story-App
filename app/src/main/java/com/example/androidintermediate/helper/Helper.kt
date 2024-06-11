package com.example.androidintermediate.helper

import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.androidintermediate.R
import com.example.androidintermediate.views.components.RecentStoryWidget
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object Helper {
    private var defaultDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

    private const val timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val simpleDateFormat = "dd MMM yyyy HH.mm"

    @SuppressLint("ConstantLocale")
    val simpleDate = SimpleDateFormat(simpleDateFormat, Locale.getDefault())

    fun notifyGivePermission(context: Context, message: String) {
        val dialog = dialogInfoBuilder(context, message)
        val button = dialog.findViewById<Button>(R.id.button_ok)
        button.setOnClickListener {
            dialog.dismiss()
            openSettingPermission(context)
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun openSettingPermission(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }

    fun getCurrentDateString(): String = defaultDate.format(getCurrentDate())

    private fun getCurrentDate(): Date {
        return Date()
    }

    @SuppressLint("ConstantLocale")
    val currentTimestamp: String = SimpleDateFormat(
        "ddMMyySSSSS",
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    private fun getSimpleDate(date: Date): String = simpleDate.format(date)

    private fun parseSimpleDate(dateValue: String): Date {
        return defaultDate.parse(dateValue) as Date
    }

    fun getSimpleDateString(dateValue: String): String = getSimpleDate(parseSimpleDate(dateValue))

    private fun parseUTCDate(timestamp: String): Date {
        return try {
            val formatter = SimpleDateFormat(timestampFormat, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(timestamp) as Date
        } catch (e: ParseException) {
            getCurrentDate()
        }
    }

    fun getUploadStoryTime(timestamp: String): String {
        val date: Date = parseUTCDate(timestamp)
        return getSimpleDate(date)
    }

    fun getTimelineUpload(context: Context, timestamp: String): String {
        val currentTime = getCurrentDate()
        val uploadTime = parseUTCDate(timestamp)
        val diff: Long = currentTime.time - uploadTime.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val label = when (minutes.toInt()) {
            0 -> "$seconds ${context.getString(R.string.text_seconds_ago)}"
            in 1..59 -> "$minutes ${context.getString(R.string.text_minutes_ago)}"
            in 60..1440 -> "$hours ${context.getString(R.string.text_hours_ago)}"
            else -> "$days ${context.getString(R.string.text_days_ago)}"
        }
        return label
    }

    fun showDialogPreviewImage(
        context: Context,
        image: Bitmap,
        path: String
    ) {
        val dialog = Dialog(context)
        dialog.setCancelable(true)
        dialog.window!!.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.windowAnimations = android.R.transition.slide_bottom
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setContentView(R.layout.custom_dialog_image_preview)
        val tvPath = dialog.findViewById<TextView>(R.id.image_path)
        tvPath.text = path
        val imageContainer = dialog.findViewById<ImageView>(R.id.image_preview)
        imageContainer.setImageBitmap(image)
        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close_preview)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun dialogInfoBuilder(
        context: Context,
        message: String,
        alignment: Int = Gravity.CENTER
    ): Dialog {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.window!!.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.windowAnimations = android.R.transition.fade
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setContentView(R.layout.custom_dialog)
        val tvMessage = dialog.findViewById<TextView>(R.id.message)
        when (alignment) {
            Gravity.CENTER -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER
            Gravity.START -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            Gravity.END -> tvMessage.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        tvMessage.text = message
        return dialog
    }

    fun showDialogInfo(
        context: Context,
        message: String,
        alignment: Int = Gravity.CENTER
    ) {
        val dialog = dialogInfoBuilder(context, message, alignment)
        val btnOk = dialog.findViewById<Button>(R.id.button_ok)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(currentTimestamp, ".jpg", storageDir)
    }

    fun createFile(application: Application): File {
        val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
            File(it, "story").apply { mkdirs() }
        }
        val outputDirectory = if (
            mediaDir != null && mediaDir.exists()
        ) mediaDir else application.filesDir

        return File(outputDirectory, "STORY-$currentTimestamp.jpg")
    }

    fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)
        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()
        return myFile
    }

    fun loadImageFromStorage(path: String): Bitmap? {
        val imgFile = File(path)
        return if (imgFile.exists()) {
            BitmapFactory.decodeFile(imgFile.absolutePath)
        } else null
    }

    /* load BITMAP from string URL */
    fun bitmapFromURL(context: Context, urlString: String): Bitmap {
        return try {
            /* allow access content from URL internet */
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            /* fetch image data from URL */
            val url = URL(urlString)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            BitmapFactory.decodeResource(context.resources, R.drawable.avatar)
        }
    }

    fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
        val matrix = Matrix()
        return if (isBackCamera) {
            matrix.postRotate(0f)
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        } else {
            matrix.postRotate(0f)
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        }
    }

    fun resizeBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        /* init matrix to resize bitmap */
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        /* recreate new bitmap as new defined size */
        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }

    fun parseAddressLocation(
        context: Context,
        lat: Double,
        lon: Double
    ): String {
        val geocoder = Geocoder(context)
        val geoLocation =
            geocoder.getFromLocation(lat, lon, 1)
        return if (geoLocation!!.size > 0) {
            val location = geoLocation!![0]
            val fullAddress = location.getAddressLine(0)
            StringBuilder("📌 ")
                .append(fullAddress).toString()
        } else {
            "📌 Location Unknown"
        }
    }

    fun updateWidgetData(context: Context) {
        Log.i("TEST_WIDGET", "Requested update data")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids: IntArray = appWidgetManager.getAppWidgetIds(
            ComponentName(context, RecentStoryWidget::class.java)
        )

        /* if widget update requested -> refresh widget data */
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
    }
}