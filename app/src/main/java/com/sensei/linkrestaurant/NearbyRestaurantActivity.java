package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.EventBus.MenuItemEvent;
import com.sensei.linkrestaurant.Model.Restaurant;
import com.sensei.linkrestaurant.Model.RestaurantModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.sensei.linkrestaurant.databinding.ActivityNearbyReataurantBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NearbyRestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityNearbyReataurantBinding binding;

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    AlertDialog dialog;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    Marker userMarker;

    boolean isFirstLoad = false;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNearbyReataurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
        initView();
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);

        buildLocationRequest();
        buildLocationCallback();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                addMarkerAndMoveCamera(locationResult.getLastLocation());

                if (!isFirstLoad){
                    isFirstLoad = !isFirstLoad;
                    requestNearbyRestaurant(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude(),10);
                }
            }
        };
    }

    private void requestNearbyRestaurant(double latitude, double longitude, int distance) {
        dialog.show();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(iLinkRestaurantAPI.getNearbyRestaurant(headers, latitude,longitude,distance)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<RestaurantModel>() {
            @Override
            public void accept(RestaurantModel restaurantModel) throws Exception {
                if (restaurantModel.isSuccess()){
                    addRestaurantMarker(restaurantModel.getMessage());
                }else {
                    Toast.makeText(NearbyRestaurantActivity.this, ""+restaurantModel.getResult(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                Toast.makeText(NearbyRestaurantActivity.this, "[NEARBY RESTAURANT]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void addRestaurantMarker(List<Restaurant> restaurantList) {
        for (Restaurant restaurant: restaurantList){
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
                    .position(new LatLng(restaurant.getLat(), restaurant.getLng()))
                    .snippet(restaurant.getAddress())
                    .title(new StringBuilder()
                            .append(restaurant.getId())
                    .append(".")
                    .append(restaurant.getName()).toString()));
        }
    }

    private void addMarkerAndMoveCamera(Location lastLocation) {
        if (userMarker != null)
            userMarker.remove();

        LatLng userlatlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        userMarker = mMap.addMarker(new MarkerOptions().position(userlatlng).title(Common.currentUser.getName()));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userlatlng, 17);
        mMap.animateCamera(yourLocation);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.nearby_restaurant));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success)
                Log.e("ERROR_MAP", "Load style error");

        }catch (Resources.NotFoundException e){
            Log.e("ERROR_MAP", "Resource not found");
        }


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String id = marker.getTitle().substring(0, marker.getTitle().indexOf("."));
                if (!TextUtils.isEmpty(id)){

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                    compositeDisposable.add(iLinkRestaurantAPI.getRestaurantById(headers,id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<RestaurantModel>() {
                        @Override
                        public void accept(RestaurantModel restaurantByIdModel) throws Exception {
                            if (restaurantByIdModel.isSuccess()){
                                Common.currentRestaurant = restaurantByIdModel.getMessage().get(0);
                                EventBus.getDefault().postSticky(new MenuItemEvent(true, Common.currentRestaurant));
                                startActivity(new Intent(NearbyRestaurantActivity.this, MenuActivity.class));
                                finish();
                            }else {
                                Toast.makeText(NearbyRestaurantActivity.this, ""+restaurantByIdModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            dialog.dismiss();
                            Toast.makeText(NearbyRestaurantActivity.this, "[GET RESTAURANT BY ID]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
                }
            }
        });
    }
}