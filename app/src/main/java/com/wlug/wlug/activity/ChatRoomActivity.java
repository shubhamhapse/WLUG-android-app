package com.wlug.wlug.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.wlug.wlug.R;
import com.wlug.wlug.adapter.ChatRoomThreadAdapter;
import com.wlug.wlug.app.Config;
import com.wlug.wlug.app.EndPoints;
import com.wlug.wlug.app.MyApplication;
import com.wlug.wlug.gcm.NotificationUtils;
import com.wlug.wlug.helper.BackgroundTask;
import com.wlug.wlug.helper.ConnectionCheck;
import com.wlug.wlug.helper.DatabaseHelper;
import com.wlug.wlug.helper.MyPreferenceManager;
import com.wlug.wlug.model.User;
import com.wlug.wlug.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class ChatRoomActivity extends AppCompatActivity {

    private String TAG = ChatRoomActivity.class.getSimpleName();


//    private String chatRoomId;
   private static String title;
    private RecyclerView recyclerView;
    public ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static EditText inputMessage;
    private Button btnSend;
    private RelativeLayout relativeLayout;
    SharedPreferences sharedPreferences;
   public static Context context;
    DatabaseHelper mydb=new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences=this.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        context=this;
        relativeLayout=(RelativeLayout) findViewById(R.id.snackbar);
        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (Button) findViewById(R.id.btn_send);

        Intent intent = getIntent();
    //    chatRoomId = intent.getStringExtra("chat_room_id");
         title = intent.getStringExtra("name");

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    /*    if (chatRoomId == null) {
            Toast.makeText(getApplicationContext(), "Chat room not found!", Toast.LENGTH_SHORT).show();
            finish();
        }*/
        if(title.equals("NOTICES"))
        {
            inputMessage.setVisibility(View.GONE);
            btnSend.setVisibility(View.GONE);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner

        String selfUserId =sharedPreferences.getString("email","N/A");
        mAdapter = new ChatRoomThreadAdapter(this, messageArrayList, selfUserId,title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      //  layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        fetchChatThread();

        if (mAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        Message message = (Message) intent.getSerializableExtra("message");
        String chatRoomName = intent.getStringExtra("chat_room_id");
        Log.e("sadas",chatRoomName);
        Log.e("sadas",title);

        if(chatRoomName.equals(title)) {
            if (message != null && chatRoomName != null) {
                inputMessage.setText("");
                messageArrayList.add(message);
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                }
            }
        }
        else
            Toast.makeText(getApplicationContext(),"Other activity",Toast.LENGTH_LONG).show();
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     */
    private void sendMessage() {
        final String message = this.inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        String email=sharedPreferences.getString("email","N/A");
        ConnectionCheck connectionCheck=new ConnectionCheck(this);
        if(connectionCheck.isNetworkConnected())
        {
          //  Toast.makeText(this,"true",Toast.LENGTH_LONG).show();
            Log.e("connected","connected");

            BackgroundTask backgroundTask=new BackgroundTask(MainActivity.context);
            String method="message";
            backgroundTask.execute(method,email,title,message);
        }else
        {
            Log.e("not connected", "not connected");
            Snackbar snackbar = Snackbar
                    .make(relativeLayout, "No internet connection!", Snackbar.LENGTH_LONG)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });

// Changing message text color
            snackbar.setActionTextColor(Color.RED);

// Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();

        }


    }
    /**
     * Fetching all the messages of a single chat room
     * */
    private void fetchChatThread() {

        Cursor res= mydb.getAllData();
        if(res.getCount()==0)
        {
            Toast.makeText(this,"not data found",Toast.LENGTH_LONG).show();
           // showMessage("fg","");
            return ;

        }
        StringBuffer buffer=new StringBuffer();
        while(res.moveToNext()){
           /* buffer.append("title :"+res.getString(0)+"\n");
            buffer.append("email :"+res.getString(1)+"\n");
            buffer.append("username :"+res.getString(2)+"\n");
            buffer.append("message :"+res.getString(3)+"\n");
            buffer.append("time :"+res.getString(4)+"\n");
           // showMessage("Data",buffer.toString());   */
            String titl,email,username,message,time;
            titl=(res.getString(0));
           if(titl.equals(title))
            {
                User user=new User((res.getString(2)),(res.getString(1)));
                Message message1=new Message((res.getString(3)),(res.getString(4)),user);
                messageArrayList.add(message1);
               // Toast.makeText(this,(res.getString(2))+("email :"+res.getString(1))+("message :"+res.getString(3))+("time :"+res.getString(4)),Toast.LENGTH_SHORT).show();

            }
        }
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_for_chatroom, menu);

        SharedPreferences sharedPreferences=getSharedPreferences("user_data",Context.MODE_PRIVATE);
        String access=sharedPreferences.getString("su_access","N/A");

        if (access.equals("N/A")) {

            MenuItem item = menu.findItem(R.id.add);
            item.setVisible(false);
            invalidateOptionsMenu();
            return true;

        }

        if(title.equals("CLUB"))
        {
            MenuItem item = menu.findItem(R.id.add);
            item.setVisible(false);
            invalidateOptionsMenu();
        }
        if(title.equals("DOWNLOADS"))
        {
            MenuItem item = menu.findItem(R.id.add);
            item.setIcon(R.drawable.ic_folder_upload);
            invalidateOptionsMenu();
        }
        if(title.equals("NOTICES")||title.equals("COMMAND LINE"))
        {
            MenuItem item = menu.findItem(R.id.add);
            item.setIcon(R.drawable.ic_plus);
            invalidateOptionsMenu();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.add: {
               if(title.equals("DOWNLOADS"))
               {startActivity(new Intent(this,UploadFile.class));}
                else if(title.equals("NOTICES"))
                {startActivity(new Intent(this,SendNotice.class));}
                else if(title.equals("COMMAND LINE"))
                {startActivity(new Intent(this,AddCommand.class));}
                return true;
            }
            case R.id.action_deletechat: {
                DatabaseHelper d=new DatabaseHelper(getApplicationContext());
                d.deleteData(title);
                Toast.makeText(this,"data deleted",Toast.LENGTH_SHORT).show();
                messageArrayList.clear();
                mAdapter.notifyDataSetChanged();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


}