package org.autojs.autojs.network;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.autojs.autojs.network.api.ConfigApi;
import org.autojs.autojs.network.api.ScriptApi;
import org.autojs.autojs.network.entity.config.Config;

import io.reactivex.Observable;
import io.reactivex.Observer;
import retrofit2.Retrofit;
import retrofit2.http.GET;

/**
 * Created by Stardust on 2017/10/26.
 */

public class ConfigService {
    private static final ConfigService sInstance = new ConfigService();
    private final Retrofit mRetrofit;
    private ConfigApi mConfigApi;

    ConfigService() {
        mRetrofit = NodeBB.instance.getRetrofit();
        mConfigApi = mRetrofit.create(ConfigApi.class);
    }

    public static ConfigService getInstance() {
        return sInstance;
    }

    public Observable<Config> getConfig() {
        return new Observable<Config>() {
            @Override
            protected void subscribeActual(Observer<? super Config> observer) {
            }
        };
    }

}
