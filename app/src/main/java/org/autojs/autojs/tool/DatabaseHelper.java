package org.autojs.autojs.tool;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dyScript";
    public static String STORE_KEY = "";
    public static String TOKEN = "";


    public static String SECRET_KEY = "";

    public static String publicKey = "";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建加密的表格等初始化工作

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 更新数据库的操作
    }

    public SQLiteDatabase openDatabase(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        SECRET_KEY = key;
        Log.i(TAG, "openDatabase: " + SECRET_KEY);
        SQLiteDatabase db = getWritableDatabase(SECRET_KEY);
//        db.execSQL("PRAGMA key = '" + SECRET_KEY + "';"); // 设
        db.execSQL("CREATE TABLE " + "app_config" + " (" + "id" + " INTEGER PRIMARY KEY," + "public_key" + " TEXT," + "script" + " TEXT," + "private_key" + " TEXT" + ");");
        // 初始化配置
        db.execSQL("CREATE TABLE " + "script_config" + " (" + "id" + " INTEGER PRIMARY KEY,"
                + "user_id" + " int," + "script_id" + " int,"+ "config" + " TEXT" + ");");
        // 初始化脚本序列号
        db.execSQL("CREATE TABLE " + "script" + " (" + "id" + " INTEGER PRIMARY KEY,"
                + "script_name" + " varchar(32)," + "script_function" + " TEXT" + ");");
        // device_login
        db.execSQL("CREATE TABLE " + "device_login" + " (" + "id" + " INTEGER PRIMARY KEY,"
                + "login_device" + " varchar(32)," + "login_token" + " TEXT" + ");");
        // device_login
        db.execSQL("CREATE TABLE " + "device_login" + " (" + "id" + " INTEGER PRIMARY KEY ,"
                + "user_id" + " INTEGER," + "login_token" + " TEXT" + ");");
        return db;
    }
}