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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.templates.LsimulatorCastle;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Maps;

// Referenced classes of package Lsimulator.server.server:
// IdFactory

public class CastleTable {

	private static Logger _log = Logger.getLogger(CastleTable.class.getName());

	private static CastleTable _instance;

	private final Map<Integer, LsimulatorCastle> _castles = Maps.newConcurrentMap();

	public static CastleTable getInstance() {
		if (_instance == null) {
			_instance = new CastleTable();
		}
		return _instance;
	}

	private CastleTable() {
		load();
	}

	private Calendar timestampToCalendar(Timestamp ts) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());
		return cal;
	}

	private void load() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM castle ORDER BY castle_id ASC");

			rs = pstm.executeQuery();

			while (rs.next()) {
				LsimulatorCastle castle = new LsimulatorCastle(rs.getInt(1), rs.getString(2));
				castle.setWarTime(timestampToCalendar((Timestamp) rs.getObject(3)));
				castle.setTaxRate(rs.getInt(4));
				castle.setPublicMoney(rs.getInt(5));

				/** 設置擁有該城堡的血盟 */
				pstm = con.prepareStatement("SELECT clan_id FROM clan_data WHERE hascastle = ?");
				pstm.setInt(1, castle.getId());
				ResultSet rstemp = pstm.executeQuery();

				while (rstemp.next()) {
					castle.setHeldClan(rstemp.getInt(1));
				}

				_castles.put(castle.getId(), castle);
			}
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public LsimulatorCastle[] getCastleTableList() {
		return _castles.values().toArray(new LsimulatorCastle[_castles.size()]);
	}

	public LsimulatorCastle getCastleTable(int id) {
		return _castles.get(id);
	}

	public void updateCastle(LsimulatorCastle castle) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("UPDATE castle SET name=?, war_time=?, tax_rate=?, public_money=? WHERE castle_id=?");
			pstm.setString(1, castle.getName());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String fm = sdf.format(castle.getWarTime().getTime());
			pstm.setString(2, fm);
			pstm.setInt(3, castle.getTaxRate());
			pstm.setInt(4, castle.getPublicMoney());
			pstm.setInt(5, castle.getId());
			pstm.execute();

			_castles.put(castle.getId(), castle);
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

}
