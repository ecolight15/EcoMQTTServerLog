
package jp.minecraftuser.ecomqttserverlog.uuid;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 UUID コマンドクラス
 * @author ecolight
 */
public class AsyncTaskCmdUUID extends UUIDTaskBase {
    
    // シングルトン実装
    private static AsyncTaskCmdUUID instance = null;
    public static final AsyncTaskCmdUUID getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncTaskCmdUUID(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncTaskCmdUUID(PluginFrame plg_) {
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
        String name;
        if (data.param.length == 0) {
            if (pl == null) {
                log.warning("コンソールからユーザー指定なしの実行はできません");
                data.result = false;
                return;
            }
            name = pl.getName();
        } else {
            name = data.param[0];
        }
        ArrayList<EcoUserUUIDData> list = db.listUUID(con, name);
        if (list == null) {
            pl.sendMessage("§d[情報]§fユーザー["+name+"]のUUID情報が取得できませんでした");
            data.result = false;
            return;
        }

        pl.sendMessage("§d[情報]§f=== ユーザー名["+name+"]のUUID情報を表示します ===");
        pl.sendMessage("§d[情報]§fUUID["+db.latestUUID(con, name).toString()+"]");
        // リストで検出した分表示
        SimpleDateFormat sd = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS]");
        for(EcoUserUUIDData dat : list) {
            pl.sendMessage("§d[情報]§fDate:"+sd.format(dat.getTime())+" - "+data.getName());
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
