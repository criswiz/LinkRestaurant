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
import com.sensei.linkrestaurant.FoodListActivity;
import com.sensei.linkrestaurant.Interface.IOnRecyclerViewClickListener;
import com.sensei.linkrestaurant.Model.Category;
import com.sensei.linkrestaurant.Model.EventBus.FoodListEvent;
import com.sensei.linkrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoryAdapter extends RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder> {

    Context context;
    List<Category> category;

    public MyCategoryAdapter(Context context, List<Category> category) {
        this.context = context;
        this.category = category;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_category, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Picasso.get().load(category.get(position).getImage()).into(holder.img_catergory);
        holder.txt_category.setText(category.get(position).getName());

        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Send postSticky event to FoodListActivity
                EventBus.getDefault().postSticky(new FoodListEvent(true, category.get(position)));
                context.startActivity(new Intent(context, FoodListActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return category.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_category)
        ImageView img_catergory;
        @BindView(R.id.txt_category)
        TextView txt_category;

        IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (category.size() == 1){
            return Common.DEFAULT_COLUMN_COUNT;
        }else {
            if (category.size() % 2 == 0){
                return Common.DEFAULT_COLUMN_COUNT;
            }else {
                return (position > 1 && position == category.size()-1) ? Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;

            }
        }
    }
}
