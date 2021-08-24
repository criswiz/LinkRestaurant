package com.sensei.linkrestaurant.Service;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.TokenModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT
                ).create(ILinkRestaurantAPI.class);
        compositeDisposable = new CompositeDisposable();
        Paper.init(this);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onNewToken(@NonNull String newToken) {
        super.onNewToken(newToken);
        //Update token here with FBID
        //Since this is a service, so Common.currentUser will be null
        //Save signed FBID by paper and get it back
        String fbid = Paper.book().read(Common.REMEMBER_FBID);
        String apiKey = Paper.book().read(Common.API_KEY_TAG);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(iLinkRestaurantAPI.updateTokenToServer(headers,newToken )
            .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TokenModel>() {
                    @Override
                    public void accept(TokenModel tokenModel) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MyFirebaseMessagingService.this, "[REFRESH TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null) {

            Common.showNotification(this,
                    new Random().nextInt(),
                    dataRecv.get(Common.NOTIFI_TITLE),
                    dataRecv.get(Common.NOTIFI_CONTENT),
                    null);
        }
    }
}
