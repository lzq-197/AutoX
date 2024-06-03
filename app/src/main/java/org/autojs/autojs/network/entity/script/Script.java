package org.autojs.autojs.network.entity.script;

import com.google.gson.annotations.SerializedName;

/**
 * 脚本配置类
 */
public class Script {

    @SerializedName("id")
    private Integer id;
    @SerializedName("scriptName")
    private String scriptName;

    @SerializedName("scriptBody")
    private String scriptBody;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptBody() {
        return scriptBody;
    }

    public void setScriptBody(String scriptBody) {
        this.scriptBody = scriptBody;
    }
}
