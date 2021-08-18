package com.sensei.linkrestaurant.Common;

import com.sensei.linkrestaurant.Model.Addon;
import com.sensei.linkrestaurant.Model.Favorite;
import com.sensei.linkrestaurant.Model.FavoriteOnlyId;
import com.sensei.linkrestaurant.Model.Restaurant;
import com.sensei.linkrestaurant.Model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String API_RESTAURANT_ENDPOINT = "https://ilinkrestaurant-android-app.azurewebsites.net";
    public static final String API_KEY = "1234";
    public static final int DEFAULT_COLUMN_COUNT = 1;
    public static final int FULL_WIDTH_COLUMN = 1;

    public static User currentUser;
    public static Restaurant currentRestaurant;
    public static Set<Addon> addonList = new HashSet<> ();
    public static List<FavoriteOnlyId> currentFavOfRestaurant;

    public static boolean checkFavorite(int id) {
        boolean result = false;
        for (FavoriteOnlyId item: currentFavOfRestaurant)
            if (item.getFoodId() == id) {
                result = true;
            }
        return result;
    }

    public static void removeFavorite(int id) {
        for (FavoriteOnlyId item: currentFavOfRestaurant)
            if (item.getFoodId() == id) {
                currentFavOfRestaurant.remove(item);
            }
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancel";
        }
    }
}
