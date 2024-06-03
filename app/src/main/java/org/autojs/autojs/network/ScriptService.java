package org.autojs.autojs.network;

import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.stardust.autojs.execution.RemoteScriptExecution;
import com.stardust.autojs.script.ScriptSource;

import org.autojs.autojs.network.api.ScriptApi;
import org.autojs.autojs.network.entity.TableDataInfo;
import org.autojs.autojs.network.entity.result.AjaxResult;
import org.autojs.autojs.network.entity.script.DyScript;
import org.autojs.autojs.network.entity.script.UserScript;
import org.autojs.autojs.ui.main.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
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

    public Observable<AjaxResult> getScript(String scriptName) {
        return NodeBB.instance
                .getXCsrfToken()
                .flatMap(token ->
                        mScriptApi.getScript(scriptName)
                                .doOnError(error -> {
                                    if (error instanceof HttpException && ((HttpException) error).code() == 403) {
                                        NodeBB.instance.invalidateXCsrfToken();
                                    }
                                }));

    }

    public Observable<TableDataInfo> list() {
        return NodeBB.instance
                .getXCsrfToken()
                .flatMap(token ->
                        mScriptApi.getScriptList()
                                .doOnError(error -> {
                                    Log.i("ScriptService:", "error: " + error);
                                    if (error instanceof HttpException && ((HttpException) error).code() == 403) {
                                        NodeBB.instance.invalidateXCsrfToken();
                                    }
                                }));
    }

    public Observable<DyScript> downloadFile(Long id) {
        return NodeBB.instance
                .getXCsrfToken()
                .flatMap(token -> mScriptApi.downloadFile(id));

    }

    public Observable<UserScript> getKey(Long scriptId) {
        return NodeBB.instance
                .getXCsrfToken()
                .flatMap(token -> mScriptApi.getKey(scriptId));

    }

    public static Map<Long, DyScript> localScriptList = new ConcurrentHashMap<>();

    public static Map<Long, UserScript> userScriptMap = new ConcurrentHashMap<>();

    public static Map<Long, RemoteScriptExecution> remoteScriptExecutionMap = new ConcurrentHashMap<>();

    public static Map<Long, ScriptSource> stringScriptMap = new ConcurrentHashMap<>();


}
