package org.autojs.autojs.network.entity.script;

import com.google.gson.annotations.SerializedName;

/**
 * 脚本配置类
 */
public class Script {

    @SerializedName("configName")
    private String scriptName;

    @SerializedName("scriptBody")
    private String scriptBody;


}
