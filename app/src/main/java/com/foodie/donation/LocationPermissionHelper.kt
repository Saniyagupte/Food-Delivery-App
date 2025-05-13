package com.foodie.donation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object LocationPermissionHelper {
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    
    fun checkPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Request location permissions using the legacy approach.
     * This method is kept for backward compatibility but marked as deprecated.
     * 
     * Use the ActivityResultLauncher approach instead:
     * ```
     * private val permissionLauncher = registerForActivityResult(
     *     ActivityResultContracts.RequestMultiplePermissions()
     * ) { permissions ->
     *     // handle permission results
     * }
     * ```
     */
    @Deprecated("This method uses the deprecated permission request approach. Use ActivityResultLauncher instead.")
    fun requestPermission(fragment: Fragment) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                fragment.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Show explanation to the user about why we need this permission
            AlertDialog.Builder(fragment.requireContext())
                .setTitle("Location Permission Needed")
                .setMessage("This app needs location permission to show your location on the map and display nearby donation locations.")
                .setPositiveButton("OK") { _, _ ->
                    fragment.requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE)
                }
                .create()
                .show()
        } else {
            // No explanation needed, request the permission
            fragment.requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    
    /**
     * Handle the result of a permission request using the legacy approach.
     * This method is kept for backward compatibility but marked as deprecated.
     */
    @Deprecated("This method handles results from the deprecated permission request approach. Use ActivityResultLauncher instead.")
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if any of the location permissions were granted
            val fineLocationGranted = permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
            
            val coarseLocationGranted = permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults.getOrNull(permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED
            
            if (fineLocationGranted || coarseLocationGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
} 