package com.sensei.linkrestaurant.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.FoodDetailActivity;
import com.sensei.linkrestaurant.Interface.IOnRecyclerViewClickListener;
import com.sensei.linkrestaurant.Model.EventBus.FoodDetailEvent;
import com.sensei.linkrestaurant.Model.Favorite;
import com.sensei.linkrestaurant.Model.FoodModel;
import com.sensei.linkrestaurant.Model.Restaurant;
import com.sensei.linkrestaurant.Model.RestaurantModel;
import com.sensei.linkrestaurant.R;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyFavoriteAdapter extends RecyclerView.Adapter<MyFavoriteAdapter.MyViewHolder> {

    Context context;
    List<Favorite> favoriteList;
    CompositeDisposable compositeDisposable;
    ILinkRestaurantAPI iLinkRestaurantAPI;

    public MyFavoriteAdapter(Context context, List<Favorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
        compositeDisposable = new CompositeDisposable();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    public void onDestroy(){
        compositeDisposable.clear();
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_favorite_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Picasso.get().load(favoriteList.get(position).getFoodImage()).into(holder.img_food);
        holder.txt_food_name.setText(favoriteList.get(position).getFoodName());
        holder.txt_food_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(favoriteList.get(position).getPrice()));
        holder.txt_restaurant_name.setText(favoriteList.get(position).getRestaurantName());

        //Event
        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                compositeDisposable.add(iLinkRestaurantAPI.getFoodById(headers,
                        favoriteList.get(position).getFoodId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FoodModel>() {
                    @Override
                    public void accept(FoodModel foodModel) throws Exception {
                        if (foodModel.isSuccess()){
                            context.startActivity(new Intent(context, FoodDetailActivity.class));
                            if (Common.currentRestaurant != null){
                                Common.currentRestaurant = new Restaurant();

                                Common.currentRestaurant.setId(favoriteList.get(position).getRestaurantId());
                                Common.currentRestaurant.setName(favoriteList.get(position).getRestaurantName());

                                compositeDisposable.add(iLinkRestaurantAPI.getRestaurantById(headers,
                                        String.valueOf(favoriteList.get(position).getRestaurantId()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<RestaurantModel>() {
                                    @Override
                                    public void accept(RestaurantModel restaurantModel){
                                        if (restaurantModel.isSuccess()){
                                            Common.currentRestaurant = restaurantModel.getResult().get(0);
                                            EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodModel.getFoodList().get(0)));
                                        }else {
                                            Toast.makeText(context, ""+restaurantModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Toast.makeText(context, "[GET RESTAURANT BY ID]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }));
                            }else {

                                EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodModel.getFoodList().get(0)));
                            }
                        }else {
                            Toast.makeText(context, "[GET FOOD BY ID RESULT]"+foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(context, "[GET FOOD BY ID]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    Unbinder unbinder;
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;

        IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }
}
