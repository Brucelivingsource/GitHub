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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.templates.LsimulatorSpawnTime;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Maps;

public class SpawnTimeTable {
	private static Logger _log = Logger.getLogger(SpawnTimeTable.class.getName());

	private static SpawnTimeTable _instance;

	private final Map<Integer, LsimulatorSpawnTime> _times = Maps.newMap();

	public static SpawnTimeTable getInstance() {
		if (_instance == null) {
			_instance = new SpawnTimeTable();
		}
		return _instance;
	}

	private SpawnTimeTable() {
		load();
	}

	public LsimulatorSpawnTime get(int id) {
		return _times.get(id);
	}

	private void load() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_time");
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("spawn_id");
				LsimulatorSpawnTime.LsimulatorSpawnTimeBuilder builder = new LsimulatorSpawnTime.LsimulatorSpawnTimeBuilder(id);
				builder.setTimeStart(rs.getTime("time_start"));
				builder.setTimeEnd(rs.getTime("time_end"));
				// builder.setPeriodStart(rs.getTimestamp("period_start"));
				// builder.setPeriodEnd(rs.getTimestamp("period_end"));
				builder.setDeleteAtEndTime(rs.getBoolean("delete_at_endtime"));

				_times.put(id, builder.build());
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
}
