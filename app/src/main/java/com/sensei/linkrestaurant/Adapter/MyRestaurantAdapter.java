package com.sensei.linkrestaurant.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Interface.IOnRecyclerViewClickListener;
import com.sensei.linkrestaurant.MenuActivity;
import com.sensei.linkrestaurant.Model.EventBus.MenuItemEvent;
import com.sensei.linkrestaurant.Model.Restaurant;
import com.sensei.linkrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder> {

    Context context;
    List<Restaurant> restaurantList;

    public MyRestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_restaurant,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Picasso.get().load(restaurantList.get(position).getImage()).into(holder.img_restaurant);
        holder.txt_restaurant_address.setText(new StringBuilder(restaurantList.get(position).getAddress()));
        holder.txt_restaurant_name.setText(new StringBuilder(restaurantList.get(position).getName()));

        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Common.currentRestaurant = restaurantList.get(position);

                //Post sticky to listen from another activity
                EventBus.getDefault().postSticky(new MenuItemEvent(true, restaurantList.get(position)));
                context.startActivity(new Intent(context, MenuActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;
        @BindView(R.id.txt_restaurant_address)
        TextView txt_restaurant_address;
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

        IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }
}
