/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package Lsimulator.server.server.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.utils.SQLUtil;

public class InnKeyTable {

	private static Logger _log = Logger.getLogger(InnKeyTable.class.getName());

	private InnKeyTable() {
	}

	public static void StoreKey(ItemInstance item) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO inn_key SET item_obj_id=?, key_id=?, npc_id=?, hall=?, due_time=?");

			pstm.setInt(1, item.getId());
			pstm.setInt(2, item.getKeyId());
			pstm.setInt(3, item.getInnNpcId());
			pstm.setBoolean(4, item.checkRoomOrHall());
			pstm.setTimestamp(5, item.getDueTime());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public static void DeleteKey(ItemInstance item) {
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM inn_key WHERE item_obj_id=?");
			pstm.setInt(1, item.getId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);

		}
	}

	public static boolean checkey(ItemInstance item) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM inn_key WHERE item_obj_id=?");

			pstm.setInt(1, item.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				int itemObj = rs.getInt("item_obj_id");
				if (item.getId() == itemObj) {
					item.setKeyId(rs.getInt("key_id"));
					item.setInnNpcId(rs.getInt("npc_id"));
					item.setHall(rs.getBoolean("hall"));
					item.setDueTime(rs.getTimestamp("due_time"));
					return true;
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return false;
	}

}
