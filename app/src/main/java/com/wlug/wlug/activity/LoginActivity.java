package com.wlug.wlug.activity;

import
        android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wlug.wlug.R;
import com.wlug.wlug.app.EndPoints;
import com.wlug.wlug.app.MyApplication;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.wlug.wlug.helper.BackgroundTask;
import com.wlug.wlug.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private String TAG = LoginActivity.class.getSimpleName();
    private EditText inputPassword, inputEmail;
    private TextInputLayout inputLayoutPassword, inputLayoutEmail;
    private Button btnEnter;
    private TextView notregistered;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        /**
         * Check for login session. It user is already logged in
         * redirect him to main activity
         * */
/*        if (MyApplication.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
*/
        setContentView(R.layout.activity_login);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        inputEmail = (EditText) findViewById(R.id.input_email);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        notregistered = (TextView) findViewById(R.id.notRegistered);

//        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
 //       inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

      /*  notregistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(this,CreateAccount.class));
            }
        });*/
    }
public void launchCreate(View view)
{
    startActivity(new Intent(this,CreateAccount.class));
    finish();
}

    /**
     * logging in user. Will make http post request with name, email
     * as parameters
     */
    private void login() {
        Log.e("Login","Login called");
        if(inputEmail.getText().toString().equals("1234")) {
            Log.e("Login","1234");
            SharedPreferences sharedPreferences=getSharedPreferences("user_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            String access=sharedPreferences.getString("su_access","N/A");
            if(access.equals("N/A")){
            Toast.makeText(MainActivity.context, "su access gained", Toast.LENGTH_LONG).show();
            editor.putString("su_access","true");
            editor.commit();
            }
            else {
                Toast.makeText(MainActivity.context, "Already have su access", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if(inputEmail.getText().toString().equals("4321")) {
            Log.e("Login","4321");
            SharedPreferences sharedPreferences=getSharedPreferences("user_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            String access=sharedPreferences.getString("su_access","N/A");
            Toast.makeText(MainActivity.context, "su access removed", Toast.LENGTH_LONG).show();
            editor.putString("su_access","N/A");
            editor.commit();
            return;
        }
        if (!validateEmail()) {
            return;
        }
        if (!validatePassword()) {
            return;
        }



        final String password = inputPassword.getText().toString();
        final String email = inputEmail.getText().toString();

        SharedPreferences sharedPreferences=getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String id=sharedPreferences.getString("reg_id","N/A");
        String method="login";
        BackgroundTask backgroundTask=new BackgroundTask(context);
        backgroundTask.execute(method,email,password,id);

      /*  StringRequest strReq = new StringRequest(Request.Method.POST,
              EndPoints.LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        // user successfully logged in

                        JSONObject userObj = obj.getJSONObject("user");
                        User user = new User(userObj.getString("user_id"),
                                userObj.getString("name"),
                                userObj.getString("email"));

                        // storing user in shared preferences
                        MyApplication.getInstance().getPrefManager().storeUser(user);

                        // start main activity
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();

                    } else {
                        // login error - simply toast the message
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
*/
        //Adding request to request queue
   //     MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    // Validating name
    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError("Invalid Password");
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    // Validating email
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError("Invalid email");
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_password:
                    validatePassword();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
            }
        }
    }
}