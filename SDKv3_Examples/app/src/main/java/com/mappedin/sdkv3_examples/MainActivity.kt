package com.mappedin.sdkv3_examples

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.indooratlas.android.sdk.IALocation
import com.indooratlas.android.sdk.IALocationListener
import com.indooratlas.android.sdk.IALocationManager
import com.indooratlas.android.sdk.IALocationRequest
import com.mappedin.sdk.listeners.MPIMapViewListener
import com.mappedin.sdk.models.*
import com.mappedin.sdk.web.MPIOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MainActivity : AppCompatActivity() {
    var sortedMaps: List<MPIMap>? = null
    var blueDot: MPIBlueDot? = null
    var selectedPolygon: MPINavigatable.MPIPolygon? = null
    private final val CODE_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val neededPermissions = arrayOf<String>(
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS)

        increaseFloor.setOnClickListener {
            changeMap(true)
        }

        decreaseFloor.setOnClickListener {
            changeMap(false)
        }

        closeButton.setOnClickListener {
            clearPolygon()
        }

        directionsButton.setOnClickListener {
            if (selectedPolygon != null && blueDot?.nearestNode != null) {
                //Get directions to selected polygon from users nearest node
                mapView.getDirections(selectedPolygon!!, blueDot?.nearestNode!!, true) { directions ->
                    directions?.path?.let { path ->
                        //Remove all paths on map before drawing new path from the directions
                        mapView.removeAllPaths() {
                            mapView.drawPath(path, MPIOptions.Path(drawDuration = 0.0, pulseIterations = 0.0))
                        }
                    }
                }
            }
        }

        //Set up MPIMapViewListener for MPIMapView events
        mapView.listener = object : MPIMapViewListener {
            override fun onDataLoaded(data: MPIData) {
                println("MPIData: " + Json.encodeToString(data))
                sortedMaps = data.maps.sortedBy{it.elevation}

                //Enable blue dot, does not appear until updatePosition is called with proper coordinates
                mapView.enableBlueDot()

                mapView.venueData?.polygons?.forEach {
                    if (it.locations.isNullOrEmpty()) {
                        mapView.addInteractivePolygon(it)
                    }
                }
            }
            override fun onMapChanged(map: MPIMap) {
                runOnUiThread {
                    supportActionBar?.title = map.name
                }
                println("MPIMap Changed: " + Json.encodeToString(map))
            }
            override fun onPolygonClicked(polygon: MPINavigatable.MPIPolygon) {
                println("MPIPolygon Clicked:" + Json.encodeToString(polygon))
                runOnUiThread {
                    selectPolygon(polygon)
                }
            }

            override fun onBlueDotUpdated(blueDot: MPIBlueDot) {
                this@MainActivity.blueDot = blueDot
                nearestNode.text = "BlueDot Nearest Node: " + (blueDot.nearestNode?.id ?: "N/A")
            }

            override fun onNothingClicked() {
                runOnUiThread {
                    clearPolygon()
                }
            }

            override fun onFirstMapLoaded() {
            }
        }

        //Load venue with credentials, if using proxy pass in MPIOptions.Init(noAuth = true, venue="venue_name", baseUrl="proxy_url")
        mapView.loadVenue(MPIOptions.Init("5eab30aa91b055001a68e996", "RJyRXKcryCMy4erZqqCbuB1NbR66QTGNXVE0x3Pg6oCIlUR1", "mappedin-demo-mall"))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.any { it == PackageManager.PERMISSION_DENIED}) return

        var mLocationManager = IALocationManager.create(this);

        val mIALocationListener: IALocationListener = object : IALocationListener {
            // Called when the location has changed.
            override fun onLocationChanged(location: IALocation) {
                val position = MPIPosition(0.0, MPIPosition.MPICoordinates(location.latitude,
                        location.longitude, location.accuracy.toDouble(),location.floorLevel), "n","a")
                mapView.updatePosition(position)
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                //TODO("Not yet implemented")
            }
        }

        mLocationManager.requestLocationUpdates(IALocationRequest.create(), mIALocationListener);

    }

    fun clearPolygon() {
        selectedPolygon = null
        locationView.visibility = View.GONE
        mapView.clearAllPolygonColors()
    }

    fun selectPolygon(polygon: MPINavigatable.MPIPolygon) {
        polygon.locations?.firstOrNull()?.let {

            selectedPolygon = polygon
            locationView.visibility = View.VISIBLE

            mapView.clearAllPolygonColors {
                mapView.setPolygonColor(polygon.id, "blue")
            }
            mapView.focusOn(MPIOptions.Focus(polygons=listOf(polygon)))

            locationTitle.text = it.name
            locationDescription.text = it.description
            Glide
                .with(this@MainActivity)
                .load(it.logo?.original)
                .centerCrop()
                .into(locationImage);
        }
    }

    fun changeMap(isIncrementing: Boolean = true) {
        mapView.currentMap?.let {
            val currentIndex = sortedMaps?.indexOf(it) ?: 0
            val nextIndex = if(isIncrementing) currentIndex+1 else currentIndex-1
            sortedMaps?.getOrNull(nextIndex)?.let { nextMap ->
                mapView.setMap(nextMap)
            }
        }
    }

}