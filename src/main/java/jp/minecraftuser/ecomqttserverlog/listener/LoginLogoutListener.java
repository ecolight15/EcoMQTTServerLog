
package jp.minecraftuser.ecomqttserverlog.listener;

import com.google.gson.Gson;
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
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJson;
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
    Gson gson = new Gson();
    
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
        LoginLogoutJson json = new LoginLogoutJson(
                "login",
                event.getPlayer(),
                new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()),
                conf.getString("Topic.UserLogin.URL"),
                String.valueOf(plg.getServer().getOnlinePlayers().size())
        );
        try {
            ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.UserLogin.Format"), plg.getName()), gson.toJson(json).getBytes(), true, 0);
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
        LoginLogoutJson json = new LoginLogoutJson(
                "logout",
                event.getPlayer(),
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
