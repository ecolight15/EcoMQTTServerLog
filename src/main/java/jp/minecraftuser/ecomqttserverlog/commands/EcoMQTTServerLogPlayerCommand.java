
package jp.minecraftuser.ecomqttserverlog.commands;

import java.util.ArrayList;
import java.util.List;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecomqttserverlog.online.OnlinePayload;
import jp.minecraftuser.ecomqttserverlog.online.OnlineThread;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class EcoMQTTServerLogPlayerCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EcoMQTTServerLogPlayerCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecomqttserverlog.player";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // パラメータチェック:1以上
        if (!checkRange(sender, args, 0, 1)) return true;

        OnlineThread worker = (OnlineThread) plg.getPluginTimer("online");
        OnlinePayload data;
        if (sender == Bukkit.getConsoleSender()) {
            data = new OnlinePayload(plg, OnlinePayload.Type.CMD_SHOW);
        } else {
            data = new OnlinePayload(plg, (Player) sender, OnlinePayload.Type.CMD_SHOW);
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("db")) {
                data.getDb = true;
            }
        }
        data.param = args.clone();
        worker.sendData(data);

        return true;
    }

    /**
     * コマンド別タブコンプリート処理
     * @param sender コマンド送信者インスタンス
     * @param cmd コマンドインスタンス
     * @param string コマンド文字列
     * @param strings パラメタ文字列配列
     * @return 保管文字列配列
     */
    @Override
    protected List<String> getTabComplete(CommandSender sender, Command cmd, String string, String[] strings) {
        ArrayList<String> list = new ArrayList<>();
        if (strings.length == 1) {
            list.add("db");
            list.add("plugin");
        }
        return list;
    }

}
