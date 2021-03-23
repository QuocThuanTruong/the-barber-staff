package com.qtt.barberstaffapp.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qtt.barberstaffapp.Model.Barber;
import com.qtt.barberstaffapp.Model.BarberServices;
import com.qtt.barberstaffapp.Model.BookingInformation;
import com.qtt.barberstaffapp.Model.MyToken;
import com.qtt.barberstaffapp.Model.Salon;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.Service.MyFCMService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import io.paperdb.Paper;

public class Common {
    public static final String KEY_DISABLE = "keyDisable";
    public static final int TIME_SLOT_TOTAL = 20;
    public static final String LOGED_KEY = "logedKey";
    public static final String STATE_KEY = "stateKey";
    public static final String SALON_KEY = "salonKey";
    public static final String BARBER_KEY = "barberKey";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "content";
    public static final int MAX_NOTI_PER_LOAD = 10;
    public static final String SERVICES_ADDED = "servicesAdded";
    public static final double DEFAULT_PRICE = 30;
    public static final String SHOPPING_LIST = "shoppingList";
    public static final String IMG_URL = "imgURL";
    public static final String RATING_STATE_KEY = "ratingStateKey";
    public static final String RATING_SALON_ID = "ratingSalonId";
    public static final String RATING_SALON_NAME = "ratingSalonName";
    public static final String RATING_BARBER_ID = "ratingBarberId";
    public static String stateName = "";
    public static Salon selectedSalon = null;
    public static Barber currentBarber = null;
    public static int currentTimeSlot = -1;
    public static Calendar bookingDate = Calendar.getInstance();
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
    public static BookingInformation currentBookingInfo = null;
    public static BarberServices selectedService = null;

    public static String convertTimeSlotToString(int position) {
        switch (position) {
            case 0:
                return "9:00 - 9:30";
            case 1:
                return "9:30 - 10:00";
            case 2:
                return "10:00 - 10:30";
            case 3:
                return "10:30 - 11:00";
            case 4:
                return "11:00 - 11:30";
            case 5:
                return "11:30 - 12:00";
            case 6:
                return "12:00 - 12:30";
            case 7:
                return "12:30 - 13:00";
            case 8:
                return "13:00 - 13:30";
            case 9:
                return "13:30 - 14:00";
            case 10:
                return "14:00 - 14:30";
            case 11:
                return "14:30 - 15:00";
            case 12:
                return "15:00 - 15:30";
            case 13:
                return "15:30 - 16:00";
            case 14:
                return "16:00 - 16:30";
            case 15:
                return "16:30 - 17:00";
            case 16:
                return "17:00 - 17:30";
            case 17:
                return "17:30 - 18:00";
            case 18:
                return "18:00 - 18:30";
            case 19:
                return "18:30 - 19:00";
            case 20:
                return "19:00 - 19:30";
            default:
                return "Closed";
        }
    }

    public static void updateToken(Context context, String s) {
        //store user token (barber's)
        Paper.init(context);
        String user = Paper.book().read(Common.LOGED_KEY);
        if (user != null) {
            if (!TextUtils.isEmpty(user)) {
                MyToken myToken = new MyToken();
                myToken.setToken(s);
                myToken.setTokenType(TOKEN_TYPE.BARBER);
                myToken.setUserPhone(user);

                FirebaseFirestore.getInstance()
                        .collection("Tokens")
                        .document(Common.currentBarber.getBarberId())
                        .set(myToken)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
            }
        }
    }

    public static String formatShoppingItemName(String name) {
        return name.length() > 13 ? new StringBuilder(name.substring(0, 10)).append("...").toString() : name;
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if (fileUri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        if (result == null) {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    public enum TOKEN_TYPE {
        CLIENT, BARBER, MANAGER
    }

    public static void showNotification(Context context, int notiId, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;

        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    notiId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        String NOTIFICATION_CHANEL_ID = "my_chanel_id_012";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANEL_ID, "Barber Booking Staff App", NotificationManager.IMPORTANCE_DEFAULT);

            //Config notification chanel
            notificationChannel.setDescription("Staff App");
            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setVibrationPattern(new long[] {0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();

        notificationManager.notify(notiId, notification);
    }
}
