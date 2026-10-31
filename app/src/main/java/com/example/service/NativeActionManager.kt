package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import java.net.URLEncoder

enum class ActionType {
    OPEN_APP,
    OPEN_URL,
    SEARCH_WEB,
    CALCULATE,
    SEND_NOTIFICATION,
    CREATE_FILE,
    RUN_CODE
}

data class NativeAction(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val type: ActionType,
    val target: String,
    val isSensitive: Boolean = false
)

class NativeActionManager(private val context: Context) {

    val defaultSupportedActions = listOf(
        NativeAction(
            id = "youtube",
            name = "YouTube",
            description = "Watch videos, tutorials & search music",
            iconName = "youtube",
            type = ActionType.OPEN_APP,
            target = "com.google.android.youtube"
        ),
        NativeAction(
            id = "whatsapp",
            name = "WhatsApp",
            description = "Chat with contacts or send messages",
            iconName = "whatsapp",
            type = ActionType.OPEN_APP,
            target = "com.whatsapp"
        ),
        NativeAction(
            id = "gmail",
            name = "Gmail",
            description = "Read emails or compose new message",
            iconName = "gmail",
            type = ActionType.OPEN_APP,
            target = "com.google.android.gm"
        ),
        NativeAction(
            id = "maps",
            name = "Google Maps",
            description = "Navigation, search places & traffic directions",
            iconName = "maps",
            type = ActionType.OPEN_APP,
            target = "com.google.android.apps.maps"
        ),
        NativeAction(
            id = "telegram",
            name = "Telegram",
            description = "Open Telegram channels & conversations",
            iconName = "telegram",
            type = ActionType.OPEN_APP,
            target = "org.telegram.messenger"
        ),
        NativeAction(
            id = "chrome",
            name = "Chrome Browser",
            description = "Browse websites and internet resources",
            iconName = "chrome",
            type = ActionType.OPEN_APP,
            target = "com.android.chrome"
        ),
        NativeAction(
            id = "camera",
            name = "Camera",
            description = "Capture photo or scan documentation",
            iconName = "camera",
            type = ActionType.OPEN_APP,
            target = "camera_intent"
        ),
        NativeAction(
            id = "calculator",
            name = "Calculator",
            description = "Quick arithmetic & scientific computing",
            iconName = "calculator",
            type = ActionType.OPEN_APP,
            target = "calculator_intent"
        ),
        NativeAction(
            id = "calendar",
            name = "Calendar",
            description = "Manage schedule, meetings & events",
            iconName = "calendar",
            type = ActionType.OPEN_APP,
            target = "calendar_intent"
        ),
        NativeAction(
            id = "instagram",
            name = "Instagram",
            description = "Explore photos, feeds & stories",
            iconName = "instagram",
            type = ActionType.OPEN_APP,
            target = "com.instagram.android"
        )
    )

    fun executeAction(action: NativeAction): Result<String> {
        return try {
            when (action.id) {
                "camera" -> openCamera()
                "calculator" -> openCalculator()
                "calendar" -> openCalendar()
                "youtube" -> openUrlOrApp("https://www.youtube.com", "com.google.android.youtube")
                "whatsapp" -> openUrlOrApp("https://web.whatsapp.com", "com.whatsapp")
                "gmail" -> openUrlOrApp("https://mail.google.com", "com.google.android.gm")
                "maps" -> openUrlOrApp("https://maps.google.com", "com.google.android.apps.maps")
                "telegram" -> openUrlOrApp("https://web.telegram.org", "org.telegram.messenger")
                "chrome" -> openUrl("https://www.google.com")
                "instagram" -> openUrlOrApp("https://www.instagram.com", "com.instagram.android")
                else -> {
                    when (action.type) {
                        ActionType.OPEN_URL -> openUrl(action.target)
                        ActionType.OPEN_APP -> openAppByPackage(action.target)
                        ActionType.SEARCH_WEB -> {
                            val enc = URLEncoder.encode(action.target, "UTF-8")
                            openUrl("https://www.google.com/search?q=$enc")
                        }
                        else -> Result.success("Executed: ${action.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openUrl(url: String): Result<String> {
        return try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opened $validUrl")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun openAppByPackage(packageName: String): Result<String> {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Result.success("Launched $packageName")
        } else {
            // Fallback to Google Play Store or web link
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(storeIntent)
            Result.success("Opened Play Store for $packageName")
        }
    }

    private fun openUrlOrApp(webUrl: String, packageName: String): Result<String> {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Result.success("Launched application")
        } else {
            openUrl(webUrl)
        }
    }

    private fun openCamera(): Result<String> {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opened Camera")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun openCalculator(): Result<String> {
        return try {
            val intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opened Calculator")
        } catch (e: Exception) {
            openUrl("https://www.google.com/search?q=calculator")
        }
    }

    private fun openCalendar(): Result<String> {
        return try {
            val intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opened Calendar")
        } catch (e: Exception) {
            openUrl("https://calendar.google.com")
        }
    }
}
