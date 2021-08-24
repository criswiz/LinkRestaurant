package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.GetKeyModel;
import com.sensei.linkrestaurant.Model.TokenModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SplashScreen extends AppCompatActivity {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        Dexter.withContext(SplashScreen.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {


                        FirebaseInstallations.getInstance()
                                .getToken(true)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SplashScreen.this, "[GET TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    if (user != null){
                                        Paper.book().write(Common.REMEMBER_FBID, user.getUid());

                                        dialog.show();


                                        Map<String, String> headers = new HashMap<>();
                                        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                        compositeDisposable.add(iLinkRestaurantAPI.updateTokenToServer(headers,
                                                task.getResult().getToken())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<TokenModel>() {
                                                    @Override
                                                    public void accept(TokenModel tokenModel) throws Exception {
                                                        if (!tokenModel.isSuccess()) {
                                                            Toast.makeText(SplashScreen.this, "[UPDATE TOKEN ERROR]" + tokenModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }else {
                                                            compositeDisposable.add(iLinkRestaurantAPI.getKey(user.getUid())
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Consumer<GetKeyModel>() {
                                                                    @Override
                                                                    public void accept(GetKeyModel getKeyModel) throws Exception {
                                                                        if (getKeyModel.isSuccess()){
                                                                            Common.API_KEY = getKeyModel.getToken();

                                                                            Map<String, String> headers = new HashMap<>();
                                                                            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                                                            compositeDisposable.add(iLinkRestaurantAPI.getUser(headers)
                                                                                    .subscribeOn(Schedulers.io())
                                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                                    .subscribe(userModel -> {
                                                                                                // If user already exists
                                                                                                if (userModel.isSuccess()) {
                                                                                                    Common.currentUser = userModel.getResult().get(0);
                                                                                                    Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
                                                                                                    startActivity(intent);
                                                                                                } else {
                                                                                                    if (Common.currentUser == null) {
                                                                                                        Intent intent = new Intent(SplashScreen.this, UpdateInfoActivity.class);
                                                                                                        startActivity(intent);
                                                                                                    }
                                                                                                }
                                                                                                finish();
                                                                                                dialog.dismiss();
                                                                                            },
                                                                                            throwable -> {
                                                                                                dialog.dismiss();
                                                                                                Toast.makeText(SplashScreen.this, "[GET USER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                            }));
                                                                        }else {
                                                                            dialog.dismiss();
                                                                            Toast.makeText(SplashScreen.this, ""+getKeyModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }, new Consumer<Throwable>() {
                                                                    @Override
                                                                    public void accept(Throwable throwable) throws Exception {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(SplashScreen.this, "Cannot get Json Web Token", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }));
                                                        }
                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        Toast.makeText(SplashScreen.this, "[GET USER API]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                        );


                                    }else{
                                        Toast.makeText(SplashScreen.this, "Not Signed in Please SignIn", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                        finish();
                                    }


                                }
                            }
                        });

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(SplashScreen.this, "You must accept all permissions to use app efficiently ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void init() {
        Paper.init(this);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }

}