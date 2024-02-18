package com.adentech.rcvr.extensions

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

fun Fragment.initMenu(
    @MenuRes menuResourceId: Int,
    onMenuItemSelected: (menuItem: MenuItem) -> Boolean,
    onCreateMenu: ((menu: Menu) -> Unit)? = null,
) {
    activity?.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(menuResourceId, menu)
            onCreateMenu?.invoke(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return onMenuItemSelected.invoke(menuItem)
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

fun Fragment.showDialog(dialog: DialogFragment) {
    val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()

    fragmentTransaction?.addToBackStack(null)

    fragmentTransaction?.let {
        dialog.show(it, this.javaClass.simpleName)
    }
}

fun Fragment.handleOnBackPressed(block: () -> Unit) {
    activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            block.invoke()
        }
    })
}

fun Fragment.navigate(navDirections: NavDirections) {
    findNavController().apply {
        currentDestination?.getAction(navDirections.actionId)?.run {
            navigate(navDirections.actionId, navDirections.arguments)
        }
    }
}
fun Fragment.popBack(@IdRes destinationId: Int, inclusive: Boolean = false) {
    findNavController().popBackStack(destinationId, inclusive)
}

fun Fragment.popBack() {
    if (!findNavController().popBackStack()) {
        activity?.finish()
    }
}