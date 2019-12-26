
package jp.minecraftuser.ecomqttserverlog;

import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.MQTTController;
import jp.minecraftuser.ecomqttserverlog.commands.EcoMQTTServerLogCommand;
import jp.minecraftuser.ecomqttserverlog.commands.EcoMQTTServerLogReloadCommand;
import jp.minecraftuser.ecomqttserverlog.config.EcoMQTTServerLogConfig;
import jp.minecraftuser.ecomqttserverlog.listener.LoginLogoutListener;

/**
 * EcoMQTTプラグインを利用したサーバー関連ログのpublishプラグイン
 * @author ecolight
 */
public class EcoMQTTServerLog extends PluginFrame {
    static MQTTController con;
    /**
     * 起動時処理
     */
    @Override
    public void onEnable() {
        initialize();
    }

    /**
     * 終了時処理
     */
    @Override
    public void onDisable() {
        disable();
    }

    /**
     * MQTTController インスタンスを取得する
     * @return MQTTController インスタンス
     */
    public MQTTController getMQTTController() {
        if (con == null) { con = new MQTTController(this); }
        return con;
    }

    /**
     * 設定初期化
     */
    @Override
    public void initializeConfig() {
        EcoMQTTServerLogConfig conf = new EcoMQTTServerLogConfig(this);

        // Other settings
        conf.registerString("DateFormat");

        // Topic settings
        conf.registerBoolean("Topic.UserLogin.Enable");
        conf.registerString("Topic.UserLogin.Format");
        conf.registerBoolean("Topic.UserLogout.Enable");
        conf.registerString("Topic.UserLogout.Format");

        // MQTT settings
        conf.registerInt("Mqtt.Publish.QoS");

        registerPluginConfig(conf);
    }

    /**
     * コマンド初期化
     */
    @Override
    public void initializeCommand() {
        CommandFrame cmd = new EcoMQTTServerLogCommand(this, "ecms");
        cmd.addCommand(new EcoMQTTServerLogReloadCommand(this, "reload"));
        registerPluginCommand(cmd);
    }

    /**
     * イベントリスナー初期化
     */
    @Override
    public void initializeListener() {
        registerPluginListener(new LoginLogoutListener(this, "loginlogout"));
    }

}