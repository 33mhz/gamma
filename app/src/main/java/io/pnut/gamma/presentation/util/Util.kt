package io.pnut.gamma.presentation.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.appcompat.R as Rc
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.ColorUtils
import androidx.core.view.MenuItemCompat
import io.pnut.gamma.R
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import androidx.core.view.size

object Util {
    fun showKeyboard(view: View) {
        val window = view.context.findWindow() ?: return
        WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.ime())
    }

    fun hideKeyboard(view: View, callback: (() -> Unit)? = null) {
        val window = view.context.findWindow()
        if (window != null) {
            WindowInsetsControllerCompat(window, view).hide(WindowInsetsCompat.Type.ime())
        }

        if (callback != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                callback()
            }, 30)
        }
    }

    private fun Context.findWindow(): android.view.Window? {
        var context = this
        while (true) {
            if (context is android.app.Activity) return context.window
            if (context is android.content.ContextWrapper) {
                context = context.baseContext
            } else {
                break
            }
        }
        return null
    }

    interface DrawerContentFragment {
        val menuItemId: Int
    }

    fun getViewPositionOnScreen(view: View): Pair<Int, Int> {
        val pos = IntArray(2)
        view.getLocationOnScreen(pos)
        val cx = pos[0] + view.width / 2
        val cy = pos[1] + view.height / 2
        return Pair(cx, cy)
    }

    fun setTintForToolbarIcons(context: Context, menu: Menu) {
        val colorStateList =
            AppCompatResources.getColorStateList(context, R.color.toolbar_icon_tint)
        for (i in 0 until menu.size) {
            setTintForToolbarIcon(colorStateList, menu[i])
        }
    }

    fun setTintForToolbarIcon(colorStateList: ColorStateList, menuItem: MenuItem) {
        MenuItemCompat.setIconTintList(menuItem, colorStateList)
    }


    fun setTintForCheckableMenuItem(context: Context, menuItem: MenuItem) {
        when (menuItem.isChecked) {
            true -> {
                val color = getPrimaryColor(context)
                menuItem.icon?.setTint(color)
            }
            false -> {
                menuItem.icon?.clearColorFilter()
                val colorStateList =
                    AppCompatResources.getColorStateList(context, R.color.toolbar_icon_tint)
                setTintForToolbarIcon(colorStateList, menuItem)
            }
        }
    }

    fun openCustomTabUrl(context: Context, link: String) {
        try {
            val color = context.getColor(R.color.colorWindowBackground)
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .build()

            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setUrlBarHidingEnabled(true)
                .setDefaultColorSchemeParams(colorSchemeParams)
                .setStartAnimations(context, R.anim.slide_in_left, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_right, R.anim.slide_out_right)
                .build()
                .launchUrl(context, link.toUri())
        } catch (e: Exception) {
            // TODO: to improve error handling
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun getPrimaryColor(context: Context) = getAttributeValue(context, Rc.attr.colorPrimary)
    fun getAccentColor(context: Context) = getAttributeValue(context, Rc.attr.colorAccent)

    private fun getAttributeValue(context: Context, @AttrRes resourceId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resourceId, typedValue, true)
        return typedValue.data
    }

    fun getPrimaryColorDark(context: Context) =
        ColorUtils.blendARGB(getPrimaryColor(context), Color.BLACK, 0.1f)

    fun getWindowBackgroundColor(context: Context): Int =
        getAttributeValue(context, android.R.attr.windowBackground)

    fun getVisibility(b: Boolean) = if (b) View.VISIBLE else View.GONE
}
