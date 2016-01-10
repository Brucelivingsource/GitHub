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

import Lsimulator.server.Config;
import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.model.LsimulatorSpawn;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Maps;

// Referenced classes of package Lsimulator.server.server:
// MobTable, IdFactory

public class NpcSpawnTable {

	private static Logger _log = Logger.getLogger(NpcSpawnTable.class.getName());

	private static NpcSpawnTable _instance;

	private Map<Integer, LsimulatorSpawn> _spawntable = Maps.newMap();

	private int _highestId;

	public static NpcSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new NpcSpawnTable();
		}
		return _instance;
	}

	private NpcSpawnTable() {
		fillNpcSpawnTable();
	}

	private void fillNpcSpawnTable() {

		int spawnCount = 0;

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_npc");
			rs = pstm.executeQuery();
			while (rs.next()) {
				if ( !Config.ALT_GMSHOP ) {
					int npcid = rs.getInt(1);
					if ((npcid >= Config.ALT_GMSHOP_MIN_ID) && (npcid <= Config.ALT_GMSHOP_MAX_ID)) {
						continue;
					}
				}
				if ( !Config.ALT_HALLOWEENIVENT  ) {
					int npcid = rs.getInt("id");
					if (((npcid >= 130852) && (npcid <= 130862)) || ((npcid >= 26656) && (npcid <= 26734)) 
						|| ((npcid >= 89634) && (npcid <= 89644))) {
						continue;
					}
				}
				if ( !Config.ALT_JPPRIVILEGED  ) {
					int npcid = rs.getInt("id");
					if ((npcid >= 1310368) && (npcid <= 1310379)) {
						continue;
					}
				}
				if ( !Config.ALT_TALKINGSCROLLQUEST  ) {
					int npcid = rs.getInt("id");
					if (((npcid >= 87537) && (npcid <= 87551)) || ((npcid >= 1310387) && (npcid <= 1310389))) {
						continue;
					}
				}
				if ( Config.ALT_TALKINGSCROLLQUEST  ) {
					int npcid = rs.getInt("id");
					if ((npcid >= 90066) && (npcid <= 90069)) {
						continue;
					}
				}
				int npcTemplateid = rs.getInt("npc_templateid");
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(npcTemplateid);
				LsimulatorSpawn l1spawn;
				if (l1npc == null) {
					_log.warning("mob data for id:" + npcTemplateid + " missing in npc table");
					l1spawn = null;
				}
				else {
					if (rs.getInt("count") == 0) {
						continue;
					}
					l1spawn = new LsimulatorSpawn(l1npc);
					l1spawn.setId(rs.getInt("id"));
					l1spawn.setAmount(rs.getInt("count"));
					l1spawn.setLocX(rs.getInt("locx"));
					l1spawn.setLocY(rs.getInt("locy"));
					l1spawn.setRandomx(rs.getInt("randomx"));
					l1spawn.setRandomy(rs.getInt("randomy"));
					l1spawn.setLocX1(0);
					l1spawn.setLocY1(0);
					l1spawn.setLocX2(0);
					l1spawn.setLocY2(0);
					l1spawn.setHeading(rs.getInt("heading"));
					l1spawn.setMinRespawnDelay(rs.getInt("respawn_delay"));
					l1spawn.setMapId(rs.getShort("mapid"));
					l1spawn.setMovementDistance(rs.getInt("movement_distance"));
					l1spawn.setName(l1npc.get_name());
					l1spawn.init();
					spawnCount += l1spawn.getAmount();

					_spawntable.put(new Integer(l1spawn.getId()), l1spawn);
					if (l1spawn.getId() > _highestId) {
						_highestId = l1spawn.getId();
					}
				}
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

		_log.config("NPC配置リスト " + _spawntable.size() + "件ロード");
		_log.fine("総NPC数 " + spawnCount + "件");
	}

	public void storeSpawn(LsimulatorPcInstance pc, LsimulatorNpc npc) {
		Connection con = null;
		PreparedStatement pstm = null;

		try {
			int count = 1;
			String note = npc.get_name();

			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO spawnlist_npc SET location=?,count=?,npc_templateid=?,locx=?,locy=?,heading=?,mapid=?");
			pstm.setString(1, note);
			pstm.setInt(2, count);
			pstm.setInt(3, npc.get_npcId());
			pstm.setInt(4, pc.getX());
			pstm.setInt(5, pc.getY());
			pstm.setInt(6, pc.getHeading());
			pstm.setInt(7, pc.getMapId());
			pstm.execute();
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);

		}
		finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public LsimulatorSpawn getTemplate(int i) {
		return _spawntable.get(i);
	}

	public void addNewSpawn(LsimulatorSpawn l1spawn) {
		_highestId++;
		l1spawn.setId(_highestId);
		_spawntable.put(l1spawn.getId(), l1spawn);
	}

}
