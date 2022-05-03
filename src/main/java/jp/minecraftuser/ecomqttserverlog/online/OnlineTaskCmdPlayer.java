
package jp.minecraftuser.ecomqttserverlog.online;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJsonPlayer;

/**
 * タスク別処理分割用 JOIN クラス
 * @author ecolight
 */
public class OnlineTaskCmdPlayer extends OnlineTaskBase {

    // シングルトン実装
    private static OnlineTaskCmdPlayer instance = null;
    public static final OnlineTaskCmdPlayer getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new OnlineTaskCmdPlayer(plg_);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param plg_ 
     */
    public OnlineTaskCmdPlayer(PluginFrame plg_) {
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
        if (data.getDb == false) {
            data.result = true;
            return;
        }
        if (conf.getBoolean("OnlinePlayerLogger.Enabled")) {
            // オンラインプレイヤー情報読み出し
            ConcurrentHashMap<UUID, String> buf;
            try {
                data.onlinePlayers = db.loadPlayers(con);
                data.result = true;
            } catch (IOException ex) {
                Logger.getLogger(OnlineTaskCmdPlayer.class.getName()).log(Level.SEVERE, null, ex);
                data.result = false;
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
            ConcurrentHashMap<UUID,LoginLogoutJsonPlayer> map;
            if (data.getDb) {
                map = data.onlinePlayers;
            } else {
                map = ((EcoMQTTServerLog) plg).onlinePlayers;
            }
            MessageFormat mf = new MessageFormat("{0}:[{1}][{2}]");
            int num = 1;
            for (LoginLogoutJsonPlayer pl : map.values()) {
                if (data.player != null) {
                    Utl.sendPluginMessage(plg, data.player,mf.format(new String[]{String.valueOf(num), pl.world, pl.name}));
                } else {
                    log.info(mf.format(new String[]{String.valueOf(num), pl.world, pl.name}));
                }
                num++;
            }
        } else {
            log.log(Level.INFO, "show online player data failed");
        }
    }
}
