package io.pnut.gamma.presentation.util

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ThemeColorUtilTest {

    @Test
    fun testDarkModeValues() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeColorUtil.DarkMode.FollowSystem.value)
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeColorUtil.DarkMode.Off.value)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeColorUtil.DarkMode.On.value)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val expectedAutoValue = if (currentHour >= 18 || currentHour < 6) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        assertEquals(expectedAutoValue, ThemeColorUtil.DarkMode.Auto.value)
    }
}
