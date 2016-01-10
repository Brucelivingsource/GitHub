package Lsimulator.server.server.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Lists;

public class LsimulatorDoorSpawn {
	private static Logger _log = Logger.getLogger(LsimulatorDoorSpawn.class.getName());
	private final int _id;
	private final LsimulatorDoorGfx _gfx;
	private final int _x;
	private final int _y;
	private final int _mapId;
	private final LsimulatorLocation _loc;
	private final int _hp;
	private final int _keeper;
	private final boolean _isOpening;

	public LsimulatorDoorSpawn(int id, LsimulatorDoorGfx gfx, int x, int y, int mapId, int hp,
			int keeper, boolean isOpening) {
		super();
		_id = id;
		_gfx = gfx;
		_x = x;
		_y = y;
		_mapId = mapId;
		_loc = new LsimulatorLocation(_x, _y, _mapId);
		_hp = hp;
		_keeper = keeper;
		_isOpening = isOpening;
	}

	public int getId() {
		return _id;
	}

	public LsimulatorDoorGfx getGfx() {
		return _gfx;
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}

	public int getMapId() {
		return _mapId;
	}

	public LsimulatorLocation getLocation() {
		return _loc;
	}

	public int getHp() {
		return _hp;
	}

	public int getKeeper() {
		return _keeper;
	}

	public boolean isOpening() {
		return _isOpening;
	}

	public static List<LsimulatorDoorSpawn> all() {
		List<LsimulatorDoorSpawn> result = Lists.newArrayList();
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_door");
			rs = pstm.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				int gfxId = rs.getInt("gfxid");
				int x = rs.getInt("locx");
				int y = rs.getInt("locy");
				int mapId = rs.getInt("mapid");
				int hp = rs.getInt("hp");
				int keeper = rs.getInt("keeper");
				boolean isOpening = rs.getBoolean("isOpening");
				LsimulatorDoorGfx gfx = LsimulatorDoorGfx.findByGfxId(gfxId);
				LsimulatorDoorSpawn spawn = new LsimulatorDoorSpawn(id, gfx, x, y, mapId, hp, keeper, isOpening);
				result.add(spawn);
			}

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return result;
	}
}
