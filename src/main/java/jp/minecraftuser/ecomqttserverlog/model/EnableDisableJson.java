
package jp.minecraftuser.ecomqttserverlog.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 起動停止情報 Jsonモデル
 * @author ecolight
 */
public class EnableDisableJson {
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("type")
    @Expose
    public String type;

    /**
     * コンストラクタ
     * @param type_ 種別
     * @param date_ 発生時刻
     * @param url_ 付帯URL(Lambda等でWebhook送信等で利用可能)
     */
    public EnableDisableJson(String type_, String date_, String url_) {
        date = date_;
        type = type_;
        url = url_;
    }
}
