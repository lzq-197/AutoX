package org.autojs.autojs.ui.main.scripts

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.Gson
import net.sqlcipher.database.SQLiteDatabase
import org.autojs.autojs.tool.AESEncryptionUtil
import org.autojs.autojs.tool.DatabaseHelper
import org.autojs.autojs.tool.KeyUtil
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


/**
 *
 * 实现和js的通讯
 * */


// 定义JavaScriptResult类，它包含biz字段和一个参数列表params
data class JavaScriptResult(val biz: String, val data: Map<String, Any>)

// 定义通用的JavaScriptHandler类型，它接收一个JavaScriptResult对象和参数列表params，并没有返回值
typealias JavaScriptHandler = (JavaScriptResult) -> Unit

// 创建处理函数1，接收JavaScriptResult和参数列表params1
fun test(result: JavaScriptResult) {
    val business = result.data
    val test = business?.get("test").toString()
    print(test)
}

// 创建一个Map，将biz值映射到对应的处理函数
val bizHandlers = mapOf(
    "test" to ::test
)


class MyJavaScriptInterface(val context: Context) {
    private val LOG_TAG = "MyJavaScriptInterface"
    val databaseHelper = DatabaseHelper(context)
    var db: SQLiteDatabase? = null
    var webView: WebView? = null
        set(value) {
            field = value
            // 这里可以在设置 webView 时执行一些额外的操作，如果需要的话
        }

    @JavascriptInterface
    fun callback(message: String) {
        if ("null" != message) {
            val gson = Gson();
            val fromJson = gson.fromJson(message, JavaScriptResult::class.java)
            bizHandlers[fromJson.biz]?.invoke(fromJson)
        } else {
            Log.i(LOG_TAG, "handlerJs: ")
        }
    }

    fun callJavaScriptFunction(functionName: String, vararg args: String) {
        val arguments = args.joinToString(", ") { "'$it'" }
        val javascriptCommand = "$functionName($arguments)"
        Log.i(LOG_TAG, "callJavaScriptFunction: $javascriptCommand")
        Handler(Looper.getMainLooper())
            .post {
                // 在主线程中执行调用 JavaScript 的代码
                webView?.evaluateJavascript(javascriptCommand, null)
//                webView?.loadUrl("javascript:JSMethod($javascriptCommand)")
            }
    }


