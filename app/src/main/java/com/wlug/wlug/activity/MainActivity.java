package com.wlug.wlug.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.wlug.wlug.R;
import com.wlug.wlug.helper.BackgroundTask;
import com.wlug.wlug.helper.MyPreferenceManager;

import com.wlug.wlug.model.Message;
import com.wlug.wlug.adapter.ChatRoomsAdapter;
import com.wlug.wlug.app.Config;
import com.wlug.wlug.app.EndPoints;
import com.wlug.wlug.app.MyApplication;
import com.wlug.wlug.gcm.GcmIntentService;
import com.wlug.wlug.gcm.NotificationUtils;
import com.wlug.wlug.helper.SimpleDividerItemDecoration;
import com.wlug.wlug.model.ChatRoom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<ChatRoom> chatRoomArrayList;
    public static ChatRoomsAdapter mAdapter;
    private RecyclerView recyclerView;
    public SharedPreferences shared,sharedPreferences ;
    public static SharedPreferences.Editor editor;
    public String[] a= new String[]{"NOTICES","CLUB","COMMAND LINE","DOWNLOADS"};
    public static Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        shared =this.getSharedPreferences("new_data",Context.MODE_PRIVATE);
        editor= shared.edit();
      sharedPreferences=getSharedPreferences("user_data",Context.MODE_PRIVATE);
       if (sharedPreferences.getString("email","N/A").equals("N/A")) {
            Log.e("not login","not login");
          startActivity(new Intent(this,LoginActivity.class));
           finish();
        }


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /**
         * Broadcast receiver calls in two scenarios
         * 1. gcm registration is completed
         * 2. when new push notification is received
         * */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
             //       subscribeToGlobalTopic();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL
                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    handlePushNotification(intent);
                    Log.e("received in main","wdasdasdasds");
                }
            }
        };

        chatRoomArrayList = new ArrayList<>();

        mAdapter = new ChatRoomsAdapter(this, chatRoomArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
       recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                ChatRoom chatRoom = chatRoomArrayList.get(position);
                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
             //   intent.putExtra("chat_room_id", chatRoom.getId());
                intent.putExtra("name", chatRoom.getName());
                ClearCount(chatRoom.getName());
                for (ChatRoom cr : chatRoomArrayList) {
                    if (cr.getName().equals(chatRoom.getName())) {
                        int index = chatRoomArrayList.indexOf(cr);

                        cr.setUnreadCount(0);
                       // chatRoomArrayList.remove(index);
                       // chatRoomArrayList.add(0,cr);
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
                startActivity(intent);
             //   finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        /**
         * Always check for google play services availability before
         * proceeding further with GCM
         * */
        if (checkPlayServices()) {
            registerGCM();
            //  fetchChatRooms();


            fetchChatRooms();
        }
    }


    /**
     * Handles new push notification
     */
    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        // if the push is of chat room message
        // simply update the UI unread messages count
        if (type == Config.PUSH_TYPE_CHATROOM) {
            Message message = (Message) intent.getSerializableExtra("message");
            String chatRoomId = intent.getStringExtra("chat_room_id");

            if (message != null && chatRoomId != null) {
                updateRow(chatRoomId, message);
            }
        } else if (type == Config.PUSH_TYPE_USER) {
            // push belongs to user alone
            // just showing the message in a toast
            Message message = (Message) intent.getSerializableExtra("message");
            Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Updates the chat list unread count and the last message
     */
    private void updateRow(String chatRoomId, Message message) {
        for (ChatRoom cr : chatRoomArrayList) {
            if (cr.getName().equals(chatRoomId)) {
                int index = chatRoomArrayList.indexOf(cr);
                String messa=message.getMessage();
                if(message.getUser().getEmail().equals(sharedPreferences.getString("email","N/A")))
                {
                    messa="YOU :-"+messa;
                }
                else
                    messa=message.getUser().getName()+" :- "+messa;
                cr.setLastMessage(messa);
                cr.setUnreadCount(cr.getUnreadCount() + 1);
                chatRoomArrayList.remove(index);
                chatRoomArrayList.add(0,cr);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }


    /**
     * fetching the chat rooms by making http call
     */
    private void fetchChatRooms()
    {

        for(int i=0;i<4;i++) {
            ChatRoom cr=new ChatRoom();
            cr.setName(a[i]);
            cr.setLastMessage(getLastMessage(a[i]));
            cr.setUnreadCount(getCount(a[i]));
            cr.setTimestamp(getLastTime(a[i]));
            chatRoomArrayList.add(cr);
            mAdapter.notifyDataSetChanged();
        }

    }





    // subscribing to global topic
  /*  private void subscribeToGlobalTopic()
    {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
    }*/

    // Subscribing to all chat room topics
    // each topic name starts with `topic_` followed by the ID of the chat room
    // Ex: topic_1, topic_2
  /*  private void subscribeToAllTopics() {
        for (ChatRoom cr : chatRoomArrayList) {

            Intent intent = new Intent(this, GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
         //   intent.putExtra(GcmIntentService.TOPIC, "topic_" + cr.getId());
            startService(intent);
        }
    }
*/
  /*  private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clearing the notification tray
//        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SharedPreferences sharedPreferences=getSharedPreferences("user_data",Context.MODE_PRIVATE);
        String email=sharedPreferences.getString("email","N/A");
        String password=sharedPreferences.getString("password","N/A");
        if(email.isEmpty()&&password.isEmpty())
        {
            MenuItem item = menu.findItem(R.id.action_signout);
            item.setVisible(false);
            invalidateOptionsMenu();
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.action_signout:
            {
                SharedPreferences sharedPreferences=getSharedPreferences("user_data",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                String email=sharedPreferences.getString("email","N/A");
                String password=sharedPreferences.getString("password","N/A");
                editor.putString("su_access","N/A");
                editor.commit();
                String method="signout";
                BackgroundTask backgroundTask=new BackgroundTask(this);
                backgroundTask.execute(method,email,password);
                break;
            }
            case R.id.action_website:
            {
                startActivity(new Intent(this,Website.class));
                break;
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }
    public String getLastMessage(String title)
    {
        return shared.getString(title+"message","No new messages");
    }
    public String getLastTime(String title)
    {
        return shared.getString(title+"time","");
    }
    public int getCount(String title)
    {
        return shared.getInt(title+"count",0);
    }
    public void ClearCount(String title)
    {
        editor.putInt(title+"count",0);
        editor.commit();
    }

}