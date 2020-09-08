package com.example.android_quickstart_sdkv2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mappedin.Mappedin;
import com.mappedin.MiGestureType;
import com.mappedin.MiMapView;
import com.mappedin.MiMapViewListener;
import com.mappedin.enums.MiMapStatus;
import com.mappedin.models.MiLevel;
import com.mappedin.models.MiOverlay;
import com.mappedin.models.MiSpace;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize the Mappedin singleton with the application and credentials
        Mappedin.init(getApplication()); //Mapbox token is optional
        Mappedin.setCredentials("5f4e59bb91b055001a68e9d9", "gmwQbwuNv7cvDYggcYl4cMa5c7n0vh4vqNQEkoyLRuJ4vU42");

        setContentView(R.layout.activity_main);

        MiMapView mapView = findViewById(R.id.mapView);

        //Set the listener on the
        mapView.setListener(new MiMapViewListener() {
            @Override
            public void onTapNothing() {

            }

            @Override
            public void onTapCoordinates(@NotNull LatLng latLng) {

            }

            @Override
            public void onManipulateCamera(@NotNull MiGestureType miGestureType) {

            }

            @Override
            public boolean didTapSpace(@Nullable MiSpace miSpace) {
                return false;
            }

            @Override
            public boolean didTapOverlay(@NotNull MiOverlay miOverlay) {
                return false;
            }

            @Override
            public void onLevelChange(MiLevel level) {
                mapView.focusOnCurrentLevel(0.0, 0.0, 0, 1000); //Frames the map camera on the current level
            }
        });

        Mappedin.getVenue("mappedin-demo-mall", venue -> {
            mapView.observeLifecycle(this.getLifecycle()); //handles the MapView lifecycle properly
            mapView.loadMap(venue, status -> {
                if (status == MiMapStatus.LOADED) {
                    //Map has successfully loaded!
                } else {
                    //There was an error loading the map
                }
            });
        });
    }
}
