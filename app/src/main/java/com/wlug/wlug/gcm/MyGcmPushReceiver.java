package com.wlug.wlug.gcm;

/**
 * Created by Inspiron on 16-06-2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.wlug.wlug.helper.DatabaseHelper;
import com.google.android.gms.gcm.GcmListenerService;
import com.wlug.wlug.activity.ChatRoomActivity;
import com.wlug.wlug.activity.MainActivity;
import com.wlug.wlug.app.Config;
import com.wlug.wlug.app.MyApplication;
import com.wlug.wlug.helper.MyPreferenceManager;
import com.wlug.wlug.model.User;
import com.wlug.wlug.model.Message;
import org.json.JSONException;
import org.json.JSONObject;



public class MyGcmPushReceiver extends GcmListenerService {

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();
    DatabaseHelper mydb=new DatabaseHelper(MyGcmPushReceiver.this);
    private NotificationUtils notificationUtils;
    public SharedPreferences shared,sharedPreferences;
    public boolean isSelfMessage=false;
    SharedPreferences.Editor editor;

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */

    @Override
    public void onMessageReceived(String from, Bundle bundle) {

        String title = bundle.getString("title");
        Boolean isBackground = Boolean.valueOf(bundle.getString("is_background"));
        Log.e("msg","msg");
        String flag = bundle.getString("flag");
        String data = bundle.getString("data");
        shared =MyGcmPushReceiver.this.getSharedPreferences("new_data",Context.MODE_PRIVATE);
        sharedPreferences=MyGcmPushReceiver.this.getSharedPreferences("user_data",Context.MODE_PRIVATE);
        editor= shared.edit();

        Log.e(TAG, "From: " + from);
        Log.e(TAG, "title: " + title);
        Log.e(TAG, "isBackground: " + isBackground);
        Log.e(TAG, "flag: " + flag);
        Log.e(TAG, "data: " + data);
    //    MyPreferenceManager myPreferenceManager=new MyPreferenceManager(MainActivity.context);
       // if (flag == null)
     //       return;

    /*   if(myPreferenceManager.getUser() == null){
            // user is not logged in, skipping push notification
            Log.e(TAG, "user is not logged in, skipping push notification");
            return;
        }
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }*/


        switch (Integer.parseInt(flag)) {
            case Config.PUSH_TYPE_CHATROOM:
                // push notification belongs to a chat room
               processChatRoomPush(title, isBackground, data);
                break;
            case Config.PUSH_TYPE_USER:
                // push notification is specific to user
              //  processUserMessage(title, isBackground, data);
                break;
        }
    }

    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     * */
    private void processChatRoomPush(String title, boolean isBackground, String data) {
        String imageUrl=null;
        if (!isBackground) {

            try {

                JSONObject datObj = new JSONObject(data);
                String chatRoomId = datObj.getString("chat_room_id");
                JSONObject mObj = datObj.getJSONObject("message");
                imageUrl=datObj.getString("image");
                Message message = new Message();
                message.setMessage(mObj.getString("message"));
                message.setCreatedAt(mObj.getString("created_at"));
                JSONObject uObj = datObj.getJSONObject("user");

                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
          /*      if (uObj.getString("user_id").equals(MyApplication.getInstance().getPrefManager().getUser().getId())) {
                    Log.e(TAG, "Skipping the push message as it belongs to same user");
                    return;
                }
                */

                User user = new User();
                user.setEmail(uObj.getString("email"));
                user.setName(uObj.getString("name"));
                message.setUser(user);
                if(sharedPreferences.getString("email","N/A").equals(uObj.getString("email")))
                {
                    isSelfMessage=true;
                   // message.setMessage("You:- "+mObj.getString("message"));
                    setData(datObj.getString("chat_room_id"),"YOU :- "+message.getMessage(),mObj.getString("created_at"));

                }
                else {
                  //  message.setMessage( uObj.getString("name") + "- " + mObj.getString("message"));
                    setData(datObj.getString("chat_room_id"), uObj.getString("name") + " :- " + mObj.getString("message"), mObj.getString("created_at"));
                }
                boolean isinserted=mydb.insertDat(datObj.getString("chat_room_id"),uObj.getString("email"),uObj.getString("name"),mObj.getString("message"),mObj.getString("created_at"));
                Log.e(" before inserted","asds");
                if(isinserted==true)
                {  Log.e("inserted","hvhvhv");}
                else
                { Log.e(" not inserted","vgvgvhg");}

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_CHATROOM);
                    pushNotification.putExtra("message", message);
                    pushNotification.putExtra("chat_room_id", chatRoomId);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils();
//                    notificationUtils.playNotificationSound();
                } else {

                    // app is in background. show the message in notification try
                    Intent resultIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                    resultIntent.putExtra("chat_room_id", chatRoomId);
                   // showNotificationMessage(getApplicationContext(), title, user.getName() + " : " + message.getMessage(), message.getCreatedAt(), resultIntent);
                    if (TextUtils.isEmpty(imageUrl)) {
                        Log.e("Image url","not received in push");
                        showNotificationMessage(getApplicationContext(), title, user.getName() + " : " + message.getMessage(), message.getCreatedAt(), resultIntent);
                    } else {
                        // push notification contains image
                        // show it with the image
                        Log.e("Image url","received in push");
                        showNotificationMessageWithBigImage(getApplicationContext(),
                                title, message.getMessage(), message.getCreatedAt(), resultIntent,
                                imageUrl);
                    }

                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
//                Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    /**
     * Processing user specific push message
     * It will be displayed with / without image in push notification tray
     * */
    /*
    private void processUserMessage(String title, boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);

                String imageUrl = datObj.getString("image");

                JSONObject mObj = datObj.getJSONObject("message");
                Message message = new Message();
                message.setMessage(mObj.getString("message"));
                message.setCreatedAt(mObj.getString("created_at"));


                JSONObject uObj = datObj.getJSONObject("user");
                User user = new User();
                user.setEmail(uObj.getString("email"));
                user.setName(uObj.getString("name"));
                message.setUser(user);

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_USER);
                    pushNotification.putExtra("message", message);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils();
//                   notificationUtils.playNotificationSound();
                } else {

                    // app is in background. show the message in notification try
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                    // check for push notification image attachment
                    if (TextUtils.isEmpty(imageUrl)) {
                        Log.e("Image url","not received in push");
                        showNotificationMessage(getApplicationContext(), title, user.getName() + " : " + message.getMessage(), message.getCreatedAt(), resultIntent);
                    } else {
                        // push notification contains image
                        // show it with the image
                        Log.e("Image url","received in push");
                        showNotificationMessageWithBigImage(getApplicationContext(),
                                title, message.getMessage(), message.getCreatedAt(), resultIntent,
                                imageUrl);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());

             //   Toast.makeText(MainActivity.context, "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }*/
/*

 */
    /**
     * Showing notification with text only
     * */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.e("Bigno","not Called");
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);


    }

    /**
     * Showing notification with text and image
     * */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent,
                                                     String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
       // Log.e("Bigno","Called");
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent,imageUrl);
    }
    public void  setData(String title,String message,String time)
    {
        editor.putString(title+"message",message);
        editor.putString(title+"time",time);
        if (!isSelfMessage) {
            int i = shared.getInt(title + "count", 0);

            editor.putInt(title + "count", i + 1);
        }
        editor.commit();
    }
}