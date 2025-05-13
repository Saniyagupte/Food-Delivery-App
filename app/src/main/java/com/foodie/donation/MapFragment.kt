package com.foodie.donation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentMapBinding
import android.util.Log
import androidx.appcompat.app.AlertDialog

class MapFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "MapFragment"
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")
    private var googleMap: GoogleMap? = null
    private var mapLoadError = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    // Permission request launcher (replaces onRequestPermissionsResult)
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val locationGranted = permissions.entries.any { 
                it.key.contains("LOCATION") && it.value 
            }
            
            if (locationGranted) {
                Log.d(TAG, "Location permission granted")
                // Permission was granted
                googleMap?.let { map ->
                    try {
                        map.isMyLocationEnabled = true
                        map.uiSettings.isMyLocationButtonEnabled = true
                        
                        // Try to get user's location and zoom to it
                        requestLocationAndZoomToUser()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Error enabling location: ${e.localizedMessage}")
                        Toast.makeText(requireContext(), "Error enabling location: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d(TAG, "Location permission denied")
                // Permission was denied
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        
        // Ensure Firebase Authentication
        ensureFirebaseAuth()
        
        // Check if Google Play Services is available
        if (isGooglePlayServicesAvailable()) {
            // Initialize the map
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
            
            // My location button
            binding.fabMyLocation.setOnClickListener {
                requestLocationAndZoomToUser()
            }
            
            // Refresh donations button
            binding.fabRefresh.setOnClickListener {
                Toast.makeText(requireContext(), "Refreshing donation locations...", Toast.LENGTH_SHORT).show()
                ensureFirebaseAuth() // Make sure we're authenticated before loading
            }
            
            // Handle retry for API key issues
            binding.retryButton.setOnClickListener {
                binding.errorContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                mapFragment?.getMapAsync(this)
            }

            // Add a test location button for debugging
            if (isEmulator()) {
                binding.fabDebug?.visibility = View.VISIBLE
                binding.fabDebug?.setOnClickListener {
                    moveToTestLocation()
                }
            }
        } else {
            // Show error message
            showErrorMessage(
                "Google Play Services Not Available",
                "Maps cannot be displayed because Google Play Services is not available on this device."
            )
        }
    }
    
    /**
     * Ensure Firebase Auth is initialized and user is signed in (anonymously if needed)
     */
    private fun ensureFirebaseAuth() {
        // If user is already signed in, load donations
        if (auth.currentUser != null) {
            Log.d(TAG, "User already authenticated: ${auth.currentUser?.uid}")
            loadDonationLocations()
            return
        }
        
        // Otherwise, sign in anonymously
        binding.progressBar.visibility = View.VISIBLE
        
        Log.d(TAG, "Attempting anonymous sign-in")
        auth.signInAnonymously()
            .addOnSuccessListener {
                Log.d(TAG, "Anonymous auth success: ${auth.currentUser?.uid}")
                loadDonationLocations()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Anonymous auth failed: ${e.localizedMessage}")
                binding.progressBar.visibility = View.GONE
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Authentication Error")
                    .setMessage("Could not authenticate with Firebase. Using sample data instead.")
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
                
                // Fall back to sample data
                showSampleDonations()
            }
    }
    
    private fun requestLocationAndZoomToUser() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                // Permissions are granted, get the location
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                
                Log.d(TAG, "Requesting location from FusedLocationProvider")
                
                // Get the user's current location and move camera
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            Log.d(TAG, "Location received: ${location.latitude}, ${location.longitude}")
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                            )
                            Toast.makeText(requireContext(), "Found your location", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.w(TAG, "Location is null - using fallback location")
                            // If in emulator or location is null, use a default location
                            val defaultLocation = LatLng(37.7749, -122.4194) // San Francisco
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f)
                            )
                            Toast.makeText(requireContext(), "Using default location", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error getting location: ${e.message}")
                        // Fallback to default location on error
                        val defaultLocation = LatLng(37.7749, -122.4194) // San Francisco
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f)
                        )
                        Toast.makeText(requireContext(), "Using default location", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: SecurityException) {
                Log.e(TAG, "Error enabling location: ${e.message}")
                Toast.makeText(requireContext(), "Location permission error", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request location permissions using the modern approach
            Log.d(TAG, "Location permission not granted, requesting...")
            
            // Launch the permission request
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    private fun showErrorMessage(title: String, message: String) {
        mapLoadError = true
        binding.progressBar.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE
        binding.errorTitle.text = title
        binding.errorMessage.text = message
    }
    
    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 1000)?.show()
            } else {
                Log.e(TAG, "This device is not supported for Google Play Services")
            }
            return false
        }
        
        return true
    }
    
    override fun onMapReady(map: GoogleMap) {
        try {
            googleMap = map
            
            // Apply custom map style if needed
            try {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            } catch (e: Exception) {
                Log.e(TAG, "Can't find map style: ${e.localizedMessage}")
            }
            
            // Only enable location features if permission is granted
            if (LocationPermissionHelper.checkPermission(requireContext())) {
                try {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                    
                    // Get initial location
                    requestLocationAndZoomToUser()
                } catch (e: SecurityException) {
                    // Handle permission not granted
                    Log.e(TAG, "Location permission not granted: ${e.localizedMessage}")
                    Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Show donation locations on the map
            loadDonationLocations()
            
            // Hide loading indicator
            binding.progressBar.visibility = View.GONE
            binding.errorContainer.visibility = View.GONE
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading map: ${e.localizedMessage}")
            
            // Check specifically for API key issues
            if (e.localizedMessage?.contains("API") == true || e.localizedMessage?.contains("authorization") == true) {
                showErrorMessage(
                    "Maps API Key Error",
                    "The Google Maps API key is not configured correctly. Please make sure the API key is enabled for Maps SDK for Android in the Google Cloud Console."
                )
            } else {
                showErrorMessage("Map Error", "Error loading map: ${e.localizedMessage}")
            }
            
            e.printStackTrace()
        }
    }
    
    /**
     * Load donation locations from Firestore and display them on the map
     */
    private fun loadDonationLocations() {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        
        try {
            // Make sure we're authenticated
            if (auth.currentUser == null) {
                Log.w(TAG, "No authenticated user, attempting to authenticate")
                ensureFirebaseAuth()
                return
            }
            
            Log.d(TAG, "Loading donations with user: ${auth.currentUser?.uid}")
            
            // Query for donations collection
            firestore.collection("donations")
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG, "Fetched ${documents.size()} donation locations")
                    
                    if (documents.isEmpty) {
                        Toast.makeText(requireContext(), "No donations available to display", Toast.LENGTH_SHORT).show()
                    } else {
                        // Clear existing markers (except user's location)
                        googleMap?.clear()
                        
                        // Add a marker for each donation
                        for (document in documents) {
                            try {
                                val donation = document.data
                                val address = donation["address"] as? String
                                val foodType = donation["foodType"] as? String ?: "Food Donation"
                                val status = donation["status"] as? String ?: "pending"
                                val quantity = donation["quantity"] as? String ?: ""
                                
                                // Only proceed if we have an address
                                if (!address.isNullOrEmpty()) {
                                    // Geocode the address in a background thread
                                    Thread {
                                        try {
                                            val geocoder = android.location.Geocoder(requireContext())
                                            val locations = geocoder.getFromLocationName(address, 1)
                                            
                                            // Return to main thread to update UI
                                            requireActivity().runOnUiThread {
                                                if (locations != null && locations.isNotEmpty()) {
                                                    val location = locations[0]
                                                    val latLng = LatLng(location.latitude, location.longitude)
                                                    
                                                    // Add marker with custom icon based on status
                                                    val marker = googleMap?.addMarker(
                                                        MarkerOptions()
                                                            .position(latLng)
                                                            .title(foodType)
                                                            .snippet("$quantity kg - Status: ${status.capitalize()}")
                                                    )
                                                    
                                                    // Store document ID with marker for potential click handling
                                                    marker?.tag = document.id
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error geocoding address $address: ${e.localizedMessage}")
                                            requireActivity().runOnUiThread {
                                                // Only show toast if we're still in the fragment
                                                if (isAdded) {
                                                    Toast.makeText(requireContext(), 
                                                        "Error finding location for address: $address", 
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }.start()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing donation: ${e.localizedMessage}")
                            }
                        }
                        
                        // Set up marker click listener
                        googleMap?.setOnMarkerClickListener { marker ->
                            // Show info window
                            marker.showInfoWindow()
                            
                            // Move camera to the marker
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(marker.position, 15f)
                            )
                            
                            // Get the document ID from the marker tag and show donation details
                            (marker.tag as? String)?.let { donationId ->
                                showDonationDetails(donationId)
                            }
                            
                            // Return true to indicate we've handled the event
                            true
                        }
                    }
                    
                    // Hide loading indicator regardless of result
                    binding.progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching donations: ${e.localizedMessage}")
                    
                    // Check if it's a permissions error
                    val errorMessage = if (e.message?.contains("permission", ignoreCase = true) == true) {
                        "Permission denied when accessing donations. Please check your internet connection and try again."
                    } else {
                        "Error loading donation locations: ${e.localizedMessage}"
                    }
                    
                    // Show an error dialog with more details
                    if (isAdded) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Error Loading Donations")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Show Sample Data") { _, _ ->
                                // Show sample data instead
                                showSampleDonations()
                            }
                            .create()
                            .show()
                    }
                    
                    binding.progressBar.visibility = View.GONE
                }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading donations: ${e.localizedMessage}")
            binding.progressBar.visibility = View.GONE
            
            // Fall back to sample data
            showSampleDonations()
        }
    }
    
    /**
     * Display sample donation locations for testing or when Firestore is not available
     */
    private fun showSampleDonations() {
        try {
            googleMap?.clear()
            
            // Sample locations
            val sampleLocations = listOf(
                Triple(LatLng(37.7749, -122.4194), "Bread and Vegetables", "123 Market St, San Francisco"),
                Triple(LatLng(40.7128, -74.0060), "Canned Goods", "456 Broadway, New York"),
                Triple(LatLng(34.0522, -118.2437), "Fresh Fruits", "789 Main St, Los Angeles"),
                Triple(LatLng(41.8781, -87.6298), "Packaged Meals", "101 State St, Chicago")
            )
            
            // Add sample markers
            sampleLocations.forEachIndexed { index, (position, foodType, address) ->
                val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(foodType)
                        .snippet("Available at: $address")
                )
                
                // Use index as tag for sample data
                marker?.tag = "sample_$index"
            }
            
            // Zoom to first sample location
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(sampleLocations.first().first, 12f)
            )
            
            Toast.makeText(requireContext(), 
                "Showing available donations", 
                Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing donation locations: ${e.localizedMessage}")
        }
    }

    /**
     * Capitalization extension function
     */
    private fun String.capitalize(): String {
        return this.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }

    /**
     * Move the map to a test location for debugging
     */
    private fun moveToTestLocation() {
        val testLocations = listOf(
            LatLng(37.7749, -122.4194), // San Francisco
            LatLng(40.7128, -74.0060),  // New York
            LatLng(34.0522, -118.2437), // Los Angeles
            LatLng(51.5074, -0.1278),   // London
            LatLng(35.6762, 139.6503)   // Tokyo
        )
        
        val random = java.util.Random()
        val testLocation = testLocations[random.nextInt(testLocations.size)]
        
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(testLocation, 15f)
        )
        
        Toast.makeText(
            requireContext(), 
            "Moved to test location: ${testLocation.latitude}, ${testLocation.longitude}", 
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Check if the app is running on an emulator
     */
    private fun isEmulator(): Boolean {
        return (android.os.Build.MANUFACTURER.contains("Google") && 
                android.os.Build.DEVICE.contains("sdk")) ||
               android.os.Build.FINGERPRINT.startsWith("generic") ||
               android.os.Build.FINGERPRINT.startsWith("unknown") ||
               android.os.Build.MODEL.contains("google_sdk") ||
               android.os.Build.MODEL.contains("Emulator") ||
               android.os.Build.MODEL.contains("Android SDK built for x86") ||
               android.os.Build.HARDWARE.contains("goldfish") ||
               android.os.Build.HARDWARE.contains("ranchu") ||
               android.os.Build.PRODUCT.contains("sdk") ||
               android.os.Build.PRODUCT.contains("google_sdk") ||
               android.os.Build.PRODUCT.contains("sdk_google") ||
               android.os.Build.PRODUCT.contains("sdk_x86") ||
               android.os.Build.PRODUCT.contains("vbox86p") ||
               android.os.Build.PRODUCT.contains("emulator") ||
               android.os.Build.PRODUCT.contains("simulator")
    }

    /**
     * Deprecated method - keeping for compatibility with older Android versions
     * but marked as deprecated to match the Android framework.
     */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        // We're using the new permission API with permissionLauncher,
        // but keeping this for backward compatibility
        Log.d(TAG, "Using legacy permission result handler")
        
        if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && 
                (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) || 
                 permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                
                // Check if any location permission was granted
                val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
                
                if (granted) {
                    // Permission was granted
                    googleMap?.let { map ->
                        try {
                            map.isMyLocationEnabled = true
                            map.uiSettings.isMyLocationButtonEnabled = true
                            
                            // Try to get user's location and zoom to it
                            requestLocationAndZoomToUser()
                        } catch (e: SecurityException) {
                            Log.e(TAG, "Error enabling location: ${e.localizedMessage}")
                            Toast.makeText(requireContext(), "Error enabling location: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Permission was denied
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Show detailed information about a donation when its marker is clicked
     */
    private fun showDonationDetails(donationId: String) {
        // Check if this is a sample donation (for when Firestore isn't available)
        if (donationId.startsWith("sample_")) {
            val sampleLocations = listOf(
                Triple(LatLng(37.7749, -122.4194), "Bread and Vegetables", "123 Market St, San Francisco"),
                Triple(LatLng(40.7128, -74.0060), "Canned Goods", "456 Broadway, New York"),
                Triple(LatLng(34.0522, -118.2437), "Fresh Fruits", "789 Main St, Los Angeles"),
                Triple(LatLng(41.8781, -87.6298), "Packaged Meals", "101 State St, Chicago")
            )
            
            try {
                // Extract the index from the sample_X id
                val index = donationId.substring(7).toInt()
                if (index >= 0 && index < sampleLocations.size) {
                    val (_, foodType, address) = sampleLocations[index]
                    
                    AlertDialog.Builder(requireContext())
                        .setTitle(foodType)
                        .setMessage("""
                            Type: $foodType
                            Quantity: 5 kg
                            Status: Available
                            Address: $address
                            
                            Description:
                            Freshly prepared donation available for pickup. Please contact the donation center for more details.
                        """.trimIndent())
                        .setPositiveButton("Close", null)
                        .create()
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error showing donation details: ${e.localizedMessage}")
            }
            return
        }
        
        // Make sure we're authenticated
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Not authenticated. Cannot load details.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // This is a real donation from Firestore
        Log.d(TAG, "Loading donation details for ID: $donationId")
        
        firestore.collection("donations").document(donationId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    
                    val foodType = data?.get("foodType") as? String ?: "Unknown food"
                    val quantity = data?.get("quantity") as? String ?: "Unknown quantity"
                    val address = data?.get("address") as? String ?: "Unknown location"
                    val status = data?.get("status") as? String ?: "pending"
                    val description = data?.get("description") as? String ?: "No description"
                    
                    // Build and show a dialog with the donation details
                    AlertDialog.Builder(requireContext())
                        .setTitle("$foodType Donation")
                        .setMessage("""
                            Type: $foodType
                            Quantity: $quantity kg
                            Status: ${status.capitalize()}
                            Address: $address
                            
                            Description:
                            $description
                        """.trimIndent())
                        .setPositiveButton("Close", null)
                        .create()
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Donation not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading donation details: ${e.localizedMessage}")
                Toast.makeText(
                    requireContext(),
                    "Error loading donation details",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null
        _binding = null
    }
} 
