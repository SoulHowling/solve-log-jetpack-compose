package com.soulhowling.solvelog_jetpackcompose.utils

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class NotificationUtilsTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Test
    fun channelId_constantsAreCorrect() {
        assertEquals("stopwatch_channel", NotificationUtils.STOPWATCH_CHANNEL_ID)
        assertEquals("timer_channel", NotificationUtils.TIMER_CHANNEL_ID)
    }

    @Test
    fun createNotificationChannel_shouldCreateChannelWithCorrectParameters() {
        val channelId = "test_channel"
        val name = "Test Name"
        val description = "Test Description"

        NotificationUtils.createNotificationChannel(
            context,
            channelId,
            name,
            description,
            NotificationManager.IMPORTANCE_HIGH
        )

        val channel = notificationManager.getNotificationChannel(channelId)
        assertNotNull(channel)
        assertEquals(name, channel.name.toString())
        assertEquals(description, channel.description)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
    }

    @Test
    fun getBaseNotificationBuilder_shouldSetCorrectProperties() {
        val channelId = "test_channel"
        val title = "Test Title"
        val content = "Test Content"

        val builder = NotificationUtils.getBaseNotificationBuilder(
            context,
            channelId,
            title,
            content
        )

        val notification = builder.build()

        assertEquals(title, notification.extras.getCharSequence("android.title").toString())
        assertEquals(content, notification.extras.getCharSequence("android.text").toString())
        assertEquals(channelId, notification.channelId)
    }

    @Test
    fun showNotification_shouldTriggerNotifyOnNotificationManager() {
        val notificationId = 123
        val channelId = "test_channel"

        NotificationUtils.showNotification(
            context,
            notificationId,
            channelId,
            "Title",
            "Content"
        )

        val activeNotifications = notificationManager.activeNotifications
        val notification = activeNotifications.find { it.id == notificationId }
        assertNotNull("Notification with ID $notificationId should be present", notification)
        assertEquals(channelId, notification?.notification?.channelId)
    }
}
