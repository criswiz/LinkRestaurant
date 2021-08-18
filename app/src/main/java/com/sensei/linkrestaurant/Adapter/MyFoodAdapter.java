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
import com.sensei.linkrestaurant.Database.CartDataSource;
import com.sensei.linkrestaurant.Database.CartDatabase;
import com.sensei.linkrestaurant.Database.CartItem;
import com.sensei.linkrestaurant.Database.LocalCartDataSource;
import com.sensei.linkrestaurant.FoodDetailActivity;
import com.sensei.linkrestaurant.Interface.IFoodDetailOrCartClickListener;
import com.sensei.linkrestaurant.Model.EventBus.FoodDetailEvent;
import com.sensei.linkrestaurant.Model.FavoriteModel;
import com.sensei.linkrestaurant.Model.FavoriteOnlyId;
import com.sensei.linkrestaurant.Model.Food;
import com.sensei.linkrestaurant.R;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyFoodAdapter extends RecyclerView.Adapter<MyFoodAdapter.MyViewHolder> {

    Context context;
    List<Food> foodList;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    ILinkRestaurantAPI iLinkRestaurantAPI;

    public void onStop(){
        compositeDisposable.clear();
    }

    public MyFoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
        compositeDisposable = new CompositeDisposable();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Picasso.get().load(foodList.get(position).getImage()).placeholder(R.drawable.app_icon).into(holder.img_food);
        holder.txt_food_name.setText(foodList.get(position).getName());
        holder.txt_food_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(foodList.get(position).getPrice()));

        //Check favorite
        if (Common.currentFavOfRestaurant != null && Common.currentFavOfRestaurant.size() > 0){
            if (Common.checkFavorite(foodList.get(position).getId())){
                holder.img_fav.setImageResource(R.drawable.ic_baseline_favorite_primary_color_24);
                holder.img_fav.setTag(true);
            }else {
                holder.img_fav.setImageResource(R.drawable.ic_baseline_favorite_border_primary_color_24);
                holder.img_fav.setTag(false);
            }
        }else {
            //Default no favorite set
            holder.img_fav.setTag(false);
        }

        //Favorite Event
        holder.img_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView fav = (ImageView) view;
                if ((Boolean) fav.getTag()){
                    //if tag = true favorite item clicked
                    compositeDisposable.add(iLinkRestaurantAPI.removeFavorite(Common.API_KEY,
                            Common.currentUser.getFbid(),
                            foodList.get(position).getId(),
                            Common.currentRestaurant.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<FavoriteModel>() {
                        @Override
                        public void accept(FavoriteModel favoriteModel) throws Exception {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")){
                                fav.setImageResource(R.drawable.ic_baseline_favorite_border_primary_color_24);
                                fav.setTag(false);
                                if (Common.currentFavOfRestaurant != null){
                                    Common.removeFavorite(foodList.get(position).getId());
                                }
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            //Toast.makeText(context, "[REMOVE FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
                }else {
                    //if tag = true favorite item clicked
                    compositeDisposable.add(iLinkRestaurantAPI.insertFavorite(Common.API_KEY,
                            Common.currentUser.getFbid(),
                            foodList.get(position).getId(),
                            Common.currentRestaurant.getId(),
                            Common.currentRestaurant.getName(),
                            foodList.get(position).getName(),
                            foodList.get(position).getImage(),
                            foodList.get(position).getPrice())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<FavoriteModel>() {
                                @Override
                                public void accept(FavoriteModel favoriteModel) throws Exception {
                                    if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")){
                                        fav.setImageResource(R.drawable.ic_baseline_favorite_primary_color_24);
                                        fav.setTag(true);
                                        if (Common.currentFavOfRestaurant != null){
                                            Common.currentFavOfRestaurant.add(new FavoriteOnlyId(foodList.get(position).getId()));
                                        }
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Toast.makeText(context, "[ADD FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            }
        });

        holder.setListener(new IFoodDetailOrCartClickListener() {
            @Override
            public void onFoodItemClickListener(View view, int position, boolean isDetail) {
                if (isDetail){
                    context.startActivity(new Intent(context, FoodDetailActivity.class));
                    EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodList.get(position)));
                } else {
                    CartItem cartItem =  new CartItem();
                    cartItem.setFoodId(foodList.get(position).getId());
                    cartItem.setFoodName(foodList.get(position).getName());
                    cartItem.setFoodPrice(foodList.get(position).getPrice());
                    cartItem.setFoodImage(foodList.get(position).getImage());
                    cartItem.setFoodQuantity(1);
                    cartItem.setUserPhone(Common.currentUser.getUserPhone());
                    cartItem.setRestaurantId(Common.currentRestaurant.getId());
                    cartItem.setFoodAddon("TODO");
                    cartItem.setFoodSize("TODO");
                    cartItem.setFoodExtraPrice(0.0);
                    cartItem.setFbid(Common.currentUser.getFbid());

                    compositeDisposable.add(
                            cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                        Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        Toast.makeText(context, "[ADD CART]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                    );
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_detail)
        ImageView img_detail;
        @BindView(R.id.img_cart)
        ImageView img_cart;

        IFoodDetailOrCartClickListener listener;

        public void setListener(IFoodDetailOrCartClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);

            img_detail.setOnClickListener(this);
            img_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_detail){
                listener.onFoodItemClickListener(v, getAdapterPosition(), true);
            }else if (v.getId() == R.id.img_cart){
                listener.onFoodItemClickListener(v, getAdapterPosition(), false);
            }
        }
    }
}
