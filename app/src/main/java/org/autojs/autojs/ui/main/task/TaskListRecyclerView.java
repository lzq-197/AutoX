package org.autojs.autojs.ui.main.task;

import static com.stardust.autojs.runtime.ScriptRuntime.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.google.gson.Gson;
import com.stardust.autojs.ScriptEngineService;
import com.stardust.autojs.core.looper.Loopers;
import com.stardust.autojs.engine.ScriptEngineManager;
import com.stardust.autojs.execution.ExecutionConfig;
import com.stardust.autojs.execution.RemoteScriptExecution;
import com.stardust.autojs.execution.ScriptExecution;
import com.stardust.autojs.execution.ScriptExecutionListener;
import com.stardust.autojs.execution.ScriptExecutionTask;
import com.stardust.autojs.execution.SimpleScriptExecutionListener;
import com.stardust.autojs.script.AutoFileSource;
import com.stardust.autojs.script.JavaScriptSource;
import com.stardust.autojs.script.ScriptSource;
import com.stardust.autojs.script.StringScriptSource;
import com.stardust.autojs.workground.WrapContentLinearLayoutManager;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.apache.commons.lang3.StringUtils;
import org.autojs.autojs.autojs.ScriptExecutionGlobalListener;
import org.autojs.autojs.network.ScriptService;
import org.autojs.autojs.network.entity.TableDataInfo;
import org.autojs.autojs.network.entity.result.AjaxResult;
import org.autojs.autojs.network.entity.script.DyScript;
import org.autojs.autojs.network.entity.script.EncipherScript;
import org.autojs.autojs.network.entity.script.Script;
import org.autojs.autojs.network.entity.script.UserScript;
import org.autojs.autojs.tool.DatabaseHelper;
import org.autojs.autoxjs.R;
import org.autojs.autojs.autojs.AutoJs;
import org.autojs.autojs.storage.database.ModelChange;
import org.autojs.autojs.timing.TimedTaskManager;
import org.autojs.autojs.ui.timing.TimedTaskSettingActivity;
import org.autojs.autojs.ui.timing.TimedTaskSettingActivity_;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.ThemeColorRecyclerView;

import net.sqlcipher.database.SQLiteDatabase;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stardust on 2017/3/24.
 */

public class TaskListRecyclerView extends ThemeColorRecyclerView {

    private static final String baseDir = "\\root\\sdcard\\scripts\\";

    private static final String LOG_TAG = "TaskListRecyclerView";
    public static final String JS = ".js";

    private final List<TaskGroup> mTaskGroups = new ArrayList<>();
    private TaskGroup.RunningTaskGroup mRunningTaskGroup;
    private TaskGroup.PendingTaskGroup mPendingTaskGroup;
    private TaskGroup.RemoteTaskGroup mRemoteTaskGroup;
    private Adapter mAdapter;
    private Disposable mTimedTaskChangeDisposable;
    private Disposable mIntentTaskChangeDisposable;

    private DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

    private SQLiteDatabase db;

    private ScriptExecutionListener mScriptExecutionListener = new SimpleScriptExecutionListener() {
        @Override
        public void onStart(final ScriptExecution execution) {
            post(() -> mAdapter.notifyChildInserted(0, mRunningTaskGroup.addTask(execution)));
        }

        @Override
        public void onSuccess(ScriptExecution execution, Object result) {
            onFinish(execution);
        }

        @Override
        public void onException(ScriptExecution execution, Throwable e) {
            onFinish(execution);
        }

        private void onFinish(ScriptExecution execution) {
            post(() -> {
                final int i = mRunningTaskGroup.removeTask(execution);
                if (i >= 0) {
                    mAdapter.notifyChildRemoved(0, i);
                } else {
                    refresh();
                }
            });
        }
    };

    public TaskListRecyclerView(Context context) {
        super(context);
        init();
    }

