package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.sensei.linkrestaurant.Adapter.MyFoodAdapter;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.Category;
import com.sensei.linkrestaurant.Model.EventBus.FoodListEvent;
import com.sensei.linkrestaurant.Model.EventBus.MenuItemEvent;
import com.sensei.linkrestaurant.Model.FoodModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FoodListActivity extends AppCompatActivity {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;
    MyFoodAdapter adapter,searchAdapter;

    @BindView(R.id.img_category)
    KenBurnsView img_category;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerFoodList;
    private Category selectedCategory;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        if (adapter != null)
            adapter.onStop();
        if (searchAdapter != null)
            searchAdapter.onStop();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        init();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem menuItem = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchFood(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                //restore to default adapter when Search is closed
                recyclerFoodList.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    private void startSearchFood(String query) {
        dialog.show();


        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(iLinkRestaurantAPI.searchFood(headers,
                query,
                selectedCategory.getId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<FoodModel>() {
            @Override
            public void accept(FoodModel foodModel) throws Exception {
                if (foodModel.isSuccess()){
                    searchAdapter = new MyFoodAdapter(FoodListActivity.this, foodModel.getMessage());
                    recyclerFoodList.setAdapter(searchAdapter);
                }else {
                    if (foodModel.getMessage().isEmpty()){
                        recyclerFoodList.setAdapter(null);
                        Toast.makeText(FoodListActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                Toast.makeText(FoodListActivity.this, "[SEARCH FOOD]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }


    private void initView() {
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerFoodList.setLayoutManager(layoutManager);
        recyclerFoodList.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void loadFoodListByCategory(@NotNull FoodListEvent event){
        if (event.isSuccess()){

            selectedCategory = event.getCategory();

            Picasso.get().load(event.getCategory().getImage()).into(img_category);
            toolbar.setTitle(event.getCategory().getName());

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            dialog.show();


            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildJWT(Common.API_KEY));

            compositeDisposable.add(iLinkRestaurantAPI.getFoodOfMenu(headers,
                    event.getCategory().getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(foodModel -> {
                        if (foodModel.isSuccess()){
                            adapter = new MyFoodAdapter(this, foodModel.getMessage());
                            recyclerFoodList.setAdapter(adapter);
                        }
                        dialog.dismiss();
                    },
                    throwable -> {
                        dialog.dismiss();
                        Toast.makeText(this, "[GET FOOD]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
        }
    }
}