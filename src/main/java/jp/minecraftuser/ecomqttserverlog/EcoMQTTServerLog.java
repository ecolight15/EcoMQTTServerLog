
package jp.minecraftuser.ecomqttserverlog;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTRegisterFailException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqttserverlog.commands.EcoMQTTServerLogCommand;
import jp.minecraftuser.ecomqttserverlog.commands.EcoMQTTServerLogPlayerCommand;
import jp.minecraftuser.ecomqttserverlog.commands.EcoMQTTServerLogReloadCommand;
import jp.minecraftuser.ecomqttserverlog.commands.UnameCommand;
import jp.minecraftuser.ecomqttserverlog.commands.UuidCommand;
import jp.minecraftuser.ecomqttserverlog.config.EcoMQTTServerLogConfig;
import jp.minecraftuser.ecomqttserverlog.listener.EnableDisableListener;
import jp.minecraftuser.ecomqttserverlog.listener.LoginLogoutListener;
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJsonPlayer;
import jp.minecraftuser.ecomqttserverlog.online.OnlineDB;
import jp.minecraftuser.ecomqttserverlog.online.OnlinePayload;
import jp.minecraftuser.ecomqttserverlog.online.OnlineThread;
import jp.minecraftuser.ecomqttserverlog.receiver.LoginLogoutReceiver;
import jp.minecraftuser.ecomqttserverlog.uuid.EcoUserUUIDData;
import jp.minecraftuser.ecomqttserverlog.uuid.UUIDDB;
import jp.minecraftuser.ecomqttserverlog.uuid.UUIDThread;

/**
 * EcoMQTTプラグインを利用したサーバー関連ログのpublishプラグイン
 * @author ecolight
 */
