package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateInfoActivity extends AppCompatActivity {

    ILinkRestaurantAPI iLinkRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @BindView(R.id.edt_username)
    EditText edt_username;
    @BindView(R.id.edt_userAddress)
    EditText edt_userAddress;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        init();
        initView();
    }

    // Override back arrow
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void initView() {
        toolbar.setTitle(getString(R.string.update_information));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                    compositeDisposable.add(iLinkRestaurantAPI.updateUserInfo(headers,
                            user.getPhoneNumber(),
                            edt_username.getText().toString(),
                            edt_userAddress.getText().toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(updateUserModel -> {

                                if (updateUserModel.isSuccess()) {
                                    // If user has been update, just refresh again
                                    compositeDisposable.add(iLinkRestaurantAPI.getUser(headers)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(userModel -> {

                                                if (userModel.isSuccess()) {
                                                    Common.currentUser = userModel.getResult().get(0);
                                                    startActivity(new Intent(UpdateInfoActivity.this, HomeActivity.class));
                                                    finish();
                                                }
                                                else {
                                                    Toast.makeText(UpdateInfoActivity.this, "[GET USER RESULT]"+userModel.getResult().get(0), Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();

                                            }, throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(UpdateInfoActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                                }
                                else {
                                    dialog.dismiss();
                                    Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API RETURN]" + updateUserModel.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }, throwable -> {
                                dialog.dismiss();
                                Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
                }else {
                    Toast.makeText(UpdateInfoActivity.this, "Not Signed in Please SignIn", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateInfoActivity.this, MainActivity.class));
                    finish();
                }


            }
        });

        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getName()))
            edt_username.setText(Common.currentUser.getName());
        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getAddress()))
            edt_userAddress.setText(Common.currentUser.getAddress());
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
    }
}