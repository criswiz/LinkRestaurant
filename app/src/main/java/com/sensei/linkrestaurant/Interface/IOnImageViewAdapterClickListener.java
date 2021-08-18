package com.sensei.linkrestaurant.Interface;

import android.view.View;

public interface IOnImageViewAdapterClickListener {
    void onCalculatePriceListener(View view, int position, boolean isDecrease, boolean isDelete);
}
