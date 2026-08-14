package io.pnut.gamma.presentation.util

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.pnut.gamma.R

fun Fragment.navigateTo(fragment: Fragment, tag: String): Fragment? {
    return FragmentHelper.addFragment(requireContext(), fragment, tag)
}

object FragmentHelper {
    fun addFragment(
        context: Context,
        fragment: Fragment,
        tag: String,
        sharedElement: View? = null,
        transitionName: String? = null
    ): Fragment? {
        return addFragment(context, fragment, tag, createTransitionMap(sharedElement, transitionName))
    }

    fun addFragment(
        context: Context,
        fragment: Fragment,
        tag: String,
        transitionMap: Map<View, String>?
    ): Fragment? {
        val fm = getFragmentManagerFromContext(context) ?: return null
        return addFragment(fm, fragment, tag, transitionMap)
    }

    fun addFragment(
        fm: FragmentManager,
        fragment: Fragment,
        tag: String,
        sharedElement: View? = null,
        transitionName: String? = null
    ): Fragment? {
        return addFragment(fm, fragment, tag, createTransitionMap(sharedElement, transitionName))
    }

    private fun createTransitionMap(sharedElement: View?, transitionName: String?): Map<View, String> {
        val res = HashMap<View, String>()
        if (sharedElement != null && transitionName != null) {
            res[sharedElement] = transitionName
        }
        return res
    }

    private fun getFragmentManagerFromContext(context: Context): FragmentManager? {
        var c = context
        while (c is ContextWrapper) {
            if (c is AppCompatActivity) return c.supportFragmentManager
            c = c.baseContext
        }
        return null
    }

    fun addFragment(
        fm: FragmentManager,
        fragment: Fragment,
        tag: String,
        transitionMap: Map<View, String>?
    ): Fragment? {
        val containerId = R.id.container
        val foundFragment = fm.findFragmentById(containerId)
        if (foundFragment != null && (foundFragment == fragment || foundFragment.tag == tag)) return foundFragment
        val ft = fm
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left,
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )

        transitionMap?.forEach {
            val sharedElement = it.key
            val transitionName = it.value
            ft.addSharedElement(sharedElement, transitionName)
        }
        
        ft.replace(containerId, fragment, tag)
        ft.addToBackStack(tag)
        ft.commit()
        return null
    }

    fun backFragment(fm: FragmentManager?) {
        fm?.let {
            if (it.backStackEntryCount > 0) it.popBackStack()
        }
    }
}