    @SuppressLint("HardwareIds")
    private var androidId: String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID);


    @SuppressLint("HardwareIds")
    @JavascriptInterface
    fun init(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.i(LOG_TAG, "设备ID: $message")
        if (message != androidId) {
            return
        }
    }

    /**
     * 登录设备，获取token到数据库中
     * */
    @SuppressLint("HardwareIds")
    @JavascriptInterface
    fun loginDevice(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.i(LOG_TAG, "设备ID: $message")
        val gson = Gson()
        val fromJson = gson.fromJson(message, JavaScriptResult::class.java)
        val token = fromJson.data["token"]
        try {
            val generatorKey = KeyUtil.generatorKey()
            val decryptedString = AESEncryptionUtil.decrypt(token.toString(), generatorKey)
            Log.d("AES", "Decrypted String: $decryptedString")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (db !== null && db!!.isOpen) {
            var values = ContentValues()
            values.put("login_device", androidId)
            val selection = "id = ?"
            val selectionArgs = arrayOf("1")
            db!!.update("device_login", values, selection, selectionArgs)
        }
    }


    @JavascriptInterface
    fun showToast(message: String) {
        Log.i(LOG_TAG, "来自网页的消息: $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun notRegisterDevice() {
        Toast.makeText(context, "您的设备暂未激活：$androidId 请联系管理员！！！", Toast.LENGTH_SHORT)
            .show()
    }

    @JavascriptInterface
    fun initDb(message: String) {
        Log.i(LOG_TAG, "初始化数据库: $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        val gson = Gson()
        val fromJson = gson.fromJson(message, JavaScriptResult::class.java)
        val token = fromJson.data["token"]

        db = databaseHelper.openDatabase(message)
        if (db !== null && db!!.isOpen) {
            webView?.post {
                callJavaScriptFunction("initDb", "")
            }
        }
    }

    @JavascriptInterface
    fun getPublicKey(message: String) {
        Log.i(LOG_TAG, "获取公钥: $message")
        val gson = Gson()
        val fromJson = gson.fromJson(message, JavaScriptResult::class.java)
        val get = fromJson.data["key"]
        DatabaseHelper.publicKey = get.toString()
        db = databaseHelper.openDatabase(fromJson.data["pwd"].toString())
        if (db !== null && db!!.isOpen) {
            val selectionArgs = arrayOf("1")
            var cursor: Cursor =
                db!!.rawQuery("select * from app_config where id = ?", selectionArgs)
            if (cursor.moveToNext()) {
                Toast.makeText(context, "存在历史记录正在更新中。。。", Toast.LENGTH_SHORT)
                    .show()
                DatabaseHelper.publicKey = cursor.getString(1)
                val values = ContentValues()
                values.put("public_key", DatabaseHelper.publicKey)
                val selection = "id = ?"
                val selectionArgs = arrayOf("1")
                try {
                    db!!.beginTransaction();
                    db!!.update("app_config", values, selection, selectionArgs)
                    db!!.setTransactionSuccessful()
                } finally {
                    db!!.endTransaction();
                }
            } else {
                val values = ContentValues()
                values.put("public_key", message)
                values.put("id", 1)
                try {
                    db!!.beginTransaction();
                    db!!.insert("app_config", "script", values)
                    db!!.setTransactionSuccessful()
                } finally {
                    db!!.endTransaction();
                }
            }
        } else {
            db = databaseHelper.openDatabase(message)
        }
    }

    // 获取私钥
    @JavascriptInterface
    fun getPrivateKey(message: String) {
        Log.i(LOG_TAG, "获取私钥: $message")
        if (db != null && db!!.isOpen) {
            val gson = Gson()
            val fromJson = gson.fromJson(message, JavaScriptResult::class.java)
            val privateKey = fromJson.data["key"]
            val selectionArgs = arrayOf("1")
            var cursor: Cursor =
                db!!.rawQuery("select * from app_config where id = ?", selectionArgs)
            if (cursor.moveToNext()) {
                val values = ContentValues()
                values.put("private_key", privateKey.toString())
                val selection = "id = ?"
                val selectionArgs = arrayOf("1")
                try {
                    db!!.beginTransaction();
                    db!!.update("app_config", values, selection, selectionArgs)
                    db!!.setTransactionSuccessful()
                } finally {
                    db!!.endTransaction();
                }
            } else {
                val values = ContentValues()
                values.put("private_key", privateKey.toString())
                values.put("public_key", DatabaseHelper.publicKey)
                values.put("id", 1)
                try {
                    db!!.beginTransaction();
                    db!!.insert("app_config", null, values)
                    db!!.setTransactionSuccessful()
                } finally {
                    db!!.endTransaction();
                }
            }
            Toast.makeText(context, "获取私钥成功！", Toast.LENGTH_SHORT).show()
        }
    }


    @JavascriptInterface
    fun saveScriptConfig(message: String) {
        Log.i(LOG_TAG, "保存脚本配置: ")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        val gson = Gson()
        val fromJson = gson.fromJson(message, JavaScriptResult::class.java)
        val scriptName = fromJson.data["script_name"]
        val scriptFunction = fromJson.data["script_name"]
        val arguments = fromJson.data["arguments"]
        val serial_number = fromJson.data["arguments"]

        if (db != null && db!!.isOpen) {
            val selectionArgs = arrayOf(scriptName)
            var cursor: Cursor =
                db!!.rawQuery("select * from script where script_name = ?", selectionArgs)
            // 存在脚本则进行更新
            if (cursor.moveToNext()) {
                val values = ContentValues()
                values.put("script_function", scriptFunction.toString())
                values.put("arguments", arguments.toString())
                values.put("serial_number", arguments.toString())
                val selection = "id = ?"
                val selectionArgs = arrayOf("1")
                try {
                    db!!.beginTransaction();
                    db!!.update("app_config", values, selection, selectionArgs)
                    db!!.setTransactionSuccessful()
                } finally {
                    db!!.endTransaction();
                }
            }
        }
    }


    @JavascriptInterface
    fun saveScriptTo(message: String) {
        Log.i(LOG_TAG, "保存脚本: ")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun runScript(message: String) {
        Log.i(LOG_TAG, "运行脚本: ")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun encryptWithPublicKey(publicKeyString: String, plainText: String): String {
        val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey: PublicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS8Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun rsaDecryptLongString(privateKeyString: String, encryptedString: String): String {
        val privateKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS8Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

// 如果无法转换为 RSAPrivateKey，可能需要处理其他类型的 PrivateKey
        // 可以根据实际情况进行适当的处理
        // 尝试将 PrivateKey 对象转换为 RSAPrivateKey 类型
        if (privateKey is RSAPrivateKey) {
            // 现在你可以将 rsaPrivateKey 作为参数传递给 getBlockSize 方法
            val blockSize = getBlockSize(privateKey)
            val encryptedBytes = Base64.decode(encryptedString, Base64.DEFAULT)
            val totalSize = encryptedBytes.size

            var offset = 0
            val byteArrayOutputStream = ByteArrayOutputStream()

            while (offset < totalSize) {
                val thisBlockSize = Math.min(totalSize - offset, blockSize)
                val decryptedBlock = cipher.doFinal(encryptedBytes, offset, thisBlockSize)
                byteArrayOutputStream.write(decryptedBlock)
                offset += blockSize
            }

            val decryptedBytes = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } else {
            return ""
        }

    }

    fun getBlockSize(privateKey: RSAPrivateKey): Int {
        val keySizeInBytes = privateKey.modulus.bitLength() / 8
        return keySizeInBytes - 11
    }

}
