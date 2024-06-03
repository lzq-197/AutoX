package org.autojs.autojs.network.entity.script;

import org.autojs.autojs.network.entity.BaseEntity;

public class EncipherScript extends BaseEntity {

    private Integer chunkIndex;
    private String scriptBody;

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getScriptBody() {
        return scriptBody;
    }

    public void setScriptBody(String scriptBody) {
        this.scriptBody = scriptBody;
    }
}
