package com.example.sapihub.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sapihub.Helpers.DatabaseHelper;
import com.example.sapihub.Helpers.MoodleAPI;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Token;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText nameInput, passwordInput;
    private CheckBox rememberMe;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeVariables();
        if (getCheckBoxData()){
            setLogInInputs();
        }
    }

    private void initializeVariables() {
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(this);
        nameInput = findViewById(R.id.name);
        passwordInput = findViewById(R.id.password);
        rememberMe = findViewById(R.id.rememberMe);

        loadingDialog = new ProgressDialog(this, R.style.ProgressDialog);
        loadingDialog.setMessage(getString(R.string.loading));

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    //checks if remember me is enabled or not
    public boolean getCheckBoxData(){
        return sharedPreferences.getBoolean("rememberMe", false);
    }

    public void setLogInInputs(){
        nameInput.setText(sharedPreferences.getString("username", null));
        passwordInput.setText(sharedPreferences.getString("password", null));
        rememberMe.setChecked(true);
    }


    public void saveUserData(){
        editor.putString("username",nameInput.getText().toString());
        editor.putString("password",passwordInput.getText().toString());
        editor.putBoolean("rememberMe",rememberMe.isChecked());
        editor.apply();
    }


    public void logIn(){
        loadingDialog.show();
        final String username = nameInput.getText().toString();
        final String password = passwordInput.getText().toString();
        String service = "moodle_mobile_app";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MoodleAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MoodleAPI api = retrofit.create(MoodleAPI.class);
        api.loginUser(username,password,service).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.body().getToken() != null){
                    loadingDialog.dismiss();

                    //add user to database
                    DatabaseHelper.addUser(new User(username,password,response.body().getToken()));

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    loadingDialog.dismiss();
                    Utils.showSnackbar(
                            findViewById(android.R.id.content),
                            getString(R.string.loginError),
                            ContextCompat.getColor(getBaseContext(), R.color.colorRed));
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                //TODO
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (rememberMe.isChecked()){
            saveUserData();
        }
        logIn();
    }
}
