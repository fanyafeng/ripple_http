package com.ripple.http.callback;

import com.ripple.http.exception.BaseException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

import kotlin.Unit;

/**
 * Author: fanyafeng
 * Data: 2020/8/10 16:52
 * Email: fanyafeng@live.cn
 * Description:
 */
public class SimpleTypedCallback<T> implements OnHttpResult.Typed, OnHttpResult<T> {

    Type mResponseClass;

    public SimpleTypedCallback(Type type) {
        this.mResponseClass = type;
    }

    @Nullable
    @Override
    public Type getTyped() {
        return mResponseClass;
    }

    @Override
    public void setTyped(@Nullable Type typed) {

    }

    @Override
    public void onItemDoing(Long aLong) {

    }

    @Override
    public void onItemFailed(BaseException e) {

    }

    @Override
    public void onItemFinish(Boolean aBoolean) {

    }

    @Override
    public void onItemSuccess(T t) {

    }

    @Override
    public void onItemStart(@NotNull Unit startResult) {

    }

    @Override
    public void onItemDoing(long doingResult) {

    }
}
