package com.qtt.barberstaffapp.Retrofit;

import com.qtt.barberstaffapp.Model.FCMResponse;
import com.qtt.barberstaffapp.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAVn_44l8:APA91bG2OqUbz6UR_k9alEpuGJXNN0A2_-1gyDAJhJ8-vIWoprqiXMZ_MAGMUqxLpiHUDneJXI89Mbiqf3uO9qN7Z80ylOpt5EuDr6mWcegkMD1Nq_PxTxSM3xCjnvQD8CVAtz9B9pPv"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
