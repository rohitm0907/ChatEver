package com.android.chatever.Firebase.FirebaseNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAApVa4B0o:APA91bEjj4CCzWDnHWcqMddC-QOlIc8WvdG8AHClqzgCIN9mt6ibg8K2YlXKW4H0QcWR2Tv35pnTiEEHuURYABmjf9cjT2RTXwKxaAh70HzsuQ-d5E2HVyy7MH0mY88keFXuBlLbWuik"            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

