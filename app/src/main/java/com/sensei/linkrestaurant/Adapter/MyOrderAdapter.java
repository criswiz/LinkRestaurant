package com.sensei.linkrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Interface.ILoadMore;
import com.sensei.linkrestaurant.Model.Order;
import com.sensei.linkrestaurant.Model.OrderModel;
import com.sensei.linkrestaurant.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;

public class MyOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    Context context;
    List<Order> orderList;
    SimpleDateFormat simpleDateFormat;

    RecyclerView recyclerView;
    ILoadMore iLoadMore;

    boolean isLoading = false;

    int totalItemCount=0, lastVisibleItem=0, visibleThreshold=10;

    public MyOrderAdapter(Context context, List<Order> orderList, RecyclerView recyclerView) {
        this.context = context;
        this.orderList = orderList;
        this.recyclerView = recyclerView;
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        //init
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= lastVisibleItem+visibleThreshold){
                    if (iLoadMore != null){
                        iLoadMore.onLoadMore();
                    }
                    isLoading=true;
                }
            }
        });
    }

    public void setIsLoaded(){isLoading=false;}

    public void addItem(List<Order> addedItems){
        int startInsertIndex = orderList.size();
        orderList.addAll(addedItems);
        notifyItemInserted(startInsertIndex);
    }

    public void setiLoadMore(ILoadMore iLoadMore) {
        this.iLoadMore = iLoadMore;
    }

    @Override
    public int getItemViewType(int position) {
        if (orderList.get(position) == null)
            return VIEW_TYPE_LOADING;
        else
            return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View viewItem;

        if (viewType == VIEW_TYPE_ITEM){
            viewItem = (LayoutInflater.from(context)
                    .inflate(R.layout.layout_order,parent,false));
        }else {
            viewItem = (LayoutInflater.from(context)
                    .inflate(R.layout.layout_loading_item,parent,false));
        }
        return new MyLoadingHolder(viewItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder){
            MyViewHolder myViewHolder = (MyViewHolder) holder;

            myViewHolder.txt_num_of_item.setText(new StringBuilder("Num of Items: " ).append(orderList.get(position).getNumberOfItem()));
            myViewHolder.txt_order_address.setText(new StringBuilder(orderList.get(position).getOrderAddress()));
            myViewHolder.txt_order_date.setText(new StringBuilder(simpleDateFormat.format(orderList.get(position).getOrderDate())));

            myViewHolder.txt_order_number.setText(new StringBuilder("Order Number: #").append(orderList.get(position).getOrderId()));
            myViewHolder.txt_order_phone.setText(new StringBuilder(orderList.get(position).getOrderPhone()));

            myViewHolder.txt_order_total_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(orderList.get(position).getTotalPrice()));
            myViewHolder.txt_order_status.setText(Common.convertStatusToString(orderList.get(position).getOrderStatus()));

            if (orderList.get(position).isCod())
                myViewHolder.txt_payment_method.setText(new StringBuilder("Cash On Delivery"));
            else
                myViewHolder.txt_payment_method.setText(new StringBuilder("Transaction ID: ").append(orderList.get(position).getTransactionId()));

        }else if (holder instanceof MyLoadingHolder){
            MyLoadingHolder myLoadingHolder = (MyLoadingHolder) holder;

            myLoadingHolder.progress_bar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void setiLoadMore(Consumer<OrderModel> orderModelConsumer) {
        this.iLoadMore = (ILoadMore) orderModelConsumer;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_phone)
        TextView txt_order_phone;
        @BindView(R.id.txt_order_address)
        TextView txt_order_address;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.txt_order_total_price)
        TextView txt_order_total_price;
        @BindView(R.id.txt_num_of_item)
        TextView txt_num_of_item;
        @BindView(R.id.txt_payment_method)
        TextView txt_payment_method;


        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }

    public class MyLoadingHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar)
        ProgressBar progress_bar;


        Unbinder unbinder;
        public MyLoadingHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }

}
