package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sensei.linkrestaurant.Adapter.MyCartAdapter;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Database.CartDataSource;
import com.sensei.linkrestaurant.Database.CartDatabase;
import com.sensei.linkrestaurant.Database.CartItem;
import com.sensei.linkrestaurant.Database.LocalCartDataSource;
import com.sensei.linkrestaurant.Model.EventBus.CalculatePriceEvent;
import com.sensei.linkrestaurant.Model.EventBus.SendTotalCashEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_final_price)
    TextView txt_final_price;
    @BindView(R.id.btn_order)
    Button btn_order;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    CartDataSource cartDataSource;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        init();
        initView();

        getAllItemInCart();
    }

    private void getAllItemInCart() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),
                Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CartItem>>() {
                    @Override
                    public void accept(List<CartItem> cartItems) throws Exception {
                        if (cartItems.isEmpty()){
                            btn_order.setText(getString(R.string.empty_cart));
                            btn_order.setEnabled(false);
                            btn_order.setBackgroundResource(android.R.color.darker_gray);
                        }else {
                            btn_order.setText(getString(R.string.place_order));
                            btn_order.setEnabled(true);
                            btn_order.setBackgroundResource(R.color.color_primary);

                            MyCartAdapter adapter = new MyCartAdapter(CartListActivity.this, cartItems);
                            recycler_cart.setAdapter(adapter);

                            calculateCartTotalPrice();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(CartListActivity.this, "[GET CART]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void calculateCartTotalPrice() {
        cartDataSource.sumPrice(Common.currentUser.getFbid(), Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Long aLong) {
                        if (aLong <= 0){
                            btn_order.setText(getString(R.string.empty_cart));
                            btn_order.setEnabled(false);
                            btn_order.setBackgroundResource(android.R.color.darker_gray);
                        }else {
                            btn_order.setText(getString(R.string.place_order));
                            btn_order.setEnabled(true);
                            btn_order.setBackgroundResource(R.color.color_primary);
                        }

                        txt_final_price.setText(String.valueOf(aLong));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (e.getMessage().contains("Query returned empty"))
                            txt_final_price.setText("0");
                        else
                            Toast.makeText(CartListActivity.this, "[SUM CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        toolbar.setTitle(getString(R.string.cart));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().postSticky(new SendTotalCashEvent(txt_final_price.getText().toString()));
                startActivity(new Intent(CartListActivity.this, PlaceOrderActivity.class));
            }
        });
    }

    private void initView() {
        ButterKnife.bind(this);
    }

    //EvenBus

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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void calculatePrice(CalculatePriceEvent event){
        if (event !=null)
            calculateCartTotalPrice();
    }
}