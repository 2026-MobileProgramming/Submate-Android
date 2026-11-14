package com.example.submate.domain.repository;

/** 비동기 작업(서버 통신 등)의 결과를 전달받는 콜백입니다. */
public interface ResultCallback<T> {
    void onSuccess(T result);

    void onError(String message);
}
