
package jp.minecraftuser.ecomqttserverlog.online;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.db.CTYPE;
import jp.minecraftuser.ecomqttserverlog.model.LoginLogoutJsonPlayer;
import org.bukkit.entity.Player;


/**
 * プレイヤー固有ファイル保存
 * @author ecolight
 */
public class OnlineDB extends DatabaseFrame {

    public OnlineDB(PluginFrame plg_, String dbfilepath_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, dbfilepath_, name_);
    }

    public OnlineDB(PluginFrame plg_, String server_, String user_, String pass_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, server_, user_, pass_, dbname_, name_);
    }

    /**
     * データベース移行処理
     * 基底クラスからDBをオープンするインスタンスの生成時に呼ばれる
     * 
     * @throws SQLException
     */
    @Override
    protected void migrationData(Connection con) throws SQLException  {
        // 全体的にテーブル操作になるため、暗黙的コミットが走り失敗してもロールバックが効かない
        // 十分なテストの後にリリースするか、何らかの形で異常検知し、DBバージョンに従い元に戻せるようテーブル操作順を考慮する必要がある
        // 本処理においては取り敢えずロールバックは諦める
        
        // version 1 の場合、新規作成もしくは旧バージョンのデータベース引き継ぎの場合を検討する
        if (dbversion == 1) {
            if (justCreated) {
                // 新規作成の場合、初版のテーブルのみ作成して終わり
                
                // ログインプレイヤー情報テーブル
                MessageFormat mf = new MessageFormat(
                        "CREATE TABLE IF NOT EXISTS players("
                        + "most {0} NOT NULL, least {1} NOT NULL, world {2} NOT NULL, name {3} NOT NULL, "
                        + "disp {4} NOT NULL, plist_header {5}, plist_footer {6}, plist_name {7} NOT NULL, PRIMARY KEY(most, least))");
                try {
                    log.log(Level.INFO, "[players SQL]:" + mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc),
                        CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc),
                        CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc), CTYPE.STRING.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[players].");
                    Logger.getLogger(OnlineDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "Create table[players].");

                log.log(Level.INFO, "{0}DataBase checked.", name);
                try {
                    updateSettingsVersion(con);
                } catch (Exception e) {
                    log.log(Level.INFO, "Error updateSettingsVersion.");
                    Logger.getLogger(OnlineDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "create {0} version {1}", new Object[]{name, dbversion});
            } else {
                // 既存DB引き継ぎの場合はdbversionだけ上げてv2->3の処理へ
                log.log(Level.INFO, "convert {0} version 1 -> 2 start", name);
                try {
                    updateSettingsVersion(con);
                } catch (Exception e) {
                    log.log(Level.INFO, "Error updateSettingsVersion 1 -> 2.");
                    Logger.getLogger(OnlineDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.log(Level.INFO, "convert {0} version 1 -> 2 complete", name);
            }
        }
        // Version 2 -> 3
//        if (dbversion == 2) {
//            updateSettingsVersion();
//            log.log(Level.INFO, "convert {0} version {1} -> {2} complete", new Object[]{name, dbversion - 1, dbversion});
//        }
    }

    /**
     *設定保存（プレイヤー指定）
     * @param con
     * @param uuid
     * @param key
     * @param value
     * @throws SQLException
     */
    public void updateSetting(Connection con, UUID uuid, String key, String value) throws SQLException {
        
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM settings WHERE most = ? AND least = ? AND key = ?");
        PreparedStatement prep2 = con.prepareStatement("UPDATE settings SET value = ? WHERE most = ? AND least = ? AND key = ?");
        PreparedStatement prep3 = con.prepareStatement("INSERT INTO settings VALUES(?, ?, ?, ?)");
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            prep1.setString(3, key);
            ResultSet rs = prep1.executeQuery();
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
            
            if (hit) {
                // update
                prep2.setString(1, value);
                prep2.setLong(2, uuid.getMostSignificantBits());
                prep2.setLong(3, uuid.getLeastSignificantBits());
                prep2.setString(4, key);
                // 実行
                prep2.executeUpdate();
            } else { 
                // insert
                prep3.setLong(1, uuid.getMostSignificantBits());
                prep3.setLong(2, uuid.getLeastSignificantBits());
                prep3.setString(3, key);
                prep3.setString(4, value);
                // 実行
                prep3.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            prep3.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
        prep3.close();
    }
    
    /**
     * 設定取得
     * @param con 
     * @param uuid 
     * @param key 
     * @return  
     * @throws SQLException
     */
    public String getSetting(Connection con, UUID uuid, String key) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM settings WHERE most = ? AND least = ? AND key = ?");
        String result = "";
        try {
            prep1.setLong(1, uuid.getMostSignificantBits());
            prep1.setLong(2, uuid.getLeastSignificantBits());
            prep1.setString(3, key);
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                if (rs.next()) {
                    result = rs.getString("value");
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        return result;
    }

    /**
     * オンラインプレイヤー情報保存
     * @param con
     * @param pl
     * @throws SQLException
     */
    public void updatePlayers(Connection con, Player pl) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM players WHERE most = ? AND least = ?");
        PreparedStatement prep2 = con.prepareStatement("INSERT INTO players VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
        try {
            prep1.setLong(1, pl.getUniqueId().getMostSignificantBits());
            prep1.setLong(2, pl.getUniqueId().getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
            
            if (!hit) {
                // 無ければinsertする
                prep2.setLong(1, pl.getUniqueId().getMostSignificantBits());
                prep2.setLong(2, pl.getUniqueId().getLeastSignificantBits());
                prep2.setString(3, pl.getWorld().getName());
                prep2.setString(4, pl.getName());
                prep2.setString(5, pl.getDisplayName());
                prep2.setString(6, pl.getPlayerListHeader());
                prep2.setString(7, pl.getPlayerListFooter());
                prep2.setString(8, pl.getPlayerListName());
                // 実行
                prep2.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
    }

    /**
     * オンラインプレイヤー情報保存
     * @param con
     * @param pl
     * @throws SQLException
     */
    public void deletePlayers(Connection con, Player pl) throws SQLException {
        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM players WHERE most = ? AND least = ?");
        PreparedStatement prep2 = con.prepareStatement("DELETE FROM players WHERE most = ? AND least = ?");
        try {
            prep1.setLong(1, pl.getUniqueId().getMostSignificantBits());
            prep1.setLong(2, pl.getUniqueId().getLeastSignificantBits());
            ResultSet rs = prep1.executeQuery();
            boolean hit = false;
            try {
                // 結果取得
                if (rs.next()) {
                    hit = true;
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
            
            if (hit) {
                // あればdeleteする
                prep2.setLong(1, pl.getUniqueId().getMostSignificantBits());
                prep2.setLong(2, pl.getUniqueId().getLeastSignificantBits());
                // 実行
                prep2.executeUpdate();
            }

        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            prep2.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        prep2.close();
    }

    /**
     * オンラインプレイヤー情報ロード
     * @param con 
     * @return  
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    public ConcurrentHashMap<UUID, LoginLogoutJsonPlayer> loadPlayers(Connection con) throws SQLException, IOException {
        ConcurrentHashMap<UUID, LoginLogoutJsonPlayer> ret = new ConcurrentHashMap<>();

        // SQLコンパイル
        PreparedStatement prep1 = con.prepareStatement("SELECT * FROM players");
        try {
            ResultSet rs = prep1.executeQuery();
            try {
                // 結果取得
                while (rs.next()) {
                    LoginLogoutJsonPlayer pl = new LoginLogoutJsonPlayer();
                    UUID uid = new UUID(rs.getLong("most"), rs.getLong("least")); 
                    pl.uuid = uid;
                    pl.world = rs.getString("world");
                    pl.name = rs.getString("name");
                    pl.disp = rs.getString("disp");
                    pl.plist_header = rs.getString("plist_header");
                    pl.plist_footer = rs.getString("plist_footer");
                    pl.plist_name = rs.getString("plist_name");
                    ret.put(uid, pl);
                }
            } catch (SQLException ex) {
                // PreparedStatementがcloseできればカーソルリークしないはずだが、
                // 念のため確実にResultSetをcloseするようにしておく
                rs.close();
                // 投げなおして上位で異常検知させる
                throw ex;
            }
            rs.close();
        } catch (SQLException ex) {
            // 抜けるため後処理
            prep1.close();
            // 投げなおして上位で異常検知させる
            throw ex;
        }
        // 後処理
        prep1.close();
        
        return ret;
    }

}
