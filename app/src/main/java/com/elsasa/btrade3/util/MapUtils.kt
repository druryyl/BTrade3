package com.elsasa.btrade3.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object MapUtils {
    fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "") {
        val uri = if (label.isNotEmpty()) {
            "geo:$latitude,$longitude?q=$latitude,$longitude($label)"
        } else {
            "geo:$latitude,$longitude"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Google Maps app not installed, try web version
            val webUri = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            context.startActivity(webIntent)
        }
    }
}