public class EcoMQTTServerLog extends PluginFrame {
    private EnableDisableListener listener;
    static LoginLogoutReceiver con;
    /**
     * 起動時処理
     */
    @Override
    public void onEnable() {
        initialize();
        listener = new EnableDisableListener(this);
        listener.onEnable();
        // プラグイン起動時点でのオンラインユーザーリストを確保する
        // 以降の更新はマニュアル(以下メソッド実行)または、自動(★)
        // ★サーバー間でプレイヤーのログインログアウトを通知しているEcoMQTTServerLogと組み合わせる事で、動的に更新させることができる。
        if (getDefaultConfig().getBoolean("OnlinePlayerLogger.Enabled")) {
            requestUpdateOnlinePlayers();

            try {
                // チャットデータ通信用 QoS 1 で受信設定
                LoginLogoutReceiver loginlogout_ = getMQTTController();
                loginlogout_.registerReceiver(
                        MQTTManager.cnv(
                            getDefaultConfig().getString("Topic.UserLogin.Format"),
                            getName(),                                          // 
                            "{server}"                                          // 全サーバから受信するため、サーバ名をシングルレベルワイルドカード指定で受信登録する
                        ),
                        loginlogout_,                                           // レシーブハンドラの指定
                        true,                                                   // 余計なprefixを付けない指定
                        1);                                                     // QoS1(必ず1回は受信)
                loginlogout_.registerReceiver(
                        MQTTManager.cnv(
                            getDefaultConfig().getString("Topic.UserLogout.Format"),
                            getName(),                                          // 
                            "{server}"                                          // 全サーバから受信するため、サーバ名をシングルレベルワイルドカード指定で受信登録する
                        ),
                        loginlogout_,                                           // レシーブハンドラの指定
                        true,                                                   // 余計なprefixを付けない指定
                        1);                                                     // QoS1(必ず1回は受信)
            } catch (EcoMQTTPluginNotFoundException ex) {
                Logger.getLogger(EcoMQTTServerLog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EcoMQTTManagerNotFoundException ex) {
                Logger.getLogger(EcoMQTTServerLog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EcoMQTTRegisterFailException ex) {
                Logger.getLogger(EcoMQTTServerLog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }

    /**
     * 終了時処理
     */
    @Override
    public void onDisable() {
        listener.onDisable();
        disable();
    }

    /**
     * MQTTController インスタンスを取得する
     * @return MQTTController インスタンス
     */
    public LoginLogoutReceiver getMQTTController() {
        if (con == null) { con = new LoginLogoutReceiver(this); }
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
        conf.registerBoolean("Topic.OnEnable.Enable");
        conf.registerString("Topic.OnEnable.Format");
        conf.registerString("Topic.OnEnable.URL");
        conf.registerBoolean("Topic.OnDisable.Enable");
        conf.registerString("Topic.OnDisable.Format");
        conf.registerString("Topic.OnDisable.URL");

        conf.registerBoolean("Topic.UserLogin.Enable");
        conf.registerString("Topic.UserLogin.Format");
        conf.registerString("Topic.UserLogin.URL");
        conf.registerBoolean("Topic.UserLogout.Enable");
        conf.registerString("Topic.UserLogout.Format");
        conf.registerString("Topic.UserLogout.URL");

        // MQTT settings
        conf.registerInt("Mqtt.Publish.QoS");

        // UUID Logger        
        conf.registerBoolean("UUIDLogger.Enabled");

        // DB設定
        conf.registerString("UUIDLogger.Database.type");
        conf.registerString("UUIDLogger.Database.name");
        conf.registerString("UUIDLogger.Database.server");
        conf.registerString("UUIDLogger.Database.user");
        conf.registerString("UUIDLogger.Database.pass");
        
        // Import DB設定
        conf.registerBoolean("UUIDLogger.ImportEnabled");
        conf.registerString("UUIDLogger.Import.type");
        conf.registerString("UUIDLogger.Import.name");
        conf.registerString("UUIDLogger.Import.server");
        conf.registerString("UUIDLogger.Import.user");
        conf.registerString("UUIDLogger.Import.pass");

        // OnlinePlayer DB設定
        conf.registerBoolean("OnlinePlayerLogger.Enabled");
        conf.registerString("OnlinePlayerLogger.Database.type");
        conf.registerString("OnlinePlayerLogger.Database.name");
        conf.registerString("OnlinePlayerLogger.Database.server");
        conf.registerString("OnlinePlayerLogger.Database.user");
        conf.registerString("OnlinePlayerLogger.Database.pass");
        
        registerPluginConfig(conf);
    }

    /**
     * コマンド初期化
     */
    @Override
    public void initializeCommand() {
        CommandFrame cmd = new EcoMQTTServerLogCommand(this, "ecms");
        cmd.addCommand(new EcoMQTTServerLogReloadCommand(this, "reload"));
        cmd.addCommand(new EcoMQTTServerLogPlayerCommand(this, "player"));
        registerPluginCommand(cmd);

        registerPluginCommand(new UnameCommand(this, "uname"));
        registerPluginCommand(new UuidCommand(this, "uuid"));
    }

    /**
     * イベントリスナー初期化
     */
    @Override
    public void initializeListener() {
        registerPluginListener(new LoginLogoutListener(this, "loginlogout"));
    }

    /**
     * 定期実行タイマー初期化
     */
    @Override
    public void initializeTimer() {
        if (getDefaultConfig().getBoolean("UUIDLogger.Enabled")) {
            UUIDThread worker = UUIDThread.getInstance(this, "uuid");
            registerPluginTimer(worker);
            worker.runTaskTimer(this, 0, 20);
        }
        if (getDefaultConfig().getBoolean("OnlinePlayerLogger.Enabled")) {
            OnlineThread worker = OnlineThread.getInstance(this, "online");
            registerPluginTimer(worker);
            worker.runTaskTimer(this, 0, 20);
        }

    }

    /**
     * データベース初期化
     */
    @Override
    public void initializeDB() {
        EcoMQTTServerLogConfig conf = (EcoMQTTServerLogConfig) getDefaultConfig();
        if (conf.getBoolean("UUIDLogger.Enabled")) {
            try {
                UUIDDB newDB = null;
                if (conf.getString("UUIDLogger.Database.type").equalsIgnoreCase("sqlite")) {
                    newDB = new UUIDDB(this, conf.getString("UUIDLogger.Database.name"), "uuid");
                } else if (conf.getString("UUIDLogger.Database.type").equalsIgnoreCase("mysql")) {
                    newDB = new UUIDDB(this,
                            conf.getString("UUIDLogger.Database.server"),
                            conf.getString("UUIDLogger.Database.user"),
                            conf.getString("UUIDLogger.Database.pass"),
                            conf.getString("UUIDLogger.Database.name"),
                            "uuid");
                }
                if (newDB != null) {
                    registerPluginDB(newDB);
                    if (conf.getBoolean("UUIDLogger.ImportEnabled")) {
                        UUIDDB oldDB = null;
                        if (conf.getString("UUIDLogger.Import.type").equalsIgnoreCase("sqlite")) {
                            oldDB = new UUIDDB(this, conf.getString("UUIDLogger.Import.name"), "import");
                        } else if (conf.getString("UUIDLogger.Import.type").equalsIgnoreCase("mysql")) {
                            oldDB = new UUIDDB(this,
                                    conf.getString("UUIDLogger.Import.server"),
                                    conf.getString("UUIDLogger.Import.user"),
                                    conf.getString("UUIDLogger.Import.pass"),
                                    conf.getString("UUIDLogger.Import.name"),
                                    "import");
                        }
                        // DB convert
                        log.log(Level.INFO, "Start convert all data");
                        Connection con = oldDB.connect();
                        ArrayList<EcoUserUUIDData> alldata = oldDB.listAll(con);
                        con.close();
                        con = newDB.connect();
                        int count = 0;
                        for (EcoUserUUIDData data : alldata) {
                            count++;
                            if (count % 100 == 0) {
                                log.log(Level.INFO, "Insert data ("+count+"/"+alldata.size()+")");
                            }
                            newDB.insertPlayer(con, data);
                        }
                        log.log(Level.INFO, "Complete insert all data ("+count+"/"+alldata.size()+")");
                        con.commit();
                        con.close();
                    }
                } else {
                    log.log(Level.SEVERE, "Invalid UUID Database");
                }
            } catch (Exception ex) {
                Logger.getLogger(EcoMQTTServerLog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (conf.getBoolean("OnlinePlayerLogger.Enabled")) {
            try {
                if (conf.getString("OnlinePlayerLogger.Database.type").equalsIgnoreCase("sqlite")) {
                    registerPluginDB(new OnlineDB(this, conf.getString("OnlinePlayerLogger.Database.name"), "online"));
                } else if (conf.getString("OnlinePlayerLogger.Database.type").equalsIgnoreCase("mysql")) {
                    registerPluginDB(new OnlineDB(this,
                            conf.getString("OnlinePlayerLogger.Database.server"),
                            conf.getString("OnlinePlayerLogger.Database.user"),
                            conf.getString("OnlinePlayerLogger.Database.pass"),
                            conf.getString("OnlinePlayerLogger.Database.name"),
                            "online"));
                }
            } catch (Exception ex) {
                Logger.getLogger(EcoMQTTServerLog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    /**
     * UUID問い合わせ
     * @param name
     * @return 
     */
    public UUID latestUUID(String name) {
        if (!getDefaultConfig().getBoolean("UUIDLogger.Enabled")) {
            return null;
        }
        log.log(Level.INFO, "request latestUUID check[{0}]", name);
        UUIDDB db = (UUIDDB) getDB("uuid");
        Connection con;
        UUID ret = null;
        try {
            con = db.connect();
            ret = db.latestUUID(con, name);
            con.commit();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * NAME問い合わせ
     * @param uuid
     * @return 
     */
    public String latestName(UUID uuid) {
        if (!getDefaultConfig().getBoolean("UUIDLogger.Enabled")) {
            return null;
        }
        log.log(Level.INFO, "request latestName check[{0}]", uuid.toString());
        UUIDDB db = (UUIDDB) getDB("uuid");
        Connection con;
        String ret = null;
        try {
            con = db.connect();
            ret = db.latestName(con, uuid);
            con.commit();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(LoginLogoutListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * オンラインプレイヤー情報更新要求
     */
    public ConcurrentHashMap<UUID, LoginLogoutJsonPlayer> onlinePlayers; // 他プラグインから直接参照する
    public void requestUpdateOnlinePlayers() {
        if (onlinePlayers == null) onlinePlayers = new ConcurrentHashMap<>();
        OnlineThread worker = OnlineThread.getInstance(this, "online");
        OnlinePayload data = new OnlinePayload(this, OnlinePayload.Type.SERVER_UPDATE);
        worker.sendData(data);
    }
}
