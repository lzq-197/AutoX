package org.autojs.autojs.network.entity.script;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.autojs.autojs.network.entity.BaseEntity;

import java.util.Date;

/**
 * 脚本对象 user_script
 *
 * @author ruoyi
 * @date 2024-05-26
 */
public class UserScript extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long uid;

    /**
     * $column.columnComment
     */
    private Long scriptId;

    /**
     * $column.columnComment
     */
    private Long id;

    private String secretKey;

    private String dbKey;

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    private Date expirationTime;

    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }


    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getUid() {
        return uid;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public Long getScriptId() {
        return scriptId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("uid", getUid())
                .append("scriptId", getScriptId())
                .append("id", getId())
                .toString();
    }
}
