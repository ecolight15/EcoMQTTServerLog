
package jp.minecraftuser.ecomqttserverlog.listener;

import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecomqttserverlog.config.EcoMQTTServerLogConfig;
import jp.minecraftuser.ecomqttserverlog.model.EnableDisableJson;

/**
 * ログイン・ログアウトListenerクラス
 * @author ecolight
 */
public class EnableDisableListener{
    private final Gson gson = new Gson();
    private final PluginFrame plg;
    private final EcoMQTTServerLogConfig conf;
    
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     */
    public EnableDisableListener(PluginFrame plg_) {
        plg = plg_;
        conf = (EcoMQTTServerLogConfig) plg.getDefaultConfig();
    }

    /**
     * プラグインロード処理
     */
    public void onEnable() {
        // ログインのpublishが無効であれば何もしない
        if (!conf.getBoolean("Topic.OnEnable.Enable")) return;
        
        // Config指定のQoSでユーザーログイン情報を通知する
        EnableDisableJson json = new EnableDisableJson(
                "onenable",
                new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()),
                conf.getString("Topic.OnEnable.URL")
        );
        try {
            ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.OnEnable.Format"), plg.getName()), gson.toJson(json).getBytes(), true, 0);
        } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
            Logger.getLogger(EnableDisableListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
          
    /**
     * プラグインアンロード処理
     */
    public void onDisable() {
        // ログアウトのpublishが無効であれば何もしない
        if (!conf.getBoolean("Topic.OnDisable.Enable")) return;
        
        // QoS 0 でユーザーログアウト情報を通知する
        EnableDisableJson json = new EnableDisableJson(
                "ondisable",
                new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()),
                conf.getString("Topic.OnDisable.URL")
        );
        try {
            ((EcoMQTTServerLog)plg).getMQTTController().publish(MQTTManager.cnv(conf.getString("Topic.OnDisable.Format"), plg.getName()), gson.toJson(json).getBytes(), true, 0);
        } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
            Logger.getLogger(EnableDisableListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
