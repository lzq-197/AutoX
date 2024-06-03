package org.autojs.autojs.network.api;

import org.autojs.autojs.network.entity.TableDataInfo;
import org.autojs.autojs.network.entity.result.AjaxResult;
import org.autojs.autojs.network.entity.script.DyScript;
import org.autojs.autojs.network.entity.script.UserScript;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Stardust on 2017/10/26.
 */

public interface ScriptApi {

    @POST("/api/script")
    Observable<AjaxResult> getScript(String scriptName);

    @GET("/api/script/list")
    Observable<TableDataInfo> getScriptList();

    @GET("/api/script/download/{id}")
    Observable<DyScript> downloadFile(@Path("id") Long id);

    @GET("/api/script/getKey/{id}")
    Observable<UserScript> getKey(@Path("id") Long scriptId);
}
