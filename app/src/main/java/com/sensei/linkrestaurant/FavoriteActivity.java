package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.sensei.linkrestaurant.Adapter.MyFavoriteAdapter;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.FavoriteModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FavoriteActivity extends AppCompatActivity {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;
    MyFavoriteAdapter adapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_fav)
    RecyclerView recycler_fav;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        if (adapter != null)
            adapter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        init();
        initView();
        loadFavoriteItems();
    }

    private void loadFavoriteItems() {
        dialog.show();


        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(iLinkRestaurantAPI.getFavoriteByUser(headers)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<FavoriteModel>() {
            @Override
            public void accept(FavoriteModel favoriteModel) throws Exception {
                if (favoriteModel.isSuccess()){
                    adapter = new MyFavoriteAdapter(FavoriteActivity.this, favoriteModel.getResult());
                    recycler_fav.setAdapter(adapter);
                }else {
                    if (favoriteModel.getMessage().contains("Empty"))
                        Toast.makeText(FavoriteActivity.this, "You do not have any favorite item", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(FavoriteActivity.this, "[GET FAV RESULT]"+favoriteModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(FavoriteActivity.this, "[GET FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void initView() {
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_fav.setLayoutManager(layoutManager);
        recycler_fav.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        toolbar.setTitle(R.string.favorite);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }
}