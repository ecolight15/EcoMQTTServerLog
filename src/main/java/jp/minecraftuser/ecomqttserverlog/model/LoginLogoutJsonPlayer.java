
package jp.minecraftuser.ecomqttserverlog.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;

/**
 * ログイン・ログアウト情報(Player) Jsonモデル
 * @author ecolight
 */
public class LoginLogoutJsonPlayer {
    @SerializedName("uuid")
    @Expose
    public String uuid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("world")
    @Expose
    public String world;
    @SerializedName("host")
    @Expose
    public String host;

    /**
     * コンストラクタ
     * @param p プレイヤーインスタンス
     */
    LoginLogoutJsonPlayer(Player p) {
        uuid = p.getUniqueId().toString();
        name = p.getName();
        world = p.getWorld().getName();
        host = p.getAddress().getAddress().getHostAddress();
    }
}
