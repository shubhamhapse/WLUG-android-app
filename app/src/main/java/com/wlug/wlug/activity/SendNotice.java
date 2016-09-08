package com.wlug.wlug.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wlug.wlug.R;
import com.wlug.wlug.app.Config;
import com.wlug.wlug.helper.BackgroundTask;
import com.wlug.wlug.helper.FilePath;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendNotice extends AppCompatActivity {
    static final int PICK_FILE_REQUEST=1;
    static Boolean isTouched = false;
    LinearLayout linearLayout;
    TextInputLayout inputLayoutNotice;
    EditText inputNotice;
    SwitchCompat switchButton;
    Button button,buttonSend;
    String TAG="sendNotice";
    ProgressDialog dialog;
    public static Context context;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String selectedFilePath="";
    private String SERVER_URL = "http://shubhamwlug.esy.es/wlug/UploadFile.php";
    SharedPreferences sharedPreferences=MainActivity.context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
    TextView textViewPath,textViewSize,textViewName;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        context=this;
        inputLayoutNotice = (TextInputLayout) findViewById(R.id.input_layout_notice);
        inputNotice= (EditText) findViewById(R.id.input_notice);
        textViewPath =(TextView)findViewById(R.id.imagePath);
        textViewSize =(TextView)findViewById(R.id.imageSize);
        textViewName =(TextView)findViewById(R.id.imageName);

        linearLayout= (LinearLayout) findViewById(R.id.imageDetail1);
        linearLayout.setVisibility(View.GONE);

        linearLayout= (LinearLayout) findViewById(R.id.imageDetail2);
        linearLayout.setVisibility(View.GONE);

        linearLayout= (LinearLayout) findViewById(R.id.imageDetail3);
        linearLayout.setVisibility(View.GONE);

        button=(Button) findViewById(R.id.btn_choose_image);
        button.setVisibility(View.GONE);

        buttonSend= (Button) findViewById(R.id.btn_enter);
        switchButton= (SwitchCompat) findViewById(R.id.addImage);
        switchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
           //     textViewSize.setText("");
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on upload button Click
                if(!switchButton.isChecked())
                {
                    String email=sharedPreferences.getString("email","N/A");


                    BackgroundTask backgroundTask=new BackgroundTask(MainActivity.context);
                    String method="message";
                    backgroundTask.execute(method,email,"NOTICES",inputNotice.getText().toString());

                }
                else{
                    if(selectedFilePath != null){
                        dialog = ProgressDialog.show(MainActivity.context,"","Uploading File...",true);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //creating new thread to handle Http Operations
                                uploadFile(selectedFilePath);
                            }
                        }).start();
                    }else{
                        Toast.makeText(MainActivity.context,"Please choose a File First",Toast.LENGTH_SHORT).show();
                    }
                }

            }

        });
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isTouched) {
                    isTouched = false;
                    if (isChecked) {
                        linearLayout= (LinearLayout) findViewById(R.id.imageDetail1);
                        linearLayout.setVisibility(View.VISIBLE);

                        linearLayout= (LinearLayout) findViewById(R.id.imageDetail2);
                        linearLayout.setVisibility(View.VISIBLE);

                        linearLayout= (LinearLayout) findViewById(R.id.imageDetail3);
                        linearLayout.setVisibility(View.VISIBLE);

                        button=(Button) findViewById(R.id.btn_choose_image);
                        button.setVisibility(View.VISIBLE);

                        buttonSend.setEnabled(false);
                      //  Toast.makeText(MainActivity.context,"enabled",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        linearLayout= (LinearLayout) findViewById(R.id.imageDetail1);
                        linearLayout.setVisibility(View.GONE);

                        linearLayout= (LinearLayout) findViewById(R.id.imageDetail2);
                        linearLayout.setVisibility(View.GONE);

                        linearLayout= (LinearLayout) findViewById(R.id.imageDetail3);
                        linearLayout.setVisibility(View.GONE);

                        button=(Button) findViewById(R.id.btn_choose_image);
                        button.setVisibility(View.GONE);
                    //    Toast.makeText(MainActivity.context,"disabled",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        //    inflater.inflate(R.menu.menu_main_for_chatroom, menu);

        SharedPreferences sharedPreferences=getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String email=sharedPreferences.getString("email","N/A");
        String password=sharedPreferences.getString("password","N/A");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }
  public void handlePushNotification(Context context)
  {

  }
/*
    private void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*0/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);

    }*/
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //sets the select file to all types of files
        intent.setType("image/*");
        //allows to select data and return it
        //intent.setAction(Intent.ACTION_PICK);
        //starts new activity to select file and return data
        startActivityForResult(intent,PICK_FILE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
              if(requestCode == PICK_FILE_REQUEST){
           // if(requestCode == 2012){
                if(data == null){
                    //no data present
                    return;
                }


                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this,selectedFileUri);
                Log.e(TAG,"Selected File Path:" + selectedFilePath);
                if(selectedFilePath != null && !selectedFilePath.equals("")){
                    buttonSend.setEnabled(true);
                    textViewPath.setText(selectedFilePath);
                    File file=new File(selectedFilePath);
                    int actualsize = 0;
                    if(file.exists())
                    { long size=file.length();
                        if (size>1024)
                        {
                         actualsize=(int)size/1024;
                        }
                    textViewSize.setText((Integer.toString(actualsize)+"Kb"));
                        textViewName.setText(file.getName());
                    Log.e("asdf","file exists");
                    }
                    else
                        Log.e("asdf","file not exists");
                }else{
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //android upload file to server
    public int uploadFile(final String selectedFilePath){
        if (inputNotice.getText().toString().trim().isEmpty()) {
            inputLayoutNotice.setError("Invalid Notice");
        return 0;
        }
        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        final File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){
            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewPath.setText("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.context,"File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName,Toast.LENGTH_LONG).show();
                            BackgroundTask backgroundTask=new BackgroundTask(MainActivity.context);
                            SharedPreferences sharedPreferences=getSharedPreferences("user_data",Context.MODE_PRIVATE);
                            String email=sharedPreferences.getString("email","N/A");
                            String method="notice";
                            backgroundTask.execute(method,email,"NOTICES",inputNotice.getText().toString(),selectedFile.getName());
                            Log.e("sendNotice",method+email+"NOTICES"+inputNotice.getText().toString()+selectedFile.getName());
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.context,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.context, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.context, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            return serverResponseCode;
        }
    }
}
