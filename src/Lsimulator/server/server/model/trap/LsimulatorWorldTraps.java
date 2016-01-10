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
package Lsimulator.server.server.model.trap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.TrapTable;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorTrapInstance;
import Lsimulator.server.server.types.Point;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Lists;

public class LsimulatorWorldTraps {
	private static Logger _log = Logger.getLogger(LsimulatorWorldTraps.class.getName());

	private List<LsimulatorTrapInstance> _allTraps = Lists.newList();

	private List<LsimulatorTrapInstance> _allBases = Lists.newList();

	private Timer _timer = new Timer();

	private static LsimulatorWorldTraps _instance;

	private LsimulatorWorldTraps() {
		initialize();
	}

	public static LsimulatorWorldTraps getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorWorldTraps();
		}
		return _instance;
	}

	private void initialize() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;

		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();

			pstm = con.prepareStatement("SELECT * FROM spawnlist_trap");

			rs = pstm.executeQuery();

			while (rs.next()) {
				int trapId = rs.getInt("trapId");
				LsimulatorTrap trapTemp = TrapTable.getInstance().getTemplate(trapId);
				LsimulatorLocation loc = new LsimulatorLocation();
				loc.setMap(rs.getInt("mapId"));
				loc.setX(rs.getInt("locX"));
				loc.setY(rs.getInt("locY"));
				Point rndPt = new Point();
				rndPt.setX(rs.getInt("locRndX"));
				rndPt.setY(rs.getInt("locRndY"));
				int count = rs.getInt("count");
				int span = rs.getInt("span");

				for (int i = 0; i < count; i++) {
					LsimulatorTrapInstance trap = new LsimulatorTrapInstance(IdFactory.getInstance().nextId(), trapTemp, loc, rndPt, span);
					LsimulatorWorld.getInstance().addVisibleObject(trap);
					_allTraps.add(trap);
				}
				LsimulatorTrapInstance base = new LsimulatorTrapInstance(IdFactory.getInstance().nextId(), loc);
				LsimulatorWorld.getInstance().addVisibleObject(base);
				_allBases.add(base);
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

	public static void reloadTraps() {
		TrapTable.reload();
		LsimulatorWorldTraps oldInstance = _instance;
		_instance = new LsimulatorWorldTraps();
		oldInstance.resetTimer();
		removeTraps(oldInstance._allTraps);
		removeTraps(oldInstance._allBases);
	}

	private static void removeTraps(List<LsimulatorTrapInstance> traps) {
		for (LsimulatorTrapInstance trap : traps) {
			trap.disableTrap();
			LsimulatorWorld.getInstance().removeVisibleObject(trap);
		}
	}

	private void resetTimer() {
		synchronized (this) {
			_timer.cancel();
			_timer = new Timer();
		}
	}

	private void disableTrap(LsimulatorTrapInstance trap) {
		trap.disableTrap();

		synchronized (this) {
			_timer.schedule(new TrapSpawnTimer(trap), trap.getSpan());
		}
	}

	public void resetAllTraps() {
		for (LsimulatorTrapInstance trap : _allTraps) {
			trap.resetLocation();
			trap.enableTrap();
		}
	}

	public void onPlayerMoved(LsimulatorPcInstance player) {
		LsimulatorLocation loc = player.getLocation();

		for (LsimulatorTrapInstance trap : _allTraps) {
			if (trap.isEnable() && loc.equals(trap.getLocation())) {
				trap.onTrod(player);
				disableTrap(trap);
			}
		}
	}

	public void onDetection(LsimulatorPcInstance caster) {
		LsimulatorLocation loc = caster.getLocation();

		for (LsimulatorTrapInstance trap : _allTraps) {
			if (trap.isEnable() && loc.isInScreen(trap.getLocation())) {
				trap.onDetection(caster);
				disableTrap(trap);
			}
		}
	}

	private class TrapSpawnTimer extends TimerTask {
		private final LsimulatorTrapInstance _targetTrap;

		public TrapSpawnTimer(LsimulatorTrapInstance trap) {
			_targetTrap = trap;
		}

		@Override
		public void run() {
			_targetTrap.resetLocation();
			_targetTrap.enableTrap();
		}
	}
}
