package com.wlug.wlug.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wlug.wlug.activity.ChatRoomActivity;
import com.wlug.wlug.activity.MainActivity;

/**
 * Created by Dnyaneshwar on 6/21/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper{
    public  static final String DATABASE_NAME="wlug";
    public  static final String TABLE_NAME="wlug_chat";
    public static final String TABLE_NAME_LATEST="wlug_latest";
    public String COL_COUNT="count";
    public  static final String COL_1="title";
    public  static final String COL_2="email";
    public  static final String COL_3="username";
    public  static final String COL_4="message";
    public  static final String COL_5="time";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db=this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_NAME+" (title TEXT,email TEXT,username TEXT,message TEXT,time TEXT);");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE iF EXISTS "+TABLE_NAME);

        onCreate(db);
    }
    public boolean insertDat(String title,String email,String username,String message,String time)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(COL_1,title);
        contentValues.put(COL_2,email);

        contentValues.put(COL_3,username);
        contentValues.put(COL_4,message);
        contentValues.put(COL_5,time);
        long result= db.insert(TABLE_NAME,null,contentValues);
        if(result==-1)
            return false;
        else
            return true;
    }
    public void deleteData(String name)
    {
        SQLiteDatabase db=this.getWritableDatabase();

         db.delete(TABLE_NAME, COL_1 + "='"+name+"'",null);
       // ChatRoomActivity.
    }
    public Cursor getAllData()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor res=db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }



}
