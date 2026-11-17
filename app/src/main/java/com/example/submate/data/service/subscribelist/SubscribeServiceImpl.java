package com.example.submate.data.service.subscribelist;

import com.example.submate.data.dto.subscribelist.SubscribeMapper;
import com.example.submate.data.dto.subscribelist.SubscribeResponse;
import com.example.submate.data.network.SubscribeApi;
import com.example.submate.domain.model.SubscribeItem;
import com.example.submate.domain.repository.ResultCallback;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Retrofit으로 실제 서버와 통신하는 구현체입니다. */
public class SubscribeServiceImpl implements SubscribeService {
    private static final String NETWORK_ERROR = "네트워크 연결을 확인해주세요.";

    private final SubscribeApi api;

    @Inject
    public SubscribeServiceImpl(SubscribeApi api) {
        this.api = api;
    }

    @Override
    public void getSubscribeList(ResultCallback<List<SubscribeItem>> callback) {
        api.getSubscribeList().enqueue(new Callback<List<SubscribeResponse>>() {
            @Override
            public void onResponse(Call<List<SubscribeResponse>> call,
                                   Response<List<SubscribeResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(serverError(response.code()));
                    return;
                }
                List<SubscribeItem> items = new ArrayList<>();
                for (SubscribeResponse body : response.body()) {
                    items.add(SubscribeMapper.toItem(body));
                }
                callback.onSuccess(items);
            }

            @Override
            public void onFailure(Call<List<SubscribeResponse>> call, Throwable t) {
                callback.onError(NETWORK_ERROR);
            }
        });
    }

    @Override
    public void addSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        api.addSubscribe(SubscribeMapper.toRequest(item)).enqueue(itemCallback(callback));
    }

    @Override
    public void updateSubscribe(SubscribeItem item, ResultCallback<SubscribeItem> callback) {
        api.updateSubscribe(item.getId(), SubscribeMapper.toRequest(item))
                .enqueue(itemCallback(callback));
    }

    @Override
    public void deleteSubscribe(long id, ResultCallback<Void> callback) {
        api.deleteSubscribe(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(serverError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(NETWORK_ERROR);
            }
        });
    }

    private Callback<SubscribeResponse> itemCallback(ResultCallback<SubscribeItem> callback) {
        return new Callback<SubscribeResponse>() {
            @Override
            public void onResponse(Call<SubscribeResponse> call, Response<SubscribeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(SubscribeMapper.toItem(response.body()));
                } else {
                    callback.onError(serverError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<SubscribeResponse> call, Throwable t) {
                callback.onError(NETWORK_ERROR);
            }
        };
    }

    private String serverError(int code) {
        return "요청에 실패했습니다. (" + code + ")";
    }
}