    public TaskListRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TaskListRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext())
                .color(ContextCompat.getColor(getContext(), R.color.divider))
                .size(2)
                .marginResId(R.dimen.script_and_folder_list_divider_left_margin, R.dimen.script_and_folder_list_divider_right_margin)
                .showLastDivider()
                .build());
        mRunningTaskGroup = new TaskGroup.RunningTaskGroup(getContext());
        mTaskGroups.add(mRunningTaskGroup);
        mPendingTaskGroup = new TaskGroup.PendingTaskGroup(getContext());
        mTaskGroups.add(mPendingTaskGroup);
        // 添加远程脚本
        mRemoteTaskGroup = new TaskGroup.RemoteTaskGroup(getContext());
        mTaskGroups.add(mRemoteTaskGroup);
        mAdapter = new Adapter(mTaskGroups);
        setAdapter(mAdapter);
    }

    private void initToken() {
        String[] columns = {"id", "user_id", "token"};
        String selection = "id = ?";
        String[] selectionArgs = {"1"};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        SQLiteDatabase db = databaseHelper.openDatabase("");
        Cursor cursor = db.query("device_login", columns, selection, selectionArgs, groupBy, having, orderBy);

    }

    public void refresh() {
        for (TaskGroup group : mTaskGroups) {
            group.refresh();
        }
        mAdapter = new Adapter(mTaskGroups);
        setAdapter(mAdapter);
        //notifyDataSetChanged not working...
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AutoJs.getInstance().getScriptEngineService().registerGlobalScriptExecutionListener(mScriptExecutionListener);
        mTimedTaskChangeDisposable = TimedTaskManager.INSTANCE.getTimeTaskChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTaskChange);
        mIntentTaskChangeDisposable = TimedTaskManager.INSTANCE.getIntentTaskChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTaskChange);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            refresh();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AutoJs.getInstance().getScriptEngineService().unregisterGlobalScriptExecutionListener(mScriptExecutionListener);
        mTimedTaskChangeDisposable.dispose();
        mIntentTaskChangeDisposable.dispose();
    }

    void onTaskChange(ModelChange taskChange) {
        if (taskChange.getAction() == ModelChange.INSERT) {
            mAdapter.notifyChildInserted(1, mPendingTaskGroup.addTask(taskChange.getData()));
        } else if (taskChange.getAction() == ModelChange.DELETE) {
            final int i = mPendingTaskGroup.removeTask(taskChange.getData());
            if (i >= 0) {
                mAdapter.notifyChildRemoved(1, i);
            } else {
                Log.w(LOG_TAG, "data inconsistent on change: " + taskChange);
                refresh();
            }
        } else if (taskChange.getAction() == ModelChange.UPDATE) {
            final int i = mPendingTaskGroup.updateTask(taskChange.getData());
            if (i >= 0) {
                mAdapter.notifyChildChanged(1, i);
            } else {
                refresh();
            }
        }
    }

    private class Adapter extends ExpandableRecyclerAdapter<TaskGroup, Task, TaskGroupViewHolder, TaskViewHolder> {

        public Adapter(@NonNull List<TaskGroup> parentList) {
            super(parentList);
        }

        @NonNull
        @Override
        public TaskGroupViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
            return new TaskGroupViewHolder(LayoutInflater.from(parentViewGroup.getContext())
                    .inflate(R.layout.dialog_code_generate_option_group, parentViewGroup, false));
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TaskViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.task_list_recycler_view_item, parent, false));
        }

        @Override
        public void onBindParentViewHolder(@NonNull TaskGroupViewHolder viewHolder, int parentPosition, @NonNull TaskGroup taskGroup) {
            viewHolder.title.setText(taskGroup.getTitle());
            initRemoteGroup(viewHolder);
        }

        @Override
        public void onBindChildViewHolder(@NonNull TaskViewHolder viewHolder, int parentPosition, int childPosition, @NonNull Task task) {
            viewHolder.bind(task);
        }

        @SuppressLint("CheckResult")
        private void initRemoteGroup(@NonNull TaskGroupViewHolder itemView) {
            TextView title = itemView.title;
            itemView.clickIcon = itemView.itemView.findViewById(R.id.async);
            if (title.getText().toString().equals("远程脚本")) {
                itemView.clickIcon.setOnClickListener(view -> {
                    ScriptService.getInstance().list()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(script -> {
                                        generatorRemoteScript(script);
                                        Toast.makeText(getApplicationContext(), R.string.text_get_script_list_success, Toast.LENGTH_SHORT).show();
                                    }
                                    , error -> {
                                        error.printStackTrace();
                                        Toast.makeText(getApplicationContext(), R.string.text_get_script_list_fail, Toast.LENGTH_SHORT).show();
                                    });
                    Log.i(LOG_TAG, "TaskGroupViewHolder: 点击了同步图标");
                });
            } else {
                itemView.clickIcon.setVisibility(View.GONE);
            }
        }

    }

    class TaskViewHolder extends ChildViewHolder<Task> {

        @BindView(R.id.first_char)
        TextView mFirstChar;
        @BindView(R.id.name)
        TextView mName;
        @BindView(R.id.desc)
        TextView mDesc;

        private Task mTask;
        private GradientDrawable mFirstCharBackground;

        private View itemView;

        TaskViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this::onItemClick);
            ButterKnife.bind(this, itemView);
            mFirstCharBackground = (GradientDrawable) mFirstChar.getBackground();
            this.itemView = itemView;
        }

        public void bind(Task task) {
            mTask = task;
            mName.setText(task.getName());
            mDesc.setText(task.getDesc());
            if (AutoFileSource.ENGINE.equals(mTask.getEngineName())) {
                mFirstChar.setText("R");
                mFirstCharBackground.setColor(getResources().getColor(R.color.color_r));
            } else {
                mFirstChar.setText("J");
                mFirstCharBackground.setColor(getResources().getColor(R.color.color_j));
            }

            if (mTask instanceof Task.RemoteTask) {
                ScriptSource oldStringScript = ScriptService.stringScriptMap.get(((Task.RemoteTask) mTask).getId());
                if (oldStringScript == null) {
                    itemView.findViewById(R.id.start).setVisibility(View.GONE);
                    itemView.findViewById(R.id.async).setVisibility(View.VISIBLE);
                } else {
                    itemView.findViewById(R.id.start).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.async).setVisibility(View.GONE);
                }
            }

        }


        @SuppressLint("NonConstantResourceId")
        @OnClick(R.id.stop)
        void stop() {
            if (mTask != null) {
                mTask.cancel();
                if (mTask instanceof Task.RemoteTask) {
                    Task.RemoteTask task = (Task.RemoteTask) mTask;
                    Long id = task.getId();

                    itemView.findViewById(R.id.async).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.start).setVisibility(View.GONE);
                    int size = mRemoteTaskGroup.mTasks.size();
                    mRemoteTaskGroup.mTasks.remove(task);
                    mAdapter.notifyChildInserted(getRemoteTaskGroupPos(), size);

                    ScriptService.localScriptList.remove(id);
                    ScriptService.stringScriptMap.remove(id);
                    ScriptService.userScriptMap.remove(id);
                    ScriptService.remoteScriptExecutionMap.remove(id);

                }

            }
        }

        @SuppressLint({"NonConstantResourceId", "CheckResult"})
        @OnClick(R.id.async)
        void async() {
            if (mTask != null) {
                Log.i(LOG_TAG, "onItemClick: 点击了同步脚本按钮");
                Task.RemoteTask task = (Task.RemoteTask) mTask;
                Long id = task.getId();
                downloadFile(id);
                asyncFile(id);
            }
        }


        private String getConfig(String config) {
            String newConfig = "";
            if (StringUtils.isAllBlank(config)) {
                Toast.makeText(getApplicationContext(), "设置配置文件失败，未找到用户配置信息！", Toast.LENGTH_SHORT).show();
            } else {
                newConfig = "var storage = storages.create(\"njC9gIPIwOGm\");" +
                        "storage.put(\"scriptConfig\"," + config + ");" +
                        "console.log(storage)";
            }
            return newConfig;
        }

        @SuppressLint("CheckResult")
        private void asyncFile(Long id) {
            ScriptSource oldStringScript = ScriptService.stringScriptMap.get(id);
            if (oldStringScript == null) {
                UserScript userScript = ScriptService.userScriptMap.get(id);
                DyScript dyScript = ScriptService.localScriptList.get(id);
                if (userScript != null && dyScript != null) {
                    String config = getConfig(userScript.getConfig());
                    String s = doEncipherScript(dyScript, userScript.getSecretKey());
                    s = config + "\n" + s;
                    Log.i(LOG_TAG, "asyncFile: " + s);
                    ScriptSource scriptSource = new StringScriptSource(dyScript.getScriptName(), s);
                    ScriptService.stringScriptMap.put(dyScript.getId(), scriptSource);
                    Toast.makeText(getApplicationContext(), "远程脚本同步成功！", Toast.LENGTH_SHORT).show();

                    itemView.findViewById(R.id.start).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.async).setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getApplicationContext(), "已经同步过远程脚本了！", Toast.LENGTH_SHORT).show();
            }
        }

        @SuppressLint({"NonConstantResourceId", "CheckResult"})
        @OnClick(R.id.start)
        void start() {
            if (mTask != null) {
                Log.i(LOG_TAG, "onItemClick: 点击了运行远程脚本");
                Task.RemoteTask task = (Task.RemoteTask) mTask;
                ScriptEngineService scriptEngineService = AutoJs.getInstance().getScriptEngineService();
                ScriptSource scriptSource = ScriptService.stringScriptMap.get(task.getId());
                if (scriptSource != null) {
                    scriptEngineService.execute(scriptSource, mScriptExecutionListener, new ExecutionConfig());
                    Toast.makeText(getApplicationContext(), "脚本启动成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "未找到改脚本", Toast.LENGTH_SHORT).show();
                }
            }
        }

        void onItemClick(View view) {
            if (mTask instanceof Task.PendingTask) {
                Task.PendingTask task = (Task.PendingTask) mTask;
                String extra = task.getTimedTask() == null ? TimedTaskSettingActivity.EXTRA_INTENT_TASK_ID
                        : TimedTaskSettingActivity.EXTRA_TASK_ID;
                TimedTaskSettingActivity_.intent(getContext())
                        .extra(extra, task.getId())
                        .start();
            }
            if (mTask instanceof Task.RemoteTask) {
                Log.i(LOG_TAG, "onItemClick: 点击了远程脚本");
            }
        }
    }

    private static class TaskGroupViewHolder extends ParentViewHolder<TaskGroup, Task> {
        TextView title;
        ImageView icon;
        ImageView clickIcon;

        TaskGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            icon = itemView.findViewById(R.id.icon);
            itemView.setOnClickListener(view -> {
                if (isExpanded()) {
                    collapseView();
                } else {
                    expandView();
                }
            });
        }

        @Override
        public void onExpansionToggled(boolean expanded) {
            icon.setRotation(expanded ? -90 : 0);
        }


    }


    private void generatorRemoteScript(TableDataInfo result) {
        int remoteTaskGroupPos = getRemoteTaskGroupPos();
        int size = mRemoteTaskGroup.getChildList().size();
        mRemoteTaskGroup.mTasks.clear();
        mAdapter.notifyChildRangeRemoved(remoteTaskGroupPos, 0, size);
        List rows = result.getRows();
        DownloadAsyncTask.performAsyncTask(() -> {
            try {
                for (int i = 0; i < rows.size(); i++) {
                    Gson gson = new Gson();
                    String jsonStr = gson.toJson(rows.get(i));
                    ScriptExecutionTask scriptExecutionTask = getScriptExecutionTask(jsonStr);
                    Object id = scriptExecutionTask.getConfig().getArgument("id");
                    RemoteScriptExecution remoteScriptExecution = new RemoteScriptExecution(new ScriptEngineManager(getContext()), scriptExecutionTask, (Long) id);
                    ScriptService.remoteScriptExecutionMap.put((Long) id, remoteScriptExecution);
                    int index = mRemoteTaskGroup.addTask(remoteScriptExecution);
                    mAdapter.notifyChildInserted(remoteTaskGroupPos, index);
                }
            } catch (Exception e) {
                String message = e.getMessage();
                Log.e(LOG_TAG, "Exception: " + e.getMessage(), e);
                Toast.makeText(getContext(), "同步脚本时出现异常：" + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private static ScriptExecutionTask getScriptExecutionTask(String json) {
        Gson gson = new Gson();
        DyScript dyScript = gson.fromJson(json, DyScript.class);
        ScriptSource scriptSource = new StringScriptSource(dyScript.getScriptName(), "toast('请先同步数据之后再操作')");
        ScriptExecutionGlobalListener listener = new ScriptExecutionGlobalListener();
        ExecutionConfig executionConfig = new ExecutionConfig();
        executionConfig.setArgument("id", dyScript.getId());
        return new ScriptExecutionTask(scriptSource, listener, executionConfig);
    }


    @SuppressLint("CheckResult")
    private void downloadFile(Long script) {
        UserScript userScript = ScriptService.userScriptMap.get(script);
        DyScript dyScript = ScriptService.localScriptList.get(script);
        if (dyScript == null) {
            ScriptService.getInstance().downloadFile(script)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                                ScriptService.localScriptList.put(file.getId(), file);
                            }
                            , error -> {
                                error.printStackTrace();
                                Toast.makeText(getApplicationContext(), R.string.text_get_script_list_fail, Toast.LENGTH_SHORT).show();
                            });
        }
        if (userScript == null) {
            ScriptService.getInstance().getKey(script).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                                ScriptService.userScriptMap.put(file.getId(), file);
                            }
                            , error -> {
                                error.printStackTrace();
                                Toast.makeText(getApplicationContext(), R.string.text_get_script_list_fail, Toast.LENGTH_SHORT).show();
                            });
        }

    }

    private DyScript getEncipherScript(Long fileName) {
        String s = readFile(fileName);
        Gson gson = new Gson();
        return gson.fromJson(s, DyScript.class);
    }

    private String doEncipherScript(DyScript dyScript, String secretKey) {
        List<EncipherScript> encipherScripts = dyScript.getEncipherScripts();
        List<String> _keys = dyScript.getKeys();
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < _keys.size(); i++) {
            keys.add(decryptData(_keys.get(i), secretKey));
        }
        List<String> body = new ArrayList<>();
        for (int i = 0; i < encipherScripts.size(); i++) {
            EncipherScript encipherScript = encipherScripts.get(i);
            body.add(encipherScript.getScriptBody());
        }
        return decryptStringFromChunks(body, keys);
    }

    public static String decryptStringFromChunks(List<String> encryptedChunks, List<String> decryptionKeys) {
        StringBuilder decryptedText = new StringBuilder();
        for (int i = 0; i < encryptedChunks.size(); i++) {
            int keyIndex = i % decryptionKeys.size(); // 根据片段序号选择解密密钥
            String chunkKey = decryptionKeys.get(keyIndex); // 获取对应的解密密钥
            String encryptedChunk = encryptedChunks.get(i); // 获取当前加密片段
            String decryptedChunk = decryptData(encryptedChunk, chunkKey); // 使用解密密钥对片段进行解密
            decryptedText.append(decryptedChunk); // 将解密后的片段拼接到最终的解密字符串中
        }
        return decryptedText.toString(); // 返回解密后的字符串
    }

    public static String decryptData(String encryptedData, String decryptionKey) {
        try {
            // 使用 Base64 解码加密后的数据
            byte[] encryptedBytes;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encryptedBytes = Base64.getDecoder().decode(encryptedData);
            } else {
                encryptedBytes = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT);
            }
            // 创建一个 AES 密钥
            SecretKeySpec secretKey = new SecretKeySpec(decryptionKey.getBytes(), "AES");
            // 创建解密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // 解密数据
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            // 将解密后的字节数组转换为字符串
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String readFile(Long fileName) {
        FileInputStream fis = null;
        try {
            fis = getContext().openFileInput(baseDir + fileName + JS);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void decryptScript(DyScript result) {
        Gson gson = new Gson();
        String json = gson.toJson(result);
        FileOutputStream fos = null;
        try {
            // 获取文件输出流
            fos = getContext().openFileOutput(baseDir + result.getId() + JS, Context.MODE_PRIVATE);
            // 写入数据
            fos.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getRemoteTaskGroupPos() {
        for (int i = 0; i < mTaskGroups.size(); i++) {
            if (mTaskGroups.get(i).getTitle().equals(getContext().getString(R.string.text_remote_task))) {
                return i;
            }
        }
        return -1;
    }

    static class DownloadAsyncTask {
        private static final Executor executor = Executors.newSingleThreadExecutor();

        public static void performAsyncTask(Runnable runnable) {
            executor.execute(runnable);
        }
    }

}