package com.sensei.linkrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensei.linkrestaurant.Database.CartDataSource;
import com.sensei.linkrestaurant.Database.CartDatabase;
import com.sensei.linkrestaurant.Database.CartItem;
import com.sensei.linkrestaurant.Database.LocalCartDataSource;
import com.sensei.linkrestaurant.Interface.IOnImageViewAdapterClickListener;
import com.sensei.linkrestaurant.Model.EventBus.CalculatePriceEvent;
import com.sensei.linkrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    CartDataSource cartDataSource;

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(cartItemList.get(position).getFoodImage()).into(holder.img_food);
        holder.txt_food_name.setText(cartItemList.get(position).getFoodName());
        holder.txt_food_price.setText(String.valueOf(cartItemList.get(position).getFoodPrice()));
        holder.txt_quantity.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));

        Double finalresult = cartItemList.get(position).getFoodPrice() * cartItemList.get(position).getFoodQuantity();
        holder.txt_price_new.setText(String.valueOf(finalresult));

        holder.txt_extra_price.setText(new StringBuilder("Extra Price (GHC): +")
        .append(cartItemList.get(position).getFoodExtraPrice()));

        //Event
        holder.setiOnImageViewAdapterClickListener(new IOnImageViewAdapterClickListener() {
            @Override
            public void onCalculatePriceListener(View view, int position, boolean isDecrease, boolean isDelete) {
                if (!isDelete){
                    if (isDecrease){
                        if (cartItemList.get(position).getFoodQuantity() > 1)
                            cartItemList.get(position).setFoodQuantity(cartItemList.get(position).getFoodQuantity()-1);
                    }else{
                        //Increase quantity
                        if (cartItemList.get(position).getFoodQuantity() < 99)
                            cartItemList.get(position).setFoodQuantity(cartItemList.get(position).getFoodQuantity()+1);
                    }
                    //Update Cart
                    cartDataSource.updateCart(cartItemList.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {
                                    
                                }

                                @Override
                                public void onSuccess(@NonNull Integer integer) {
                                    holder.txt_quantity.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));

                                    EventBus.getDefault().postSticky(new CalculatePriceEvent());
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    Toast.makeText(context, "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }else {
                    //delete
                    cartDataSource.deleteCart(cartItemList.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@NonNull Integer integer) {
                                    notifyItemRemoved(position);
                                    EventBus.getDefault().postSticky(new CalculatePriceEvent());
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    Toast.makeText(context, "[DELETE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_price_new)
        TextView txt_price_new;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_quantity)
        TextView txt_quantity;
        @BindView(R.id.txt_extra_price)
        TextView txt_extra_price;

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_delete_food)
        ImageView img_delete_food;
        @BindView(R.id.img_decrease)
        ImageView img_decrease;
        @BindView(R.id.img_increase)
        ImageView img_increase;

        IOnImageViewAdapterClickListener iOnImageViewAdapterClickListener;

        public void setiOnImageViewAdapterClickListener(IOnImageViewAdapterClickListener iOnImageViewAdapterClickListener) {
            this.iOnImageViewAdapterClickListener = iOnImageViewAdapterClickListener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);

            img_decrease.setOnClickListener(this);
            img_increase.setOnClickListener(this);
            img_delete_food.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == img_decrease){
                iOnImageViewAdapterClickListener.onCalculatePriceListener(view, getAdapterPosition(), true, false);
            }else if (view == img_increase){
                iOnImageViewAdapterClickListener.onCalculatePriceListener(view, getAdapterPosition(), false, false);
            }else if (view == img_delete_food){
                iOnImageViewAdapterClickListener.onCalculatePriceListener(view, getAdapterPosition(), true, true);
            }
        }
    }
}
