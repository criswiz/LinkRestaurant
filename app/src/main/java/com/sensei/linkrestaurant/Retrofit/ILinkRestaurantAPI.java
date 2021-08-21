package com.sensei.linkrestaurant.Retrofit;

import com.sensei.linkrestaurant.Model.AddonModel;
import com.sensei.linkrestaurant.Model.CreateOrderModel;
import com.sensei.linkrestaurant.Model.FavoriteModel;
import com.sensei.linkrestaurant.Model.FavoriteOnlyIdModel;
import com.sensei.linkrestaurant.Model.FoodModel;
import com.sensei.linkrestaurant.Model.MaxOrderModel;
import com.sensei.linkrestaurant.Model.MenuModel;
import com.sensei.linkrestaurant.Model.OrderModel;
import com.sensei.linkrestaurant.Model.RestaurantModel;
import com.sensei.linkrestaurant.Model.SizeModel;
import com.sensei.linkrestaurant.Model.TokenModel;
import com.sensei.linkrestaurant.Model.UpdateOrderModel;
import com.sensei.linkrestaurant.Model.UpdateUserModel;
import com.sensei.linkrestaurant.Model.UserModel;

import io.reactivex.Observable;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ILinkRestaurantAPI {
    @GET("user")
    Observable<UserModel> getUser(@Query("key") String apiKey,
                                  @Query("fbid") String fbid);

    @GET("restaurant")
    Observable<RestaurantModel> getRestaurant(@Query("key") String apiKey);

    @GET("restaurantById")
    Observable<RestaurantModel> getRestaurantById(@Query("key") String apiKey,
                                                      @Query("restaurantId") String id);

    @GET("nearbyrestaurant")
    Observable<RestaurantModel> getNearbyRestaurant(@Query("key") String apiKey,
                                                    @Query("lat") Double lat,
                                                    @Query("lng") Double lng,
                                                    @Query("distance") int distance);

    @GET("menu")
    Observable<MenuModel> getCategories(@Query("key") String apiKey,
                                        @Query("restaurantId") int restaurantId);

    @GET("food")
    Observable<FoodModel> getFoodOfMenu(@Query("key") String apiKey,
                                        @Query("menuId") int menuId);

    @GET("foodById")
    Observable<FoodModel> getFoodById(@Query("key") String apiKey,
                                      @Query("foodId") int foodId);

    @GET("searchFood")
    Observable<FoodModel> searchFood(@Query("key") String apiKey,
                                     @Query("foodName") String foodName,
                                     @Query("menuId") int menuId);

    @GET("size")
    Observable<SizeModel> getSizeOfFood(@Query("key") String apiKey,
                                        @Query("foodId") int foodId);

    @GET("addon")
    Observable<AddonModel> getAddonOfFood(@Query("key") String apiKey,
                                          @Query("foodId") int foodId);

    @GET("favorite")
    Observable<FavoriteModel> getFavoriteByUser (@Query("key") String apiKey,
                                                 @Query("fbid") String fbid);

    @GET("favoriteByID")
    Observable<FavoriteOnlyIdModel> getFavoriteByRestaurant (@Query("key") String apiKey,
                                                             @Query("fbid") String fbid,
                                                             @Query("restaurantId") int restaurantId);

    @GET("order")
    Observable<OrderModel> getOrder (@Query("key") String apiKey,
                                     @Query("orderFBID") String orderFBID,
                                     @Query("from") int from,
                                     @Query("to") int to);

    @GET("maxorder")
    Observable<MaxOrderModel> getMaxOrder (@Query("key") String apiKey,
                                           @Query("orderFBID") String orderFBID);

    @GET("token")
    Observable<TokenModel> getToken (@Query("key") String apiKey,
                                           @Query("FBID") String FBID);

    //POST
    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateTokenToServer(@Field("key") String apiKey,
                                       @Field("orderFBID") String orderFBID,
                                       @Field("orderPhone") String token);


    @POST("createOrder")
    @FormUrlEncoded
    Observable<CreateOrderModel> createOrder(@Field("key") String apiKey,
                                             @Field("orderFBID") String orderFBID,
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
    Observable<UpdateOrderModel> updateOrder(@Field("key") String apiKey,
                                             @Field("orderId") String orderId,
                                             @Field("orderDetail") String orderDetail);

    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel> updateUserInfo(@Field("key") String apiKey,
                                               @Field("userPhone") String userPhone,
                                               @Field("userName") String userName,
                                               @Field("userAddress") String userAddress,
                                               @Field("fbid") String fbid);

    @POST("favorite")
    @FormUrlEncoded
    Observable<FavoriteModel> insertFavorite   (@Field("key") String apiKey,
                                                @Field("fbid") String fbid,
                                                @Field("foodId") int foodId,
                                                @Field("retaurantId") int restaurantId,
                                                @Field("restaurantName") String restaurantName,
                                                @Field("foodName") String foodName,
                                                @Field("foodImage") String foodImage,
                                                @Field("price") double price);

    //DELETE
    @DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@Query("key") String apiKey,
                                             @Query("fbid") String fbid,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);
}
