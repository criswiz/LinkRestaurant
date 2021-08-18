package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nex3z.notificationbadge.NotificationBadge;
import com.sensei.linkrestaurant.Adapter.MyCategoryAdapter;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Database.CartDataSource;
import com.sensei.linkrestaurant.Database.CartDatabase;
import com.sensei.linkrestaurant.Database.LocalCartDataSource;
import com.sensei.linkrestaurant.Model.EventBus.MenuItemEvent;
import com.sensei.linkrestaurant.Model.FavoriteOnlyIdModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.sensei.linkrestaurant.Utils.SpacesItemDecoration;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MenuActivity extends AppCompatActivity {

    @BindView(R.id.img_restaurant)
    KenBurnsView img_restaurant;
    @BindView(R.id.recycler_category)
    RecyclerView recycler_category;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton btn_cart;
    @BindView(R.id.badge)
    NotificationBadge badge;

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    MyCategoryAdapter adapter;
    CartDataSource cartDataSource;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        init();
        initView();

        countCartByRestaurant();
        loadFavoriteByRestaurant();
    }

    private void loadFavoriteByRestaurant() {
        compositeDisposable.add(iLinkRestaurantAPI.getFavoriteByRestaurant(Common.API_KEY,
                Common.currentUser.getFbid(),
                Common.currentRestaurant.getId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<FavoriteOnlyIdModel>() {
            @Override
            public void accept(FavoriteOnlyIdModel favoriteOnlyIdModel) throws Exception {
                if (favoriteOnlyIdModel.isSuccess()){
                    if (favoriteOnlyIdModel.getResult() != null && favoriteOnlyIdModel.getResult().size() > 0){
                        Common.currentFavOfRestaurant = favoriteOnlyIdModel.getResult();
                    }else {
                        Common.currentFavOfRestaurant = new ArrayList<>();
                    }
                }else {
                    //Toast.makeText(MenuActivity.this, "[GET FAVORITE]"+favoriteOnlyIdModel.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(MenuActivity.this, "[GET FAVORITE]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartByRestaurant();
    }

    private void countCartByRestaurant() {
        cartDataSource.countItemInCart(Common.currentUser.getFbid(),
                Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NotNull Integer integer) {
                        badge.setText(String.valueOf(integer));
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        Toast.makeText(MenuActivity.this, "Count Cart"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initView() {
        ButterKnife.bind(this);

        btn_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, CartListActivity.class));
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //If item is last it will set full width on Grid layout
                if (adapter != null){
                    switch (adapter.getItemViewType(position))
                    {
                        case Common.DEFAULT_COLUMN_COUNT:
                            return 1;
                        default: return -1;
                    }
                }else
                    return -1;
            }
        });
        recycler_category.setLayoutManager(gridLayoutManager);
        recycler_category.addItemDecoration(new SpacesItemDecoration(8));
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
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
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    public void loadMenuByRestaurant(MenuItemEvent event){
        if (event.isSuccess()){
            Picasso.get().load(event.getRestaurant().getImage()).into(img_restaurant);
            toolbar.setTitle(event.getRestaurant().getName());

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //request category by restaurant Id
            compositeDisposable.add(iLinkRestaurantAPI.getCategories(Common.API_KEY,event.getRestaurant().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(menuModel -> {
                                adapter = new MyCategoryAdapter(MenuActivity.this, menuModel.getResult());
                                recycler_category.setAdapter(adapter);
                            },
                            throwable -> {
                                Toast.makeText(this, "[GET CATEGORY]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            })
            );
        }else {

        }
    }
}