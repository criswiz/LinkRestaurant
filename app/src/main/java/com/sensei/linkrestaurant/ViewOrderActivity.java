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

import com.sensei.linkrestaurant.Adapter.MyOrderAdapter;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.MaxOrderModel;
import com.sensei.linkrestaurant.Model.Order;
import com.sensei.linkrestaurant.Model.OrderModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewOrderActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_order)
    RecyclerView recycler_view_order;

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    MyOrderAdapter adapter;
    List<Order> orderList;

    int maxData = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        init();
        initView();

        //getAllOrder();
        getMaxOrder();
    }

    private void getMaxOrder() {
        dialog.show();

        compositeDisposable.add(iLinkRestaurantAPI.getMaxOrder(Common.API_KEY,
                Common.currentUser.getFbid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MaxOrderModel>() {
                    @Override
                    public void accept(MaxOrderModel maxordermodel) throws Exception {
                        if (maxordermodel.isSuccess()){
                            maxData = maxordermodel.getResult().get(0).getMaxRow();
                            dialog.dismiss();

                            getAllOrder(0,10);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        dialog.dismiss();
                        Toast.makeText(ViewOrderActivity.this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void getAllOrder(int from, int to) {
        dialog.show();

        compositeDisposable.add(iLinkRestaurantAPI.getOrder(Common.API_KEY,
                Common.currentUser.getFbid(),from,to)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<OrderModel>() {
            @Override
            public void accept(OrderModel orderModel) {
                if (orderModel.isSuccess()){
                    if(orderModel.getResult().size() > 0){
                        if (adapter == null){
                            orderList = new ArrayList<>();
                            orderList = (orderModel.getResult());
                            adapter = new MyOrderAdapter(ViewOrderActivity.this, orderList, recycler_view_order);
                            adapter.setiLoadMore(this);
                            recycler_view_order.setAdapter(adapter);
                        }else{
                            orderList.remove(orderList.size()-1);
                            orderList = orderModel.getResult();
                            adapter.addItem(orderList);
                        }
                    }
                    dialog.dismiss();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                Toast.makeText(ViewOrderActivity.this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void init(){
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    private void initView() {
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view_order.setLayoutManager(layoutManager);
        recycler_view_order.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        toolbar.setTitle(getString(R.string.your_order));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onLoadMore(){
        if (adapter.getItemCount() < maxData){
            orderList.add(null);
            adapter.notifyItemInserted(orderList.size()-1);

            getAllOrder(adapter.getItemCount()+1, adapter.getItemCount()+10);

            adapter.notifyDataSetChanged();
            adapter.setIsLoaded();
        }else{
            Toast.makeText(this, "Max Data to load", Toast.LENGTH_SHORT).show();
        }
    }*/

}