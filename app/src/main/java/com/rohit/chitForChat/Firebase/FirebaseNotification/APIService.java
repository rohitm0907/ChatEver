package com.rohit.chitForChat.Firebase.FirebaseNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAAvSTVpc8:APA91bH1_LiuHhD2Rco7eltJTb38y_9V6j6qOdNd4AaEwUCYAh6DoAKDw8nmELDVXfkQHctqww9wGK8wt2MguiZqxrUdQaFvbZz4K_wh8KUCXlKL4wBv0Gj-hq2ipI7Z9fiaSBFntDbG" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

