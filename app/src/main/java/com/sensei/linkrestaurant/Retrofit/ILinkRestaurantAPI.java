package com.sensei.linkrestaurant.Retrofit;

import com.sensei.linkrestaurant.Model.AddonModel;
import com.sensei.linkrestaurant.Model.CreateOrderModel;
import com.sensei.linkrestaurant.Model.DiscountModel;
import com.sensei.linkrestaurant.Model.FavoriteModel;
import com.sensei.linkrestaurant.Model.FavoriteOnlyIdModel;
import com.sensei.linkrestaurant.Model.FoodModel;
import com.sensei.linkrestaurant.Model.GetKeyModel;
import com.sensei.linkrestaurant.Model.MaxOrderModel;
import com.sensei.linkrestaurant.Model.MenuModel;
import com.sensei.linkrestaurant.Model.OrderModel;
import com.sensei.linkrestaurant.Model.RestaurantModel;
import com.sensei.linkrestaurant.Model.SizeModel;
import com.sensei.linkrestaurant.Model.TokenModel;
import com.sensei.linkrestaurant.Model.UpdateOrderModel;
import com.sensei.linkrestaurant.Model.UpdateUserModel;
import com.sensei.linkrestaurant.Model.UserModel;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ILinkRestaurantAPI {

    //GET KEY
    @GET("getkey")
    Observable<GetKeyModel> getKey(@Query("fbid") String fbid);

    @GET("discount")
    Observable<DiscountModel> getDiscount(@HeaderMap Map<String, String> headers,
                                          @Query("code") String code);

    @GET("checkdiscount")
    Observable<DiscountModel> checkDiscount(@HeaderMap Map<String, String> headers,
                                          @Query("code") String code);

    @GET("user")
    Observable<UserModel> getUser(@HeaderMap Map<String, String> headers);

    @GET("restaurant")
    Observable<RestaurantModel> getRestaurant(@HeaderMap Map<String, String> headers);

    @GET("restaurantById")
    Observable<RestaurantModel> getRestaurantById(@HeaderMap Map<String, String> headers,
                                                      @Query("id") String id);

    @GET("nearbyrestaurant")
    Observable<RestaurantModel> getNearbyRestaurant(@HeaderMap Map<String, String> headers,
                                                    @Query("lat") Double lat,
                                                    @Query("lng") Double lng,
                                                    @Query("distance") int distance);

    @GET("menu")
    Observable<MenuModel> getCategories(@HeaderMap Map<String, String> headers,
                                        @Query("restaurantId") int restaurantId);

    @GET("food")
    Observable<FoodModel> getFoodOfMenu(@HeaderMap Map<String, String> headers,
                                        @Query("menuId") int menuId);

    @GET("foodById")
    Observable<FoodModel> getFoodById(@HeaderMap Map<String, String> headers,
                                      @Query("foodId") int foodId);

    @GET("searchFood")
    Observable<FoodModel> searchFood(@HeaderMap Map<String, String> headers,
                                     @Query("foodName") String foodName,
                                     @Query("menuId") int menuId);

    @GET("size")
    Observable<SizeModel> getSizeOfFood(@HeaderMap Map<String, String> headers,
                                        @Query("foodId") int foodId);

    @GET("addon")
    Observable<AddonModel> getAddonOfFood(@HeaderMap Map<String, String> headers,
                                          @Query("foodId") int foodId);

    @GET("favorite")
    Observable<FavoriteModel> getFavoriteByUser (@HeaderMap Map<String, String> headers);

    @GET("favoriteByRestaurant")
    Observable<FavoriteOnlyIdModel> getFavoriteByRestaurant(@HeaderMap Map<String, String> headers,
                                                             @Query("restaurantId") int restaurantId);

    @GET("order")
    Observable<OrderModel> getOrder (@HeaderMap Map<String, String> headers,
                                     @Query("from") int from,
                                     @Query("to") int to);

    @GET("maxorder")
    Observable<MaxOrderModel> getMaxOrder (@HeaderMap Map<String, String> headers);

    @GET("token")
    Observable<TokenModel> getToken(@HeaderMap Map<String, String> headers);

    //POST
    @POST("applydiscount")
    @FormUrlEncoded
    Observable<DiscountModel> insertDiscount(@HeaderMap Map<String, String> headers,
                                               @Field("orderPhone") String token);

    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateTokenToServer(@HeaderMap Map<String, String> headers,
                                       @Field("token") String token);


    @POST("createOrder")
    @FormUrlEncoded
    Observable<CreateOrderModel> createOrder(@HeaderMap Map<String, String> headers,
                                             @Field("orderPhone") String orderPhone,
                                             @Field("orderName") String orderName,
                                             @Field("orderAddress") String orderAddress,
                                             @Field("orderDate") String orderDate,
                                             @Field("restaurantId") int restaurantId,
                                             @Field("transactionId") String transactionId,
                                             @Field("cod") boolean cod,
                                             @Field("totalPrice") Double totalPrice,
                                             @Field("numberOfItem") int numberOfItem);

    @POST("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrder(@HeaderMap Map<String, String> headers,
                                             @Field("orderId") String orderId,
                                             @Field("orderDetail") String orderDetail);

    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel> updateUserInfo(@HeaderMap Map<String, String> headers,
                                               @Field("userPhone") String userPhone,
                                               @Field("userName") String userName,
                                               @Field("userAddress") String userAddress);

    @POST("favorite")
    @FormUrlEncoded
    Observable<FavoriteModel> insertFavorite   (@HeaderMap Map<String, String> headers,
                                                @Field("foodId") int foodId,
                                                @Field("restaurantId") int restaurantId,
                                                @Field("restaurantName") String restaurantName,
                                                @Field("foodName") String foodName,
                                                @Field("foodImage") String foodImage,
                                                @Field("price") double price);



    //DELETE
    @DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@HeaderMap Map<String, String> headers,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);
}
