package com.sensei.linkrestaurant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.gson.Gson;
import com.sensei.linkrestaurant.Common.Common;
import com.sensei.linkrestaurant.Database.CartDataSource;
import com.sensei.linkrestaurant.Database.CartDatabase;
import com.sensei.linkrestaurant.Database.CartItem;
import com.sensei.linkrestaurant.Database.LocalCartDataSource;
import com.sensei.linkrestaurant.Model.BraintreeToken;
import com.sensei.linkrestaurant.Model.BraintreeTransaction;
import com.sensei.linkrestaurant.Model.CreateOrderModel;
import com.sensei.linkrestaurant.Model.Discount;
import com.sensei.linkrestaurant.Model.DiscountModel;
import com.sensei.linkrestaurant.Model.EventBus.SendTotalCashEvent;
import com.sensei.linkrestaurant.Model.FCMResponse;
import com.sensei.linkrestaurant.Model.FCMSendData;
import com.sensei.linkrestaurant.Model.UpdateOrderModel;
import com.sensei.linkrestaurant.Retrofit.IBraintreeAPI;
import com.sensei.linkrestaurant.Retrofit.IFMCService;
import com.sensei.linkrestaurant.Retrofit.ILinkRestaurantAPI;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.sensei.linkrestaurant.Retrofit.RetrofitFCMClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final int REQUEST_BRAINTREE_CODE = 7777;
    private static final int SCAN_QR_PERMISSION = 0000;

    boolean isUseDiscount = false;
    String discountUser = "";

    @BindView(R.id.btn_scan_qr)
    Button btn_scan_qr;
    @BindView(R.id.btn_apply)
    Button btn_apply;
    @BindView(R.id.edt_discount_code)
    EditText edt_discount_code;
    @BindView(R.id.txt_discount_cash)
    TextView txt_discount_cash;
    @BindView(R.id.edt_date)
    EditText edt_date;
    @BindView(R.id.txt_total_cash)
    TextView txt_total_cash;
    @BindView(R.id.txt_user_phone)
    TextView txt_user_phone;
    @BindView(R.id.txt_user_address)
    TextView txt_user_address;
    @BindView(R.id.txt_new_address)
    TextView txt_new_address;
    @BindView(R.id.btn_add_new_address)
    Button btn_add_new_address;
    @BindView(R.id.ckb_default_address)
    CheckBox ckb_default_address;
    @BindView(R.id.rdi_cod)
    RadioButton rdi_cod;
    @BindView(R.id.rdi_online_payment)
    RadioButton rdi_online_payment;
    @BindView(R.id.btn_proceed)
    Button btn_proceed;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    IFMCService ifmcService;
    ILinkRestaurantAPI iLinkRestaurantAPI;
    IBraintreeAPI iBraintreeAPI;
    AlertDialog dialog;
    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    boolean isSelectedDate = false, isAddNewAddress = false;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        init();
        initView();

    }

    private void init(){
        ifmcService = RetrofitFCMClient.getInstance().create(IFMCService.class);
        iLinkRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(ILinkRestaurantAPI.class);
        iBraintreeAPI = RetrofitClient.getInstance(Common.currentRestaurant.getPaymentUrl()).create(IBraintreeAPI.class);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
    }

    private void initView() {
        ButterKnife.bind(this);

        txt_user_phone.setText(Common.currentUser.getUserPhone());
        txt_user_address.setText(Common.currentUser.getAddress());

        toolbar.setTitle(getString(R.string.place_order));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_scan_qr.setOnClickListener(v ->
                startActivityForResult(new Intent(PlaceOrderActivity.this, ScanQRActivity.class), SCAN_QR_PERMISSION)
        );

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                compositeDisposable.add(
                        iLinkRestaurantAPI.checkDiscount(headers, edt_discount_code.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<DiscountModel>() {
                            @Override
                            public void accept(DiscountModel discountModel) throws Exception {
                                if (discountModel.isSuccess()){
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                    compositeDisposable.add(
                                            iLinkRestaurantAPI.getDiscount(headers, edt_discount_code.getText().toString())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Consumer<DiscountModel>() {
                                                        @Override
                                                        public void accept(DiscountModel discountModel) throws Exception {
                                                            if (discountModel.isSuccess()){
                                                                if (!isUseDiscount) {
                                                                    Discount discount = discountModel.getMessage().get(0);
                                                                    Double total_cash = Double.valueOf(txt_total_cash.getText().toString());
                                                                    Double discount_value = (total_cash * discount.getValue()) / 100;
                                                                    Double final_cash = total_cash - discount_value;

                                                                    txt_discount_cash.setText(new StringBuilder("(-")
                                                                            .append(discount.getValue())
                                                                            .append("%) "));

                                                                    txt_discount_cash.setVisibility(View.VISIBLE);

                                                                    txt_total_cash.setText(new StringBuilder("")
                                                                            .append(final_cash));

                                                                    isUseDiscount = true;
                                                                    discountUser = edt_discount_code.getText().toString();
                                                                    btn_apply.setEnabled(false);
                                                                }else{
                                                                    Toast.makeText(PlaceOrderActivity.this, "Discount Applied Already", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                            dialog.dismiss();
                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(Throwable throwable) throws Exception {
                                                            Toast.makeText(PlaceOrderActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                        }
                                                    }));
                                }else {
                                    Toast.makeText(PlaceOrderActivity.this, "You already use this code", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(PlaceOrderActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }));
            }
        });

        btn_add_new_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddNewAddress = true;
                ckb_default_address.setChecked(false);

                View layout_add_new_address = LayoutInflater.from(PlaceOrderActivity.this)
                        .inflate(R.layout.layout_add_new_address, null);

                EditText edt_new_address = layout_add_new_address.findViewById(R.id.edt_add_new_address);

                edt_new_address.setText(txt_new_address.getText().toString());

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PlaceOrderActivity.this)
                        .setTitle("Add New Address")
                        .setView(layout_add_new_address)
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                txt_new_address.setText(edt_new_address.getText().toString());
                            }
                        });

                androidx.appcompat.app.AlertDialog addNewAddressDialog = builder.create();
                addNewAddressDialog.show();
            }
        });

        edt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now  = Calendar.getInstance();

                DatePickerDialog dpd = DatePickerDialog.newInstance(PlaceOrderActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONDAY),
                        now.get(Calendar.DAY_OF_MONTH));

                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSelectedDate){
                    Toast.makeText(PlaceOrderActivity.this, "Please select date", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    String dateString = edt_date.getText().toString();
                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    try {
                        Date order = df.parse(dateString);

                        //Get Current Date
                        Calendar calendar = Calendar.getInstance();

                        Date currentDate = df.parse(df.format(calendar.getTime()));

                        if (!DateUtils.isToday(order.getTime())){
                            if (order.before(currentDate)){
                                Toast.makeText(PlaceOrderActivity.this, "Please select a current date or future date", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (!isAddNewAddress){
                    if (!ckb_default_address.isChecked()){
                        Toast.makeText(PlaceOrderActivity.this, "Please choose default Address or set new Address", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (rdi_cod.isChecked()){
                    getOrderNumber(false);
                }else if (rdi_online_payment.isChecked()){
                    getOrderNumber(true);
                }
            }
        });
    }

    private void getOrderNumber(boolean isOnlinePayment) {
        dialog.show();
        if (!isOnlinePayment){
            String address = ckb_default_address.isChecked() ? txt_user_address.getText().toString():txt_new_address.getText().toString();

            compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),
                    Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CartItem>>() {
                    @Override
                    public void accept(List<CartItem> cartItems) throws Exception {
                        //Get order number from server

                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                        compositeDisposable.add(
                                iLinkRestaurantAPI.createOrder(headers,
                                        Common.currentUser.getUserPhone(),
                                        Common.currentUser.getName(),
                                        address,
                                        edt_date.getText().toString(),
                                        Common.currentRestaurant.getId(),
                                        "NONE",
                                        true,
                                        Double.valueOf(txt_total_cash.getText().toString()),
                                        cartItems.size())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<CreateOrderModel>() {
                                    @Override
                                    public void accept(CreateOrderModel createOrderModel) throws Exception {
                                        if (createOrderModel.isSuccess()){
                                        compositeDisposable.add(iLinkRestaurantAPI.updateOrder(headers,
                                                String.valueOf(createOrderModel.getMessage().get(0).getOrderNumber()),
                                                new Gson().toJson(cartItems))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<UpdateOrderModel>() {
                                                    @Override
                                                    public void accept(UpdateOrderModel updateOrderModel) throws Exception {
                                                        //Get order number and update to order detail
                                                        //First select cart items
                                                        if (updateOrderModel.isSuccess()){
                                                            if (isUseDiscount && !TextUtils.isEmpty(discountUser)){

                                                                compositeDisposable.add(iLinkRestaurantAPI.insertDiscount(headers,
                                                                        discountUser)
                                                                        .subscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(new Consumer<DiscountModel>() {
                                                                            @Override
                                                                            public void accept(DiscountModel discountModel) throws Exception {
                                                                                if (discountModel.isSuccess()){
                                                                                    cartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                                                            Common.currentRestaurant.getId())
                                                                                            .subscribeOn(Schedulers.io())
                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                            .subscribe(new SingleObserver<Integer>() {
                                                                                                @Override
                                                                                                public void onSubscribe(@NonNull Disposable d) {

                                                                                                }

                                                                                                @Override
                                                                                                public void onSuccess(@NonNull Integer integer) {
                                                                                                    Map<String, String> dataSend = new HashMap<>();
                                                                                                    dataSend.put(Common.NOTIFI_TITLE, "New Order");
                                                                                                    dataSend.put(Common.NOTIFI_CONTENT, "You have new order"+createOrderModel.getMessage().get(0));

                                                                                                    FCMSendData sendData = new FCMSendData(Common.createTopicSender(Common.getTopicChannel(
                                                                                                            Common.currentRestaurant.getId()
                                                                                                    )), dataSend);

                                                                                                    compositeDisposable.add(ifmcService.sendNotification(sendData)
                                                                                                            .subscribeOn(Schedulers.io())
                                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                                            .subscribe(new Consumer<FCMResponse>() {
                                                                                                                @Override
                                                                                                                public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                                                    if (fcmResponse.getMessage_id() > 0){
                                                                                                                        Toast.makeText(PlaceOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
                                                                                                                        Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                        startActivity(intent);
                                                                                                                        finish();
                                                                                                                    }else{

                                                                                                                    }
                                                                                                                }
                                                                                                            }, new Consumer<Throwable>() {
                                                                                                                @Override
                                                                                                                public void accept(Throwable throwable) throws Exception {
                                                                                                                    Toast.makeText(PlaceOrderActivity.this, "Order Placed but can't send notification to server"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                    Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                    startActivity(intent);
                                                                                                                    finish();
                                                                                                                }
                                                                                                            }));
                                                                                                }

                                                                                                @Override
                                                                                                public void onError(@NonNull Throwable e) {
                                                                                                    Toast.makeText(PlaceOrderActivity.this, "[CLEAR CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            });

                                                                                }
                                                                            }
                                                                        }, new Consumer<Throwable>() {
                                                                            @Override
                                                                            public void accept(Throwable throwable) throws Exception {
                                                                                Toast.makeText(PlaceOrderActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }));
                                                            }else{
                                                                //update cart and clear and show success message
                                                                cartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                                        Common.currentRestaurant.getId())
                                                                        .subscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(new SingleObserver<Integer>() {
                                                                            @Override
                                                                            public void onSubscribe(@NonNull Disposable d) {

                                                                            }

                                                                            @Override
                                                                            public void onSuccess(@NonNull Integer integer) {
                                                                                Map<String, String> dataSend = new HashMap<>();
                                                                                dataSend.put(Common.NOTIFI_TITLE, "New Order");
                                                                                dataSend.put(Common.NOTIFI_CONTENT, "You have new order"+createOrderModel.getMessage().get(0));

                                                                                FCMSendData sendData = new FCMSendData(Common.createTopicSender(Common.getTopicChannel(
                                                                                        Common.currentRestaurant.getId()
                                                                                )), dataSend);

                                                                                compositeDisposable.add(ifmcService.sendNotification(sendData)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(new Consumer<FCMResponse>() {
                                                                                            @Override
                                                                                            public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                                if (fcmResponse.getMessage_id() > 0){
                                                                                                    Toast.makeText(PlaceOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
                                                                                                    Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                    startActivity(intent);
                                                                                                    finish();
                                                                                                }else{

                                                                                                }
                                                                                            }
                                                                                        }, new Consumer<Throwable>() {
                                                                                            @Override
                                                                                            public void accept(Throwable throwable) throws Exception {
                                                                                                Toast.makeText(PlaceOrderActivity.this, "Order Placed but can't send notification to server"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                startActivity(intent);
                                                                                                finish();
                                                                                            }
                                                                                        }));
                                                                            }

                                                                            @Override
                                                                            public void onError(@NonNull Throwable e) {
                                                                                Toast.makeText(PlaceOrderActivity.this, "[CLEAR CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                        if (dialog.isShowing()){
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        dialog.dismiss();
                                                        //Toast.makeText(PlaceOrderActivity.this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                        );
                                    }else {
                                            dialog.dismiss();
                                            Toast.makeText(PlaceOrderActivity.this, "[CREATE ORDER]"+createOrderModel.getResult(), Toast.LENGTH_SHORT).show();
                                        }
                                  }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        dialog.dismiss();
                                        Toast.makeText(PlaceOrderActivity.this, "[CREATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                        );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(PlaceOrderActivity.this, "[GET ALL CART]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));

        }
        else {
            compositeDisposable.add(iBraintreeAPI.getToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<BraintreeToken>() {
                @Override
                public void accept(BraintreeToken braintreeToken) throws Exception {
                    if (braintreeToken.isSuccess()){

                        DropInRequest dropInRequest = new DropInRequest().clientToken(braintreeToken.getClientToken());
                        startActivityForResult(dropInRequest.getIntent(PlaceOrderActivity.this), REQUEST_BRAINTREE_CODE);
                    }else {
                        Toast.makeText(PlaceOrderActivity.this, "Cannot get token", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    dialog.dismiss();
                    Toast.makeText(PlaceOrderActivity.this, "[GET TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        isSelectedDate = true;

        edt_date.setText(new StringBuilder("")
        .append(monthOfYear + 1)
        .append("/")
        .append(dayOfMonth)
        .append("/")
        .append(year));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BRAINTREE_CODE){
            if (resultCode == RESULT_OK){
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();

                if (!TextUtils.isEmpty(txt_total_cash.getText().toString())){
                    String amount = txt_total_cash.getText().toString();

                    if (!dialog.isShowing())
                        dialog.show();

                    String address = ckb_default_address.isChecked() ? txt_user_address.getText().toString():txt_new_address.getText().toString();

                    compositeDisposable.add(iBraintreeAPI.submitPayment(amount, nonce.getNonce())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BraintreeTransaction>() {
                        @Override
                        public void accept(BraintreeTransaction braintreeTransaction) throws Exception {
                            if (braintreeTransaction.isSuccess()){
                                if (!dialog.isShowing())
                                    dialog.show();

                                compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),
                                        Common.currentRestaurant.getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<List<CartItem>>() {
                                            @Override
                                            public void accept(List<CartItem> cartItems) throws Exception {
                                                //Get order number from server

                                                Map<String, String> headers = new HashMap<>();
                                                headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                                compositeDisposable.add(
                                                        iLinkRestaurantAPI.createOrder(headers,
                                                                Common.currentUser.getUserPhone(),
                                                                Common.currentUser.getName(),
                                                                address,
                                                                edt_date.getText().toString(),
                                                                Common.currentRestaurant.getId(),
                                                                braintreeTransaction.getTransaction().getId(),
                                                                false,
                                                                Double.valueOf(amount),
                                                                cartItems.size())
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Consumer<CreateOrderModel>() {
                                                                    @Override
                                                                    public void accept(CreateOrderModel createOrderModel) throws Exception {
                                                                        if (createOrderModel.isSuccess()){

                                                                            Map<String, String> headers = new HashMap<>();
                                                                            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                                                            compositeDisposable.add(iLinkRestaurantAPI.updateOrder(headers,
                                                                                    String.valueOf(createOrderModel.getMessage().get(0).getOrderNumber()),
                                                                                    new Gson().toJson(cartItems))
                                                                                    .subscribeOn(Schedulers.io())
                                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                                    .subscribe(new Consumer<UpdateOrderModel>() {
                                                                                        @Override
                                                                                        public void accept(UpdateOrderModel updateOrderModel) throws Exception {
                                                                                            //Get order number and update to order detail
                                                                                            //First select cart items
                                                                                            if (updateOrderModel.isSuccess()){
                                                                                                if (isUseDiscount && !TextUtils.isEmpty(discountUser)){

                                                                                                    compositeDisposable.add(iLinkRestaurantAPI.insertDiscount(headers,
                                                                                                            discountUser)
                                                                                                            .subscribeOn(Schedulers.io())
                                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                                            .subscribe(new Consumer<DiscountModel>() {
                                                                                                                @Override
                                                                                                                public void accept(DiscountModel discountModel) throws Exception {
                                                                                                                    if (discountModel.isSuccess()){
                                                                                                                        cartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                                                                                                Common.currentRestaurant.getId())
                                                                                                                                .subscribeOn(Schedulers.io())
                                                                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                                                                .subscribe(new SingleObserver<Integer>() {
                                                                                                                                    @Override
                                                                                                                                    public void onSubscribe(@NonNull Disposable d) {

                                                                                                                                    }

                                                                                                                                    @Override
                                                                                                                                    public void onSuccess(@NonNull Integer integer) {
                                                                                                                                        Map<String, String> dataSend = new HashMap<>();
                                                                                                                                        dataSend.put(Common.NOTIFI_TITLE, "New Order");
                                                                                                                                        dataSend.put(Common.NOTIFI_CONTENT, "You have new order"+createOrderModel.getMessage().get(0));

                                                                                                                                        FCMSendData sendData = new FCMSendData(Common.createTopicSender(Common.getTopicChannel(
                                                                                                                                                Common.currentRestaurant.getId()
                                                                                                                                        )), dataSend);

                                                                                                                                        compositeDisposable.add(ifmcService.sendNotification(sendData)
                                                                                                                                                .subscribeOn(Schedulers.io())
                                                                                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                                                                                .subscribe(new Consumer<FCMResponse>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                                                                                        if (fcmResponse.getMessage_id() > 0){
                                                                                                                                                            Toast.makeText(PlaceOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
                                                                                                                                                            Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                                                            startActivity(intent);
                                                                                                                                                            finish();
                                                                                                                                                        }else{

                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }, new Consumer<Throwable>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void accept(Throwable throwable) throws Exception {
                                                                                                                                                        Toast.makeText(PlaceOrderActivity.this, "Order Placed but can't send notification to server"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                                        Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                                                        startActivity(intent);
                                                                                                                                                        finish();
                                                                                                                                                    }
                                                                                                                                                }));
                                                                                                                                    }

                                                                                                                                    @Override
                                                                                                                                    public void onError(@NonNull Throwable e) {
                                                                                                                                        Toast.makeText(PlaceOrderActivity.this, "[CLEAR CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                    }
                                                                                                                                });

                                                                                                                    }
                                                                                                                }
                                                                                                            }, new Consumer<Throwable>() {
                                                                                                                @Override
                                                                                                                public void accept(Throwable throwable) throws Exception {
                                                                                                                    Toast.makeText(PlaceOrderActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }));
                                                                                                }else{
                                                                                                    //update cart and clear and show success message
                                                                                                    cartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                                                                            Common.currentRestaurant.getId())
                                                                                                            .subscribeOn(Schedulers.io())
                                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                                            .subscribe(new SingleObserver<Integer>() {
                                                                                                                @Override
                                                                                                                public void onSubscribe(@NonNull Disposable d) {

                                                                                                                }

                                                                                                                @Override
                                                                                                                public void onSuccess(@NonNull Integer integer) {
                                                                                                                    Map<String, String> dataSend = new HashMap<>();
                                                                                                                    dataSend.put(Common.NOTIFI_TITLE, "New Order");
                                                                                                                    dataSend.put(Common.NOTIFI_CONTENT, "You have new order"+createOrderModel.getMessage().get(0));

                                                                                                                    FCMSendData sendData = new FCMSendData(Common.createTopicSender(Common.getTopicChannel(
                                                                                                                            Common.currentRestaurant.getId()
                                                                                                                    )), dataSend);

                                                                                                                    compositeDisposable.add(ifmcService.sendNotification(sendData)
                                                                                                                            .subscribeOn(Schedulers.io())
                                                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                                                            .subscribe(new Consumer<FCMResponse>() {
                                                                                                                                @Override
                                                                                                                                public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                                                                    if (fcmResponse.getMessage_id() > 0){
                                                                                                                                        Toast.makeText(PlaceOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
                                                                                                                                        Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                                        startActivity(intent);
                                                                                                                                        finish();
                                                                                                                                    }else{

                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }, new Consumer<Throwable>() {
                                                                                                                                @Override
                                                                                                                                public void accept(Throwable throwable) throws Exception {
                                                                                                                                    Toast.makeText(PlaceOrderActivity.this, "Order Placed but can't send notification to server"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                    Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                                                                                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                                    startActivity(intent);
                                                                                                                                    finish();
                                                                                                                                }
                                                                                                                            }));
                                                                                                                }

                                                                                                                @Override
                                                                                                                public void onError(@NonNull Throwable e) {
                                                                                                                    Toast.makeText(PlaceOrderActivity.this, "[CLEAR CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }

                                                                                            if (dialog.isShowing()){
                                                                                                dialog.dismiss();
                                                                                            }
                                                                                        }
                                                                                    }, new Consumer<Throwable>() {
                                                                                        @Override
                                                                                        public void accept(Throwable throwable) throws Exception {
                                                                                            dialog.dismiss();
                                                                                            Toast.makeText(PlaceOrderActivity.this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    })
                                                                            );
                                                                        }else {
                                                                            dialog.dismiss();
                                                                            Toast.makeText(PlaceOrderActivity.this, "[CREATE ORDER]"+createOrderModel.getResult(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                }, new Consumer<Throwable>() {
                                                                    @Override
                                                                    public void accept(Throwable throwable) throws Exception {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(PlaceOrderActivity.this, "[CREATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                );
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                Toast.makeText(PlaceOrderActivity.this, "[GET ALL CART]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }));
                            }else {
                                dialog.dismiss();
                                Toast.makeText(PlaceOrderActivity.this,  "Transaction Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            Toast.makeText(PlaceOrderActivity.this, "[SUBMIT PAYMENT]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
                }
            }
        }
        else if(requestCode == SCAN_QR_PERMISSION){
            if (resultCode == RESULT_OK){
                edt_discount_code.setText(data.getStringExtra(Common.QR_CODE_TAG));
                btn_apply.setEnabled(true);
            }
        }
    }

    //EventBus

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setTotalCash(SendTotalCashEvent event){
        txt_total_cash.setText(String.valueOf(event.getCash()));
    }
}