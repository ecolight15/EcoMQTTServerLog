
package jp.minecraftuser.ecomqttserverlog.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * ログイン・ログアウトListenerクラス
 * @author ecolight
 */
public class LoginLogoutListener extends ListenerFrame {
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public LoginLogoutListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * プレイヤーログイン処理
     * プレイヤーのログインをMQTTで通知する
     * 参考：同一プレイヤーが多重ログインしてきた場合のイベント発生順序は次の通り。
     * (A)Kick->(A)Quit->(B)AsyncPreLogin->(B)PreLogin->(B)Login->(B)Join
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        // ログインのpublishが無効であれば何もしない
        if (!conf.getBoolean("Topic.UserLogin.Enable")) return;
        
        // Config指定のQoSでユーザーログイン情報を通知する
        Player p = event.getPlayer();
        StringBuilder sb = new StringBuilder(new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()));
        sb.append(",");
        sb.append(p.getUniqueId().toString());
        sb.append(",");
        sb.append(p.getName());
        sb.append(",");
        sb.append(p.getWorld().toString());
        sb.append(",");
        sb.append(p.getAddress().getAddress().getHostAddress());
        try {
            
            ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.UserLogout.Format"), plg.getName()), sb.toString().getBytes(), true, 0);
        } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
            Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
          
    /**
     * プレイヤー切断処理
     * プレイヤーのログアウトをMQTTで通知する
     * 参考：同一プレイヤーが多重ログインしてきた場合のイベント発生順序は次の通り。
     * (A)Kick->(A)Quit->(B)AsyncPreLogin->(B)PreLogin->(B)Login->(B)Join
     * @param event イベント
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        // ログアウトのpublishが無効であれば何もしない
        if (!conf.getBoolean("Topic.UserLogout.Enable")) return;
        
        // QoS 0 でユーザーログアウト情報を通知する
        Player p = event.getPlayer();
        StringBuilder sb = new StringBuilder(new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()));
        sb.append(",");
        sb.append(p.getUniqueId().toString());
        sb.append(",");
        sb.append(p.getName());
        sb.append(",");
        sb.append(p.getWorld().toString());
        sb.append(",");
        sb.append(p.getAddress().getAddress().getHostAddress());
        try {
            ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.UserLogout.Format"), plg.getName()), sb.toString().getBytes(), true, 0);
        } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
            Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
