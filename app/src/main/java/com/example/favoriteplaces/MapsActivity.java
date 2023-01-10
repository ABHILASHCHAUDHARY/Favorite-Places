package com.example.favoriteplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.favoriteplaces.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.favoriteplaces.MainActivity.arrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    String address;

    public void centerMapOnLocation(Location location,String title){
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,12));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,100,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, address);

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    private void buildAlertMessageNoGps(){
     final AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setMessage("Your GPS seems to be disabled, do you want to enable it?\"")
             .setCancelable(false)
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                 }
             })
             .setNegativeButton("No", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                 }
             });
final AlertDialog alert =builder.create();
alert.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        if (intent.getIntExtra("placeNumber", 0) == 0) {
            // Zoom in on user location
String s = intent.getStringExtra("Info");
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addressList != null && addressList.size() > 0) {

                            address = "";
                            if (addressList.get(0).getThoroughfare() != null) {
                                address += addressList.get(0).getThoroughfare() + " ";
                            }

                            if (addressList.get(0).getLocality() != null) {
                                address += addressList.get(0).getLocality() + " ";
                            }
                            if (addressList.get(0).getAdminArea() != null) {
                                address += addressList.get(0).getAdminArea() + " ";
                            }

                            if (addressList.get(0).getPostalCode() != null) {
                                address += addressList.get(0).getPostalCode() + " ";
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    centerMapOnLocation(location, address);


                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    mMap.clear();
                    centerMapOnLocation(lastKnownLocation, address);
                }
            }
        } else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);

            centerMapOnLocation(placeLocation, arrayList.get(intent.getIntExtra("placeNumber",0)));
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        String address ="";
        Geocoder geocoder1= new Geocoder(getApplicationContext(),Locale.getDefault());
        try {
            List<Address> newAddressList = geocoder1.getFromLocation(latLng.latitude,latLng.longitude,1);

            if(newAddressList != null && newAddressList.size()>0){

                if(newAddressList.get(0).getThoroughfare() != null){
                    if(newAddressList.get(0).getSubThoroughfare() != null){
                        address += newAddressList.get(0).getSubThoroughfare() + " ";
                    }
                    address += newAddressList.get(0).getThoroughfare() + " ";
                }

                if(newAddressList.get(0).getLocality() != null){
                    address +=newAddressList.get(0).getLocality() + " ";
                }
                if(newAddressList.get(0).getAdminArea() != null){
                    address +=newAddressList.get(0).getAdminArea() + " ";
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(address.equals("")){
            address = "No Address found";
        }
        //sending new locations to add marker
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
        arrayList.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

   //shared preferences for storing places of users

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.favoriteplaces", Context.MODE_PRIVATE);
        try {
            sharedPreferences.edit().putString("placesArrayList",ObjectSerializer.serialize(arrayList)).apply();

            // Object Serialize class does not able to take latlng type arraylist, make two arraylist for lat and lng

            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            for(LatLng coord: MainActivity.locations){
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));
            }

            sharedPreferences.edit().putString("Lats",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("Longs",ObjectSerializer.serialize(longitudes)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(MapsActivity.this, "Location Saved!", Toast.LENGTH_SHORT).show();
    }

    }