
package jp.minecraftuser.ecomqttserverlog.online;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecomqttserverlog.listener.LoginLogoutListener;
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJson;

/**
 * タスク別処理分割用 JOIN クラス
 * @author ecolight
 */
public class OnlineTaskQuit extends OnlineTaskBase {
    Gson gson = new Gson();
    
    // シングルトン実装
    private static OnlineTaskQuit instance = null;
    public static final OnlineTaskQuit getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new OnlineTaskQuit(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public OnlineTaskQuit(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     * @throws java.sql.SQLException 
     */
    @Override
    public void asyncThread(OnlineThread thread, OnlineDB db, Connection con, OnlinePayload data) throws SQLException {
        // オンラインプレイヤーDBに挿入
        if (conf.getBoolean("OnlinePlayerLogger.Enabled")) {
            // publishの前にDB更新する
            db.deletePlayers(con, data.player);
            con.commit();
            log.log(Level.INFO, "OnlinePlayer Delete [{0}]", data.player.getName());
        }
        data.result = true;
        
        // ログアウトのpublishが無効であれば何もしない
        if (conf.getBoolean("Topic.UserLogout.Enable")) {
            // QoS 0 でユーザーログアウト情報を通知する
            LoginLogoutJson json = new LoginLogoutJson(
                    "logout",
                    data.player,
                    new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()),
                    conf.getString("Topic.UserLogout.URL"),
                    String.valueOf(plg.getServer().getOnlinePlayers().size() - 1)
            );
            try {
                ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.UserLogout.Format"), plg.getName()), gson.toJson(json).getBytes(), true, 0);
            } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
                Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(OnlineThread thread, OnlinePayload data) {
        if (data.result) {
            log.log(Level.INFO, "delete online player data [{0}]", data.player.getName());
        } else {
            log.log(Level.INFO, "delete online player data failed [{0}]", data.player.getName());
        }
    }
}
