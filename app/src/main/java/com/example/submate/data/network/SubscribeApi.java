package com.example.submate.data.network;

import com.example.submate.data.dto.subscribelist.SubscribeRequest;
import com.example.submate.data.dto.subscribelist.SubscribeResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** 구독 목록 서버 API */
public interface SubscribeApi {
    @GET("subscriptions")
    Call<List<SubscribeResponse>> getSubscribeList();

    @POST("subscriptions")
    Call<SubscribeResponse> addSubscribe(@Body SubscribeRequest request);

    @PUT("subscriptions/{id}")
    Call<SubscribeResponse> updateSubscribe(@Path("id") long id, @Body SubscribeRequest request);

    @DELETE("subscriptions/{id}")
    Call<Void> deleteSubscribe(@Path("id") long id);
}
