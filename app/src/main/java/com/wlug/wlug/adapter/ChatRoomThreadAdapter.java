package com.wlug.wlug.adapter;

/**
 * Created by Inspiron on 18-06-2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wlug.wlug.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.wlug.wlug.activity.ChatRoomActivity;
import com.wlug.wlug.activity.MainActivity;
import com.wlug.wlug.model.Message;

public class ChatRoomThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static String TAG = ChatRoomThreadAdapter.class.getSimpleName();
    private String userId;
    private int SELF = 100;
    private static String today;
    private String title;

    private Context mContext;
    private ArrayList<Message> messageArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) itemView.findViewById(R.id.message);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        }
    }


    public ChatRoomThreadAdapter(Context mContext, ArrayList<Message> messageArrayList, String userId,String title) {
        this.mContext = mContext;
        this.messageArrayList = messageArrayList;
        this.userId = userId;
        this.title=title;

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // view type is to identify where to render the chat message
        // left or right or notices
        if(title.equals("NOTICES"))
        {

            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_room_notice, parent, false);
            Log.e("Notice ","Notice");
        }
       else {
            if (viewType == SELF) {
                // self message
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_item_self, parent, false);
            } else {
                // others message
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_item_other, parent, false);
            }
            Log.e(" not Notice "," not Notice");
        }
        return new ViewHolder(itemView);
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        SharedPreferences sharedPreferences= (MainActivity.context).getSharedPreferences("user_data",Context.MODE_PRIVATE);
        if (message.getUser().getEmail().equals(sharedPreferences.getString("email","N/A")))
        {
            return SELF;
        }

        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        ((ViewHolder) holder).message.setText(message.getMessage());

        String timestamp = getTimeStamp(message.getCreatedAt());

        if (message.getUser().getName() != null)
            timestamp = message.getUser().getName() + ", " + timestamp;

        ((ViewHolder) holder).timestamp.setText(timestamp);
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }
}