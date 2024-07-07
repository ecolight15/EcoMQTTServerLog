
package jp.minecraftuser.ecomqttserverlog.online;

import com.google.gson.Gson;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecomqttserverlog.listener.LoginLogoutListener;
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJson;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * タスク別処理分割用 FIRST_JOIN クラス
 * @author ecolight
 */
public class OnlineTaskFirstJoin extends OnlineTaskBase {
    Gson gson = new Gson();

    // シングルトン実装
    private static OnlineTaskFirstJoin instance = null;
    public static final OnlineTaskFirstJoin getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new OnlineTaskFirstJoin(plg_);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param plg_
     */
    public OnlineTaskFirstJoin(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     * @throws SQLException
     */
    @Override
    public void asyncThread(OnlineThread thread, OnlineDB db, Connection con, OnlinePayload data) throws SQLException {


        // ログインのpublishが無効であれば何もしない
        if (conf.getBoolean("Topic.UserFirstLogin.Enable")) {
            // Config指定のQoSでユーザーログイン情報を通知する
            LoginLogoutJson json = new LoginLogoutJson(
                    "firstlogin",
                    data.player,
                    new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()),
                    conf.getString("Topic.UserFirstLogin.URL"),
                    String.valueOf(plg.getServer().getOnlinePlayers().size())
            );
            try {
                ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.UserFirstLogin.Format"), plg.getName()), gson.toJson(json).getBytes(), true, 0);
            } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
                Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
                data.result = false;
                return;
            }
        }
        data.result = true;
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(OnlineThread thread, OnlinePayload data) {

    }
}
