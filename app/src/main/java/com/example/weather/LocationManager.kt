package com.example.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun getLocation(callback: (String?, String?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null, null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                getCityAndCountry(location.latitude, location.longitude, callback)
            } else {
                callback(null, null)
            }
        }
    }

    private fun getCityAndCountry(latitude: Double, longitude: Double, callback: (String?, String?) -> Unit) {
        val geocoder = Geocoder(context, Locale.getDefault())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val city = addresses[0]?.locality
                        val country = addresses[0]?.countryName
                        callback(city, country)
                    } else {
                        callback(null, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null, null)
            }
        }
    }
}
