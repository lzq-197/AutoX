package org.autojs.autojs.network.api;

import org.autojs.autojs.network.entity.config.Config;
import org.autojs.autojs.network.entity.script.Script;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Stardust on 2017/10/26.
 */

public interface ScriptApi {

    @POST("/api/script")
    Observable<Script> getScript(Map<String, String> token, String scriptName);

    @POST("/api/script/list")
    Observable<Script> getScriptList(Map<String, String> token);
}
