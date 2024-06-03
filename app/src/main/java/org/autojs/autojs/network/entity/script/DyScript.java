package org.autojs.autojs.network.entity.script;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.autojs.autojs.network.entity.BaseEntity;

import java.util.List;

/**
 * 脚本对象 dy_script
 *
 * @author ruoyi
 * @date 2024-05-24
 */
public class DyScript extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 序列号
     */
    private Long serialNumber;

    /**
     * 脚本方法
     */
    private String scriptFunction;
    /**
     * 脚本分组
     */
    private Long groupType;

    private List<EncipherScript> encipherScripts;

    private List<String> keys;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public List<EncipherScript> getEncipherScripts() {
        return encipherScripts;
    }

    public void setEncipherScripts(List<EncipherScript> encipherScripts) {
        this.encipherScripts = encipherScripts;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setSerialNumber(Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public void setScriptFunction(String scriptFunction) {
        this.scriptFunction = scriptFunction;
    }

    public String getScriptFunction() {
        return scriptFunction;
    }

    public void setGroupType(Long groupType) {
        this.groupType = groupType;
    }

    public Long getGroupType() {
        return groupType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("scriptName", getScriptName())
                .append("serialNumber", getSerialNumber())
                .append("scriptFunction", getScriptFunction())
                .append("groupType", getGroupType())
                .toString();
    }
}
