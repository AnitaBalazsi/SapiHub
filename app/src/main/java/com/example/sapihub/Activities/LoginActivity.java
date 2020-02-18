package com.example.sapihub.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.MoodleAPI;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Token;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;

import java.time.Year;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText nameInput, passwordInput, yearInput;
    private String token;
    private AlertDialog profileDataDialog;
    private Spinner degreeInput, departmentInput;
    private RadioGroup radioGroup;
    private CheckBox rememberMe;
    private View dialogView;
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
        loadingDialog.setCanceledOnTouchOutside(false);
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
        editor.putString("token",token);
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
                    token = response.body().getToken();
                    loadingDialog.dismiss();
                    saveUserData();

                    //check if user is already stored in database
                    DatabaseHelper.isUserStored(response.body().getToken(), new FirebaseCallback() {
                        @Override
                        public void onCallback(Object object) {
                            if ((Boolean) object){
                                showAddDataDialog();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
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


    private void showAddDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogView = this.getLayoutInflater().inflate(R.layout.login_dialog_layout,null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        yearInput = dialogView.findViewById(R.id.yearInput);
        departmentInput = dialogView.findViewById(R.id.departmentInput);
        degreeInput = dialogView.findViewById(R.id.degreeInput);

        radioGroup = dialogView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == 2){
                    //if student option is checked
                    yearInput.setVisibility(View.VISIBLE);
                    degreeInput.setVisibility(View.VISIBLE);
                } else {
                    yearInput.setVisibility(View.INVISIBLE);
                    degreeInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        Button sendButton = dialogView.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()){
                    showConfirmSendDialog();
                }
            }
        });

        profileDataDialog = builder.create();
        profileDataDialog.show();
    }

    private void showConfirmSendDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.addProfileData))
                .setMessage(getString(R.string.confirmProfileData))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                       storeUserData();
                       profileDataDialog.dismiss();

                       Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                       startActivity(intent);
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void storeUserData() {
        User user = new User(nameInput.getText().toString().trim(),token);
        switch (radioGroup.getCheckedRadioButtonId()){
            case 1:
                user.setOccupation(getString(R.string.teacher));
                break;
            case 2:
                user.setOccupation(getString(R.string.student));
                break;
        }
        if (yearInput.getVisibility() == View.VISIBLE){
            user.setStudyYear(yearInput.getText().toString().trim());
        }
        if (degreeInput.getVisibility() == View.VISIBLE){
            user.setDegree(degreeInput.getSelectedItem().toString());
        }
        user.setDepartment(departmentInput.getSelectedItem().toString());

        DatabaseHelper.addUser(user);
    }

    private boolean validateInputs() {
        if (radioGroup.getCheckedRadioButtonId() == -1){ // no option is checked
            Utils.showSnackbar(dialogView,getString(R.string.inputError),getColor(R.color.colorRed));
            return false;
        }

        String year = yearInput.getText().toString().trim();
        if (yearInput.getVisibility() == View.VISIBLE && (yearInput.length() < 4 || year.compareTo(Year.now().toString()) > 0)){
            //if input is not a four digit number or the year is not valid
            yearInput.setError(getString(R.string.wrongYearInput));
            yearInput.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        logIn();
    }
}
