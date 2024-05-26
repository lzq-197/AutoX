package org.autojs.autojs.network;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.stardust.util.Objects;
import com.tencent.bugly.crashreport.CrashReport;

import org.autojs.autojs.network.api.ScriptApi;
import org.autojs.autojs.network.api.UserApi;
import org.autojs.autojs.network.entity.notification.Notification;
import org.autojs.autojs.network.entity.notification.NotificationResponse;
import org.autojs.autojs.network.entity.script.Script;
import org.autojs.autojs.network.entity.user.User;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * 获取脚本信息
 */

public class ScriptService {
    private static final ScriptService sInstance = new ScriptService();
    private final Retrofit mRetrofit;
    private ScriptApi mScriptApi;

    ScriptService() {
        mRetrofit = NodeBB.instance.getRetrofit();
        mScriptApi = mRetrofit.create(ScriptApi.class);
    }

    public static ScriptService getInstance() {
        return sInstance;
    }

    public Observable<Script> getScript(String scriptName) {
        return NodeBB.instance
                .getXCsrfToken()
                .flatMap(token ->
                        mScriptApi.getScript(token, scriptName)
                                .doOnError(error -> {
                                    if (error instanceof HttpException && ((HttpException) error).code() == 403) {
                                        NodeBB.instance.invalidateXCsrfToken();
                                    }
                                }));

    }

    public Observable<Script> list() {
        return NodeBB.instance
                .getXCsrfToken()
                .flatMap(token ->
                        mScriptApi.getScriptList(token)
                                .doOnError(error -> {
                                    if (error instanceof HttpException && ((HttpException) error).code() == 403) {
                                        NodeBB.instance.invalidateXCsrfToken();
                                    }
                                }));

    }

}
