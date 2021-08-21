package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Model.TokenModel;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    private static final int APP_REQUEST_CODE = 1234;
    CallbackManager callbackManager;
    private static final String EMAIL = "email";
    LoginButton loginButton;

    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void logInUser(){
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), APP_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppEventsLogger.activateApp(getApplication());
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Collections.singletonList(EMAIL));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();

        listener = firebaseAuth1 -> {
            FirebaseUser user = firebaseAuth1.getCurrentUser();
            if (user != null){

                dialog.show();

                Paper.book().write(Common.REMEMBER_FBID, user.getUid());

                FirebaseInstallations.getInstance()
                        .getToken(true)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "[GET TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                        if (task.isSuccessful()) {

                            dialog.show();

                            compositeDisposable.add((Disposable) iLinkRestaurantAPI.updateTokenToServer(Common.API_KEY,
                                    user.getUid(),
                                    task.getResult().getToken())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<TokenModel>() {
                                        @Override
                                        public void accept(TokenModel tokenModel) throws Exception {
                                            if (!tokenModel.isSuccess()) {
                                                Toast.makeText(MainActivity.this, "[UPDATE TOKEN ERROR]" + tokenModel.getMessage(), Toast.LENGTH_SHORT).show();

                                                compositeDisposable.add(iLinkRestaurantAPI.getUser(Common.API_KEY, user.getUid())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(userModel -> {
                                                                    // If user already exists
                                                                    if (userModel.isSuccess()) {
                                                                        Common.currentUser = userModel.getResult().get(0);
                                                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                                        startActivity(intent);
                                                                    }
                                                                    finish();
                                                                    dialog.dismiss();
                                                                },
                                                                throwable -> {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(MainActivity.this, "{GET USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }));
                                            }
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(MainActivity.this, "[UPDATE TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            );

                        }
                    }
                });

            }else{
                logInUser();
            }
        };

        Paper.init(this);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == APP_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (listener != null && firebaseAuth != null)
            firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null && firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }
}