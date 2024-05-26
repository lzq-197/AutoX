package org.autojs.autojs.model.explorer;

import com.stardust.pio.PFile;

import org.autojs.autojs.model.script.ScriptFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RemoteFileItem implements ExplorerItem {

    private static final Set<String> sEditableFileExts = new HashSet<>(Arrays.asList(
            "js", "java", "xml", "json", "txt", "log", "ts"
    ));


    public RemoteFileItem() {
    }


    @Override
    public String getName() {
        return "remote.js";
    }

    @Override
    public ExplorerPage getParent() {
        return null;
    }

    @Override
    public String getPath() {
        return "/remote_script";
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public boolean canDelete() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public String getType() {
        return "js";
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public ScriptFile toScriptFile() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isExecutable() {
        return false;
    }
}
