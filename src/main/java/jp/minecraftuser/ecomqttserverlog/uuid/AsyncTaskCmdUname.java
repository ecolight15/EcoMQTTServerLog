
package jp.minecraftuser.ecomqttserverlog.uuid;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 UNAME コマンドクラス
 * @author ecolight
 */
public class AsyncTaskCmdUname extends UUIDTaskBase {
    
    // シングルトン実装
    private static AsyncTaskCmdUname instance = null;
    public static final AsyncTaskCmdUname getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncTaskCmdUname(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncTaskCmdUname(PluginFrame plg_) {
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
    public void asyncThread(UUIDThread thread, UUIDDB db, Connection con, UUIDPayload data) throws SQLException {
        Player pl = data.player;
        UUID uid;
        String name;
        if (data.param.length == 0) {
            if (pl == null) {
                log.warning("コンソールからユーザー指定なしの実行はできません");
                data.result = false;
                return;
            }
            uid = pl.getUniqueId();
            name = pl.getName();
        } else {
            uid = db.latestUUID(con, data.param[0]);
            name = data.param[0];
        }
        ArrayList<EcoUserUUIDData> list = db.listName(con, uid);
        if (list == null) {
            data.player.sendMessage("§d[情報]§fユーザー["+name+"]の関連UUID情報が取得できませんでした");
            data.result = false;
            return;
        }

        pl.sendMessage("§d[情報]§f=== ユーザー名["+name+"]の関連UUID情報を表示します ===");
        pl.sendMessage("§d[情報]§fNAME["+name+"]");
        // リストで検出した分表示
        SimpleDateFormat sd = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS]");
        for(EcoUserUUIDData uuid : list) {
            pl.sendMessage("§d[情報]§fDate:"+sd.format(uuid.getTime())+" - "+uuid.getUUID().toString());
        }
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(UUIDThread thread, UUIDPayload data) {

    }
}
