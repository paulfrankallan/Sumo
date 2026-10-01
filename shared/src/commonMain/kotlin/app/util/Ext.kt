package app.util

import androidx.navigation.NavController

fun NavController.popBackStackOrNavToRoute(route: String) {
    if (!popBackStack()) navigate(route)
}

fun Boolean?.isTrue() = this == true

fun String?.isNotNullOrEmpty() = !this.isNullOrEmpty()