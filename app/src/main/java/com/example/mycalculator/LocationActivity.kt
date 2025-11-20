package com.example.mycalculator

import com.google.gson.Gson
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationActivity : AppCompatActivity() {

    val value: Int = 0
    val LOG_TAG: String = "LOCATION_ACTIVITY"
    private lateinit var bBackToMain: Button
    private val handler = android.os.Handler()
    private val locationUpdateInterval = 10000L
    private val locationRunnable = object : Runnable {
        override fun run() {
            getCurrentLocation()
            handler.postDelayed(this, locationUpdateInterval)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION= 100
    }

    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bBackToMain = findViewById<Button>(R.id.back_to_main)

        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        tvLat = findViewById(R.id.tv_lat)
        tvLon = findViewById(R.id.tv_lon)
        tvAlt = findViewById(R.id.tv_alt)
        tvTime = findViewById(R.id.tv_time)

    }

    override fun onResume() {
        super.onResume()

        bBackToMain.setOnClickListener({
            val backToMain = Intent(this, MainActivity::class.java)
            startActivity(backToMain)
        })

        handler.post(locationRunnable)

    }

    override fun onPause() {
        handler.removeCallbacks(locationRunnable)
        super.onPause()
    }

    private fun saveLocationToJsonFile(location: Location) {
        val gson = Gson()
        val locationData = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "altitude" to location.altitude,
            "time" to location.time
        )
        val jsonString = gson.toJson(locationData)

        val fileName = "location_data.json"
        try {
            openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Toast.makeText(this, "Location saved to JSON file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error writing JSON file", e)
        }
    }
    private fun getCurrentLocation(){

        if(checkPermissions()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                myFusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location: Location?=task.result
                    if(location == null){
                        Toast.makeText(applicationContext, "problems with signal", Toast.LENGTH_SHORT).show()
                    } else {
                        tvLat.setText(location.latitude.toString())
                        tvLon.setText(location.longitude.toString())
                        tvAlt.setText(location.altitude.toString())
                        tvTime.setText(location.time.toString())

                        // yo JSON
                        saveLocationToJsonFile(location)

                    }
                }

            } else{
                // open settings to enable location
                Toast.makeText(applicationContext, "Enable location in settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "location permission is not allowed");
            tvLat.setText("Permission is not granted")
            tvLon.setText("Permission is not granted")
            tvAlt.setText("Permission is not granted")
            tvTime.setText("Permission is not granted")
            requestPermissions()
        }

    }

    private fun requestPermissions() {
        Log.w(LOG_TAG, "requestPermissions()");
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermissions(): Boolean{
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Denied by user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}