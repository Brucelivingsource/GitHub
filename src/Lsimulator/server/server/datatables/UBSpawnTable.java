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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.model.LsimulatorUbPattern;
import Lsimulator.server.server.model.LsimulatorUbSpawn;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Maps;

public class UBSpawnTable {
	private static Logger _log = Logger.getLogger(UBSpawnTable.class.getName());

	private static UBSpawnTable _instance;

	private Map<Integer, LsimulatorUbSpawn> _spawnTable = Maps.newMap();

	public static UBSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new UBSpawnTable();
		}
		return _instance;
	}

	private UBSpawnTable() {
		loadSpawnTable();
	}

	private void loadSpawnTable() {

		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_ub");
			rs = pstm.executeQuery();

			while (rs.next()) {
				LsimulatorNpc npcTemp = NpcTable.getInstance().getTemplate(rs.getInt(6));
				if (npcTemp == null) {
					continue;
				}

				LsimulatorUbSpawn spawnDat = new LsimulatorUbSpawn();
				spawnDat.setId(rs.getInt(1));
				spawnDat.setUbId(rs.getInt(2));
				spawnDat.setPattern(rs.getInt(3));
				spawnDat.setGroup(rs.getInt(4));
				spawnDat.setName(npcTemp.get_name());
				spawnDat.setNpcTemplateId(rs.getInt(6));
				spawnDat.setAmount(rs.getInt(7));
				spawnDat.setSpawnDelay(rs.getInt(8));
				spawnDat.setSealCount(rs.getInt(9));

				_spawnTable.put(spawnDat.getId(), spawnDat);
			}
		}
		catch (SQLException e) {
			// problem with initializing spawn, go to next one
			_log.warning("spawn couldnt be initialized:" + e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_log.config("UBモンスター配置リスト " + _spawnTable.size() + "件ロード");
	}

	public LsimulatorUbSpawn getSpawn(int spawnId) {
		return _spawnTable.get(spawnId);
	}

	/**
	 * 指定されたUBIDに対するパターンの最大数を返す。
	 * 
	 * @param ubId
	 *            調べるUBID。
	 * @return パターンの最大数。
	 */
	public int getMaxPattern(int ubId) {
		int n = 0;
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT MAX(pattern) FROM spawnlist_ub WHERE ub_id=?");
			pstm.setInt(1, ubId);
			rs = pstm.executeQuery();
			if (rs.next()) {
				n = rs.getInt(1);
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
		return n;
	}

	public LsimulatorUbPattern getPattern(int ubId, int patternNumer) {
		LsimulatorUbPattern pattern = new LsimulatorUbPattern();
		for (LsimulatorUbSpawn spawn : _spawnTable.values()) {
			if ((spawn.getUbId() == ubId) && (spawn.getPattern() == patternNumer)) {
				pattern.addSpawn(spawn.getGroup(), spawn);
			}
		}
		pattern.freeze();

		return pattern;
	}
}
