
package jp.minecraftuser.ecomqttserverlog.uuid;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * メインスレッドと非同期スレッド間のデータ送受用クラス(メッセージ送受用)
 * @author ecolight
 */
public class UUIDPayload extends PayloadFrame {
    public boolean result = false;
    public Type type;
    public Type reloadtype;
    private PluginFrame plg;
    public ConcurrentHashMap<UUID, String> onlinePlayers;

    // 単一プレイヤー用
    public Player player;

    // 複数プレイヤー用
    public Player[] players;

    // その他実行種別ごとの必要な情報
    public String[] param;

    // 処理種別を追加した場合、AsyncSaveLoadTimer の initTask に処理クラスを登録すること
    public enum Type {
        NONE,
        RESET,
        CMD_UUID,
        CMD_UNAME,
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param player_
     * @param type_
     */
    public UUIDPayload(PluginFrame plg_, Player player_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        player = player_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param players_
     * @param type_
     */
    public UUIDPayload(PluginFrame plg_, Player[] players_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        players = players_;
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param type_
     */
    public UUIDPayload(PluginFrame plg_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
    }
    
    /**
     * コマンドの再キュー指定を子スレッドから親スレッドに依頼する
     */
    public void request_reset() {
        reloadtype = type;
        type = Type.RESET;
    }
    
    /**
     * 依頼されたリセットを実行
     */
    public void reset() {
        type = reloadtype;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PlayerDataPayload->");
        if (player != null) {
            sb.append("[").append(player.getName()).append("]");
        }
        return sb.toString();
    }
}
