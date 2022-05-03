
package jp.minecraftuser.ecomqttserverlog.uuid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.db.CTYPE;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author ecolight
 */
public class UUIDDB extends DatabaseFrame {

    public UUIDDB(PluginFrame plg_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, dbname_, name_);
    }

    public UUIDDB(PluginFrame plg_, String addr_, String user_, String pass_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, addr_, user_, pass_, dbname_, name_);
    }


    /**
     * データベース移行処理
     * 内部処理からトランザクション開始済みの状態で呼ばれる
     * @throws SQLException
     */
    @Override
    protected void migrationData(Connection con) throws SQLException {
        // version 1 の場合、新規作成もしくは旧バージョンのデータベース引き継ぎの場合を検討する
        if (dbversion == 1) {
            if (justCreated) {
                // 新規作成の場合、テーブル定義のみ作成して終わり
                MessageFormat mf = new MessageFormat(
                          "CREATE TABLE IF NOT EXISTS UUIDTABLE("
                        + "MOSTUUID {0} NOT NULL,"
                        + "LEASTUUID {1} NOT NULL, "
                        + "NAME {2} NOT NULL, "
                        + "TIME {3} NOT NULL);");
                try {
                    log.log(Level.INFO, "[UUIDTABLE SQL]:" + mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[UUIDTABLE].");
                    Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase UUIDTABLE table checked.");

                // データベースバージョンは最新版数に設定する
                log.info("create " + name + " version 2");
                updateSettingsVersion(con, 2);
                return;
            } else {
 
                //-----------------------------------------------------------------------------------
                // データベースバージョンは次版にする
                //-----------------------------------------------------------------------------------
                updateSettingsVersion(con);
                
                log.info(plg.getName() + " database migration " + name + " version 1 -> 2 completed.");
            }
        }
    }

    /**
     * 新規プレイヤー登録
     * @param player プレイヤーインスタンス
     */
    public void checkPlayer(Connection con, OfflinePlayer player) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return;}
        String latestName = latestName(con, player.getUniqueId());
        // 最後に登録してある名前と現在の名前が同じなら登録の必要なし
        // 最後に登録した名前がない(新規)、または名前が変わっている場合には継続
        if ((latestName != null) && (player.getName().equals(latestName))) {
            log.info("not change name["+latestName+"]");
            return;
        }

        PreparedStatement prep = null;
        try {
            // UUIDテーブルに現在の名前を登録
            Date date = new Date();
            UUID uuid = player.getUniqueId();
            prep = con.prepareStatement("INSERT INTO UUIDTABLE(MOSTUUID, LEASTUUID, NAME, TIME) VALUES (?, ?, ?, ?);");
            prep.setLong(1, uuid.getMostSignificantBits());
            prep.setLong(2, uuid.getLeastSignificantBits());
            prep.setString(3, player.getName());
            prep.setLong(4, date.getTime());
            prep.executeUpdate();
            con.commit();
            prep.close();
            if (latestName == null) {
                log.info("Added uuid name["+player.getName()+"]");            
            } else {
                log.info("Added uuid name["+latestName+"]→["+player.getName()+"]");
            }
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }
    }

    /**
     * 指定UUIDのプレイヤーの最後のサーバー内での名称を取得する
     * @param uuid 検索UUID
     * @return プレイヤー名を返却
     */
    public String latestName(Connection con, UUID uuid) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return null;}
        String name = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT NAME FROM UUIDTABLE WHERE MOSTUUID = ? AND LEASTUUID = ? ORDER BY TIME DESC");
            prep.setLong(1, uuid.getMostSignificantBits());
            prep.setLong(2, uuid.getLeastSignificantBits());
            rs = prep.executeQuery();
            boolean result = rs.next();
            if (result) {
                name = rs.getString("NAME");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return name;
    }

    /**
     * プレイヤー名から最後のUUIDを検索する
     * @param name プレイヤー名
     * @return UUID
     */
    public UUID latestUUID(Connection con, String name) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return null;}
        UUID uuid = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT * FROM UUIDTABLE WHERE NAME LIKE ? ORDER BY TIME DESC");
            prep.setString(1, name);
            rs = prep.executeQuery();
            boolean result = rs.next();
            if (result) {
                long most = rs.getLong("MOSTUUID");
                long least = rs.getLong("LEASTUUID");
                uuid = new UUID(most, least);
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return uuid;
    }

    /**
     * 指定したUUIDが使用したサーバー内でのプレイヤー名を列挙する
     * @param uuid 検索UUID
     * @return プレイヤー名のリストを返却する
     */
    public ArrayList<EcoUserUUIDData> listName(Connection con, UUID uuid) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return null;}
        if (uuid == null) { log.log(Level.SEVERE, "Invalid uuid."); return null;}
        ArrayList<EcoUserUUIDData> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT * FROM UUIDTABLE WHERE MOSTUUID = ? AND LEASTUUID = ? ORDER BY TIME ASC");
            prep.setLong(1, uuid.getMostSignificantBits());
            prep.setLong(2, uuid.getLeastSignificantBits());
            rs = prep.executeQuery();
            while(true) {
                boolean result = rs.next();
                if (!result) break;
                list.add(new EcoUserUUIDData((EcoMQTTServerLog) plg, new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")), rs.getString("NAME"), new Date(rs.getLong("TIME"))));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (list.isEmpty()) return null;
        return list;
    }

    /**
     * 指定したプレイヤー名を使用したことのあるプレイヤーのUUIDを列挙する
     * @param name 検索プレイヤー名
     * @return UUIDのリストを返却する
     */
    public ArrayList<EcoUserUUIDData> listUUID(Connection con, String name) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return null;}
        ArrayList<EcoUserUUIDData> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT * FROM UUIDTABLE WHERE NAME = ? ORDER BY TIME ASC");
            prep.setString(1, name);
            rs = prep.executeQuery();
            while(true) {
                boolean result = rs.next();
                if (!result) break;
                list.add(new EcoUserUUIDData((EcoMQTTServerLog) plg, new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")), rs.getString("NAME"), new Date(rs.getLong("TIME"))));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (list.isEmpty()) return null;
        return list;
    }

    /**
     * 全データの返却
     * @return 全データ
     */
    public ArrayList<EcoUserUUIDData> listAll(Connection con) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return null;}
        ArrayList<EcoUserUUIDData> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT * FROM UUIDTABLE ORDER BY NAME ASC");
            rs = prep.executeQuery();
            while(true) {
                boolean result = rs.next();
                if (!result) break;
                list.add(new EcoUserUUIDData((EcoMQTTServerLog) plg, new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")), rs.getString("NAME"), new Date(rs.getLong("TIME"))));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (list.isEmpty()) return null;
        return list;
    }

    /**
     * 新規プレイヤー登録
     * @param data レコード
     */
    public void insertPlayer(Connection con, EcoUserUUIDData data) {
        if (con == null) { log.log(Level.SEVERE, "Invalid connection."); return;}

        PreparedStatement prep = null;
        try {
            // UUIDテーブルに現在の名前を登録
            prep = con.prepareStatement("INSERT INTO UUIDTABLE(MOSTUUID, LEASTUUID, NAME, TIME) VALUES (?, ?, ?, ?);");
            prep.setLong(1, data.getUUID().getMostSignificantBits());
            prep.setLong(2, data.getUUID().getLeastSignificantBits());
            prep.setString(3, data.getName());
            prep.setLong(4, data.getTime().getTime());
            prep.executeUpdate();
            con.commit();
            prep.close();
            log.info("Added uuid name["+data.getName()+"]");            
        } catch (SQLException ex) {
            log.info(ex.getLocalizedMessage());
            log.info(ex.getMessage());
            log.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(UUIDDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }
    }

}
