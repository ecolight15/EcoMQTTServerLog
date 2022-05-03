
package jp.minecraftuser.ecomqttserverlog.online;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;

/**
 * タスク別処理分割用 JOIN クラス
 * @author ecolight
 */
public class OnlineTaskUpdate extends OnlineTaskBase {

    // シングルトン実装
    private static OnlineTaskUpdate instance = null;
    public static final OnlineTaskUpdate getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new OnlineTaskUpdate(plg_);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param plg_ 
     */
    public OnlineTaskUpdate(PluginFrame plg_) {
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
        if (conf.getBoolean("OnlinePlayerLogger.Enabled")) {
            // オンラインプレイヤー情報読み出し
            ConcurrentHashMap<UUID, String> buf;
            try {
                data.onlinePlayers = db.loadPlayers(con);
                data.result = true;
            } catch (IOException ex) {
                Logger.getLogger(OnlineTaskUpdate.class.getName()).log(Level.SEVERE, null, ex);
                data.result = false;
            }
            for (UUID uid : data.onlinePlayers.keySet()) {
                log.log(Level.INFO, "Update Online Players[{0}][{1}][{2}]", new Object[]{data.onlinePlayers.get(uid).world, data.onlinePlayers.get(uid).name, uid.toString()});
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
            // ロードできたので更新
            ((EcoMQTTServerLog) plg).onlinePlayers = data.onlinePlayers;
            log.log(Level.INFO, "reload online player data");
        } else {
            log.log(Level.INFO, "reload online player data failed");
        }
    }
}
