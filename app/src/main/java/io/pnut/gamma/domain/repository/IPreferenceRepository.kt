package io.pnut.gamma.domain.repository

import android.content.SharedPreferences
import io.pnut.gamma.domain.model.preference.ShapeOfAvatar
import io.pnut.gamma.presentation.util.ThemeColorUtil

interface IPreferenceRepository {
    fun onRegisterChangePreference(listener: SharedPreferences.OnSharedPreferenceChangeListener)
    fun onUnregisterChangePreference(listener: SharedPreferences.OnSharedPreferenceChangeListener)
    fun load()
    fun reload()
    val themeColor: ThemeColorUtil.ThemeColor
    val darkMode: ThemeColorUtil.DarkMode
    val darkModeStr: String
    val avatarSwipe: Boolean
    val loadingSize: Int
    val thresholdOfAutoPager: Int
    val unifiedStream: Boolean
    val shapeOfAvatar: ShapeOfAvatar
    val cacheSize: Int
    val hideDirectedPosts: Boolean
    val hideCopyMentions: Boolean
}