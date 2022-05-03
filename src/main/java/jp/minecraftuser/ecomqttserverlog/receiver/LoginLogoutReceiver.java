
package jp.minecraftuser.ecomqttserverlog.receiver;

import com.google.gson.Gson;
import java.util.logging.Level;
import jp.minecraftuser.ecomqtt.io.MQTTController;
import jp.minecraftuser.ecomqtt.io.MQTTReceiver;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJson;
import jp.minecraftuser.ecomqttserverlog.online.OnlinePayload;
import jp.minecraftuser.ecomqttserverlog.online.OnlineThread;

/**
 * MQTTサブスクライブ受信ハンドラ/パブリッシュ制御クラス
 * @author ecolight
 */
public class LoginLogoutReceiver extends MQTTController implements MQTTReceiver {
    Gson gson = new Gson();
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     */
    public LoginLogoutReceiver(EcoMQTTServerLog plg_) {
        super(plg_);
    }

    /**
     * コマンド受信登録ハンドラ
     * @param topic 受信トピック
     * @param payload 受信電文
     */
    @Override
    public void handler(String topic, byte[] payload) {
        LoginLogoutJson json;
        String param = new String(payload);
        try {
            json = gson.fromJson(param, LoginLogoutJson.class);
            json.check();   // メンバのnullチェック
        } catch(Exception e) {
            plg.getLogger().log(Level.WARNING,
                    "gson convert failed. payload[{1}]",
                    new Object[]{param});
            plg.getLogger().log(Level.WARNING, null, e);
            return;
        }
        plg.getLogger().log(Level.INFO, "topic[{0}] payload[{1}]", new Object[]{topic, new String(payload)});
        plg.getLogger().log(Level.INFO, "receive json:{0} player[{1}]", new Object[]{json.type, json.player.name});

        // とりあえずタスクにDBの読み出しを依頼
        EcoMQTTServerLog plugin = (EcoMQTTServerLog) plg;
        OnlineThread worker = (OnlineThread) plugin.getPluginTimer("online");
        OnlinePayload data = new OnlinePayload(plugin, OnlinePayload.Type.SERVER_UPDATE);
        worker.sendData(data);
    }
}
