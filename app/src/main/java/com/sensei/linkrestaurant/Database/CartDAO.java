package com.sensei.linkrestaurant.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
    //Load cart by restaurant id
    //every restaurant has a unique receipt
    //every restaurant has different gateway so unique carts for each

    @Query("SELECT * FROM Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Flowable<List<CartItem>> getAllCart(String fbid, int restaurantId);

    @Query("SELECT COUNT(*) from Cart where fbid=:fbid AND restaurantId=:restaurantId")
    Single<Integer> countItemInCart(String fbid, int restaurantId);

    @Query("SELECT SUM (foodPrice * foodQuantity) + (foodExtraPrice * foodQuantity) from Cart where fbid=:fbid AND restaurantId=:restaurantId")
    Single<Long> sumPrice(String fbid, int restaurantId);

    @Query("SELECT * from Cart where foodId=:foodId AND fbid=:fbid AND restaurantId=:restaurantId")
    Single<CartItem> getItemInCart(String foodId, String fbid, int restaurantId);

    @Insert (onConflict = OnConflictStrategy.REPLACE) //if foodId conflict update information
    Completable insertOrReplaceAll(CartItem... cartItem);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCart(CartItem cartItem);

    @Delete
    Single<Integer> deleteCart(CartItem cartItem);

    @Query("Delete from Cart Where fbid=:fbid AND restaurantId=:restaurantId")
    Single<Integer> cleanCart(String fbid, int restaurantId);

}
