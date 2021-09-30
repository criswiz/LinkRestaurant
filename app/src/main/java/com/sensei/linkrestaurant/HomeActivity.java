package com.sensei.linkrestaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.sensei.linkrestaurant.Adapter.MyRestaurantAdapter;
import com.sensei.linkrestaurant.Adapter.RestaurantSliderAdapter;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.EventBus.RestaurantLoadEvent;
import com.sensei.linkrestaurant.Model.Restaurant;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.sensei.linkrestaurant.Service.PicassoImageLoadingService;
import com.sensei.linkrestaurant.databinding.ActivityHomeBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    TextView txt_user_name, txt_user_phone;

    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_restaurant)
    RecyclerView recyclerView;

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_link_main2);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        txt_user_name = headerView.findViewById(R.id.txt_user_name);
        txt_user_phone = headerView.findViewById(R.id.txt_user_phone);

        txt_user_name.setText(Common.currentUser.getName());
        txt_user_phone.setText(Common.currentUser.getUserPhone());

        navigationView.setNavigationItemSelectedListener(this);


        init();
        initView();
        loadRestaurant();
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);

        Slider.init(new PicassoImageLoadingService());

    }

    private void initView() {
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

    }


    private void loadRestaurant() {
        dialog.show();


            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
            compositeDisposable.add(
                    iLinkRestaurantAPI.getRestaurant(headers)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(restaurantModel -> {
                                        //Event bus to send local even to set adapter and slider
                                        EventBus.getDefault().post(new RestaurantLoadEvent(true, restaurantModel.getMessage()));
                                        dialog.dismiss();
                                    },
                                    throwable -> {
                                        dialog.dismiss();
                                        EventBus.getDefault().post(new RestaurantLoadEvent(false, throwable.getMessage()));
                                        Toast.makeText(this, "[GET RESTAURANT]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
            );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_link_main2);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_log_out){
            signOut();
        } else if (id == R.id.nav_nearby){
            startActivity(new Intent(HomeActivity.this, NearbyRestaurantActivity.class));
        } else if(id == R.id.order_history){
            startActivity(new Intent(HomeActivity.this, ViewOrderActivity.class));
        } else if(id == R.id.update_information){
            startActivity(new Intent(HomeActivity.this, UpdateInfoActivity.class));
        }else if (id == R.id.nav_fav){
            startActivity(new Intent(HomeActivity.this, FavoriteActivity.class));
        }
        return true;

    }

    private void signOut() {
        AlertDialog confirmExit = new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Do you really want to Sign Out")
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Common.currentUser = null;
                        Common.currentRestaurant = null;

                        FirebaseAuth.getInstance().signOut();
                        Intent intent  = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).create();

        confirmExit.show();

    }

    //Register Event Bus
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    //Listen Event Bus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processRestaurantLoadEvent(RestaurantLoadEvent event) {

            if (event.isSuccess()) {
                displayBanner(event.getRestaurantList());
                displayRestaurant(event.getRestaurantList());
            } else {
                Toast.makeText(this, "[RESTAURANT LOAD]" + event.getMessage(), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
    }

    private void displayRestaurant(List<Restaurant> restaurantList) {
        MyRestaurantAdapter adapter = new MyRestaurantAdapter(this, restaurantList);
        recyclerView.setAdapter(adapter);
    }

    private void displayBanner(List<Restaurant> restaurantList) {
        banner_slider.setAdapter(new RestaurantSliderAdapter(restaurantList));
    }

}