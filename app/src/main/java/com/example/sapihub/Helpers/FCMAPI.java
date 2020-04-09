package com.example.sapihub.Helpers;

import com.example.sapihub.Model.Notifications.NotificationResponse;
import com.example.sapihub.Model.Notifications.NotificationSender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FCMAPI {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAtfS33l0:APA91bF5LYA8TUfoAuN061a0bRLCp2kAWbB9UKFYuyc6kJe9idl3Iq-oJEF6hZFGkkUJ81BCry_F-AHE2d-gG6TFBjHPmuD-4wi0PGhTqSHWVx4CnRNDvfcNhYsH2Ktba8WsLCualkWS"
            }
    )

    @POST("fcm/send")
    Call<NotificationResponse> sendNotification(@Body NotificationSender body);
}
