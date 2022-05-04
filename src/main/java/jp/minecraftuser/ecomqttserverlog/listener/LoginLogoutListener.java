
package jp.minecraftuser.ecomqttserverlog.listener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecomqttserverlog.online.OnlinePayload;
import jp.minecraftuser.ecomqttserverlog.online.OnlineThread;
import jp.minecraftuser.ecomqttserverlog.uuid.UUIDDB;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * ログイン・ログアウトListenerクラス
 * @author ecolight
 */
public class LoginLogoutListener extends ListenerFrame {
    EcoMQTTServerLog plugin;
    
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public LoginLogoutListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
        plugin = (EcoMQTTServerLog) plg;
    }

    /**
     * プレイヤーログイン処理
     * プレイヤーのログインをDB保存+MQTTで通知する
     * 参考：同一プレイヤーが多重ログインしてきた場合のイベント発生順序は次の通り。
     * (A)Kick->(A)Quit->(B)AsyncPreLogin->(B)PreLogin->(B)Login->(B)Join
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        if (plugin.onlinePlayers == null) plugin.onlinePlayers = new ConcurrentHashMap<>();
        OnlineThread worker = OnlineThread.getInstance(plg, "online");
        OnlinePayload data = new OnlinePayload(plg, event.getPlayer(), OnlinePayload.Type.SERVER_JOIN);
        worker.sendData(data);
    }
          
    /**
     * プレイヤー切断処理
     * プレイヤーのログアウトをDB保存+MQTTで通知する
     * 参考：同一プレイヤーが多重ログインしてきた場合のイベント発生順序は次の通り。
     * (A)Kick->(A)Quit->(B)AsyncPreLogin->(B)PreLogin->(B)Login->(B)Join
     * @param event イベント
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        if (plugin.onlinePlayers == null) plugin.onlinePlayers = new ConcurrentHashMap<>();
        OnlineThread worker = OnlineThread.getInstance(plg, "online");
        OnlinePayload data = new OnlinePayload(plg, event.getPlayer(), OnlinePayload.Type.SERVER_QUIT);
        worker.sendData(data);
    }

    /**
     * プレイヤーログインイベント処理
     * @param event イベント情報
     */
    @EventHandler
    public void PlayerLogin(PlayerLoginEvent event)
    {
        // EcoUserManagerのDB更新処理
        if (plg.getDefaultConfig().getBoolean("UUIDLogger.Enabled")) {
            Player p = event.getPlayer();
            log.info("UUID check["+p.getName()+"]:"+p.getUniqueId().toString());
            UUIDDB db = (UUIDDB) plg.getDB("uuid");
            Connection con;
            try {
                con = db.connect();
                db.checkPlayer(con, p);
                con.commit();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
