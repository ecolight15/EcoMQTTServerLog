
package jp.minecraftuser.ecomqttserverlog.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * ログイン・ログアウト情報(Player) Jsonモデル
 * @author ecolight
 */
public class LoginLogoutJsonPlayer {
    @SerializedName("uuid")
    @Expose
    public UUID uuid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("world")
    @Expose
    public String world;
    @SerializedName("host")
    @Expose
    public String host;
    @SerializedName("disp")
    @Expose
    public String disp;
    @SerializedName("plist_header")
    @Expose
    public String plist_header;
    @SerializedName("plist_footer")
    @Expose
    public String plist_footer;
    @SerializedName("plist_name")
    @Expose
    public String plist_name;

    public LoginLogoutJsonPlayer() {};
    /**
     * コンストラクタ
     * @param p プレイヤーインスタンス
     */
    public LoginLogoutJsonPlayer(Player p) {
        uuid = p.getUniqueId();
        name = p.getName();
        world = p.getWorld().getName();
        host = p.getAddress().getAddress().getHostAddress();
        disp = p.getDisplayName();
        plist_header = p.getPlayerListHeader();
        plist_footer = p.getPlayerListFooter();
        plist_name = p.getPlayerListName();
    }
    public void check() {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(name);
        Objects.requireNonNull(world);
        Objects.requireNonNull(host);
        if (disp == null) disp = "";
        if (plist_header == null) plist_header = "";
        if (plist_footer == null) plist_footer = "";
        if (plist_name == null) plist_name = "";
    }
}
