package com.sensei.linkrestaurant.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sensei.linkrestaurant.Model.Addon;
import com.sensei.linkrestaurant.Model.Favorite;
import com.sensei.linkrestaurant.Model.FavoriteOnlyId;
import com.sensei.linkrestaurant.Model.Restaurant;
import com.sensei.linkrestaurant.Model.User;
import com.sensei.linkrestaurant.R;
import com.sensei.linkrestaurant.Retrofit.IFMCService;
import com.sensei.linkrestaurant.Retrofit.RetrofitClient;
import com.sensei.linkrestaurant.Service.MyFirebaseMessagingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String API_RESTAURANT_ENDPOINT = "https://ilinkrestaurant-android-app.azurewebsites.net/";
    public static final String QR_CODE_TAG = "QR_CODE";
    public static String API_KEY = "";
    public static final int DEFAULT_COLUMN_COUNT = 1;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String REMEMBER_FBID = "REMEMBER FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFI_TITLE ="title" ;
    public static final String NOTIFI_CONTENT = "content";

    public static User currentUser;
    public static Restaurant currentRestaurant;
    public static Set<Addon> addonList = new HashSet<> ();
    public static List<FavoriteOnlyId> currentFavOfRestaurant;

    public static IFMCService getIFMCSercive(){
        return RetrofitClient.getInstance("https://fcm.googleapis.com/").create(IFMCService.class);
    }

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

    public static void showNotification(Context context, int notiId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        String NOTIFICATION_CHANNEL_ID = "Lint Restaurant";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,notiId,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            "LintRestaurant Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                    notificationChannel.setDescription("Link Restaurant Client App");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);

                    notificationManager.createNotificationChannel(notificationChannel);
                }



        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));

        if (pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }

        Notification nNotification = builder.build();

        notificationManager.notify(notiId,nNotification);
    }

    public static String buildJWT(String apiKey) {
        return new StringBuilder("Bearer")
                .append(" ")
                .append(apiKey).toString();
    }

    public static String getTopicChannel(int id) {
        return new StringBuilder("Restaurant_").append(id).toString();
    }

    public static String createTopicSender(String topicChannel) {
        return new StringBuilder("/topics/").append(topicChannel).toString();
    }
}
