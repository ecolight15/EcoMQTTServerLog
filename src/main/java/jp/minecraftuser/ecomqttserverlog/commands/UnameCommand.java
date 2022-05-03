
package jp.minecraftuser.ecomqttserverlog.commands;

import java.util.ArrayList;
import java.util.List;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecomqttserverlog.uuid.UUIDPayload;
import jp.minecraftuser.ecomqttserverlog.uuid.UUIDThread;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * unameコマンドクラス
 * @author ecolight
 */
public class UnameCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public UnameCommand(PluginFrame plg_, String name_) {
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
        return "ecouser.uname";
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

        UUIDThread worker = (UUIDThread) plg.getPluginTimer("uuid");
        UUIDPayload data;
        if (sender == Bukkit.getConsoleSender()) {
            data = new UUIDPayload(plg, UUIDPayload.Type.CMD_UNAME);
        } else {
            data = new UUIDPayload(plg, (Player) sender, UUIDPayload.Type.CMD_UNAME);
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
            for (Player p : plg.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(strings[0].toLowerCase())) {
                    list.add(p.getName());
                }
            }
        }
        list.add("[<offlinePlayerName>]");
        return list;
    }
}
