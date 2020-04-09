package com.example.sapihub.Helpers;

import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Model.Notifications.FCMToken;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        DatabaseHelper.saveFCMToken(new FCMToken(token), Utils.getCurrentUserToken(getApplicationContext()));
    }
}
