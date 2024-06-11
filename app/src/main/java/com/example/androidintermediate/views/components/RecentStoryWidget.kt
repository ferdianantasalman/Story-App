package com.example.androidintermediate.views.components

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.os.bundleOf
import com.example.androidintermediate.R
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.activity.DetailActivity
import com.example.androidintermediate.views.activity.SplashActivity

class RecentStoryWidget : AppWidgetProvider() {
    companion object {
        const val ITEMS_CLICK = "ITEMS_CLICK_FROM_LIST"
        const val WIDGET_TITLE_CLICK = "USER_CLICK_WIDGET_TITLE"
        const val WIDGET_REFRESH_CLICK = "USER_REFRESH_WIDGET_DATA"
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val views = RemoteViews(context.packageName, R.layout.widget_recent_story)

        initStackItems(views, context, appWidgetId)
        initClickLabel(views, context)
        initClickRefresh(views, context)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun initStackItems(views: RemoteViews, context: Context, appWidgetId: Int) {
        val clickIntent = Intent(context, RecentStoryWidget::class.java)
        clickIntent.action = ITEMS_CLICK
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, clickIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else 0
        )
        views.setPendingIntentTemplate(R.id.stack_view, pendingIntent)
        views.setEmptyView(R.id.stack_view, R.id.stack_view)
    }

    private fun initClickLabel(views: RemoteViews, context: Context) {
        val openAppIntent = Intent(context, RecentStoryWidget::class.java)
        openAppIntent.action = WIDGET_TITLE_CLICK
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, openAppIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else 0
        )
        views.setOnClickPendingIntent(R.id.label_appName, pendingIntent)
    }

    private fun initClickRefresh(views: RemoteViews, context: Context) {
        val refreshIntent = Intent(context, RecentStoryWidget::class.java)
        refreshIntent.action = WIDGET_REFRESH_CLICK
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else 0
        )
        views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null) {
            when (intent.action) {
                ITEMS_CLICK -> {
                    val bundle = intent.extras
                    bundle?.let { params ->
                        val detailIntent = Intent(context, DetailActivity::class.java)
                        detailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        val extras = bundleOf(
                            Constant.StoryDetail.UserName.name to params.get(Constant.StoryDetail.UserName.name),
                            Constant.StoryDetail.ImageURL.name to params.get(Constant.StoryDetail.ImageURL.name),
                            Constant.StoryDetail.Longitude.name to params.get(Constant.StoryDetail.Longitude.name),
                            Constant.StoryDetail.Latitude.name to params.get(Constant.StoryDetail.Latitude.name),
                            Constant.StoryDetail.ContentDescription.name to params.get(Constant.StoryDetail.ContentDescription.name),
                            Constant.StoryDetail.UploadTime.name to params.get(Constant.StoryDetail.UploadTime.name),
                        )
                        detailIntent.putExtras(extras)
                        context.startActivity(detailIntent)
                    }
                }
                WIDGET_REFRESH_CLICK -> {
                    Handler(context.mainLooper).post {
                        Toast.makeText(context, "Data Widget Dimuat", Toast.LENGTH_SHORT).show()
                        Helper.updateWidgetData(context)
                    }
                }
                WIDGET_TITLE_CLICK -> {
                    val openAppIntent = Intent(context, SplashActivity::class.java)
                    openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(openAppIntent)
                }
            }
        }
    }
}