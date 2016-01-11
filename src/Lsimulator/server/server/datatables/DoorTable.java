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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.DoorInstance;
import Lsimulator.server.server.templates.LsimulatorDoorGfx;
import Lsimulator.server.server.templates.LsimulatorDoorSpawn;
import Lsimulator.server.server.utils.collections.Lists;
import Lsimulator.server.server.utils.collections.Maps;

public class DoorTable {
	private static Logger _log = Logger.getLogger(DoorTable.class.getName());
	private static DoorTable _instance;

	private final Map<LsimulatorLocation, DoorInstance> _doors = Maps.newConcurrentHashMap();
	private final Map<LsimulatorLocation, DoorInstance> _doorDirections = Maps.newConcurrentHashMap();

	public static void initialize() {
		_instance = new DoorTable();
	}

	public static DoorTable getInstance() {
		return _instance;
	}

	private DoorTable() {
		loadDoors();
	}

	private void loadDoors() {
		for (LsimulatorDoorSpawn spawn : LsimulatorDoorSpawn.all()) {
			LsimulatorLocation loc = spawn.getLocation();
			if (_doors.containsKey(loc)) {
				_log.log(Level.WARNING, String.format("Duplicate door location: id = %d", spawn.getId()));
				continue;
			}
			createDoor(spawn.getId(), spawn.getGfx(), loc, spawn.getHp(), spawn.getKeeper(), spawn.isOpening());
		}
	}

	private void putDirections(DoorInstance door) {
		for (LsimulatorLocation key : makeDirectionsKeys(door)) {
			_doorDirections.put(key, door);
		}
	}

	private void removeDirections(DoorInstance door) {
		for (LsimulatorLocation key : makeDirectionsKeys(door)) {
			_doorDirections.remove(key);
		}
	}

	private List<LsimulatorLocation> makeDirectionsKeys(DoorInstance door) {
		List<LsimulatorLocation> keys = Lists.newArrayList();
		int left = door.getLeftEdgeLocation();
		int right = door.getRightEdgeLocation();
		if (door.getDirection() == 0) {
			for (int x = left; x <= right; x++) {
				keys.add(new LsimulatorLocation(x, door.getY(), door.getMapId()));
			}
		} else {
			for (int y = left; y <= right; y++) {
				keys.add(new LsimulatorLocation(door.getX(), y, door.getMapId()));
			}
		}
		return keys;
	}

	public DoorInstance createDoor(int doorId, LsimulatorDoorGfx gfx, LsimulatorLocation loc,
			int hp, int keeper, boolean isOpening) {
		if (_doors.containsKey(loc)) {
			return null;
		}
		DoorInstance door = new DoorInstance(doorId, gfx, loc, hp, keeper, isOpening);

		door.setId(IdFactory.getInstance().nextId());

		LsimulatorWorld.getInstance().storeObject(door);
		LsimulatorWorld.getInstance().addVisibleObject(door);

		_doors.put(door.getLocation(), door);
		putDirections(door);
		return door;
	}

	public void deleteDoorByLocation(LsimulatorLocation loc) {
		DoorInstance door = _doors.remove(loc);
		if (door != null) {
			removeDirections(door);
			door.deleteMe();
		}
	}

	public int getDoorDirection(LsimulatorLocation loc) {
		DoorInstance door = _doorDirections.get(loc);
		if (door == null || door.getOpenStatus() == ActionCodes.ACTION_Open) {
			return -1;
		}
		return door.getDirection();
	}

	public DoorInstance findByDoorId(int doorId) {
		for (DoorInstance door : _doors.values()) {
			if (door.getDoorId() == doorId) {
				return door;
			}
		}
		return null;
	}

	public DoorInstance[] getDoorList() {
		return _doors.values().toArray(new DoorInstance[_doors.size()]);
	}
}
