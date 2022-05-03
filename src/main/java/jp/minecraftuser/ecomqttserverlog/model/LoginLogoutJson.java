
package jp.minecraftuser.ecomqttserverlog.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * ログイン・ログアウト情報 Jsonモデル
 * @author ecolight
 */
public class LoginLogoutJson {
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("count")
    @Expose
    public String count;
    @SerializedName("player")
    @Expose
    public LoginLogoutJsonPlayer player;

    /**
     * コンストラクタ
     * @param type_ 種別
     * @param p プレイヤーインスタンス
     * @param date_ 発生時刻
     * @param url_ 付帯URL(Lambda等でWebhook送信等で利用可能)
     * @param count_ ユーザー人数
     */
    public LoginLogoutJson(String type_, Player p, String date_, String url_, String count_) {
        date = date_;
        type = type_;
        url = url_;
        count = count_;
        player = new LoginLogoutJsonPlayer(p);
    }
    public void check() {
        Objects.requireNonNull(date);
        Objects.requireNonNull(type);
        Objects.requireNonNull(url);
        Objects.requireNonNull(count);
        Objects.requireNonNull(player);
        player.check();
    }
}
