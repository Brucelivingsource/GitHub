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
package Lsimulator.server.server.model;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.Instance.DoorInstance;
import Lsimulator.server.server.model.Instance.MonsterInstance;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.gametime.LsimulatorGameTime;
import Lsimulator.server.server.model.gametime.LsimulatorGameTimeAdapter;
import Lsimulator.server.server.model.gametime.LsimulatorGameTimeClock;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.templates.LsimulatorSpawnTime;
import Lsimulator.server.server.types.Point;
import Lsimulator.server.server.utils.Random;
import Lsimulator.server.server.utils.collections.Lists;
import Lsimulator.server.server.utils.collections.Maps;

public class LsimulatorSpawn extends LsimulatorGameTimeAdapter {
	private static Logger _log = Logger.getLogger(LsimulatorSpawn.class.getName());

	private final LsimulatorNpc _template;

	private int _id; // just to find this in the spawn table

	private String _location;

	private int _maximumCount;

	private int _npcid;

	private int _groupId;

	private int _locx;

	private int _locy;

	private int Randomx;

	private int Randomy;

	private int _locx1;

	private int _locy1;

	private int _locx2;

	private int _locy2;

	private int _heading;

	private int _minRespawnDelay;

	private int _maxRespawnDelay;

	private short _mapid;

	private boolean _respaenScreen;

	private int _movementDistance;

	private boolean _rest;

	private int _spawnType;

	private int _delayInterval;

	private LsimulatorSpawnTime _time;

	private Map<Integer, Point> _homePoint = null; // initでspawnした個々のオブジェクトのホームポイント

	private List<NpcInstance> _mobs = Lists.newList();

	private String _name;

	private class SpawnTask implements Runnable {
		private int _spawnNumber;

		private int _objectId;

		private SpawnTask(int spawnNumber, int objectId) {
			_spawnNumber = spawnNumber;
			_objectId = objectId;
		}

		@Override
		public void run() {
			doSpawn(_spawnNumber, _objectId);
		}
	}

	public LsimulatorSpawn(LsimulatorNpc mobTemplate) {
		_template = mobTemplate;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public short getMapId() {
		return _mapid;
	}

	public void setMapId(short _mapid) {
		this._mapid = _mapid;
	}

	public boolean isRespawnScreen() {
		return _respaenScreen;
	}

	public void setRespawnScreen(boolean flag) {
		_respaenScreen = flag;
	}

	public int getMovementDistance() {
		return _movementDistance;
	}

	public void setMovementDistance(int i) {
		_movementDistance = i;
	}

	public int getAmount() {
		return _maximumCount;
	}

	public int getGroupId() {
		return _groupId;
	}

	public int getId() {
		return _id;
	}

	public String getLocation() {
		return _location;
	}

	public int getLocX() {
		return _locx;
	}

	public int getLocY() {
		return _locy;
	}

	public int getNpcId() {
		return _npcid;
	}

	public int getHeading() {
		return _heading;
	}

	public int getRandomx() {
		return Randomx;
	}

	public int getRandomy() {
		return Randomy;
	}

	public int getLocX1() {
		return _locx1;
	}

	public int getLocY1() {
		return _locy1;
	}

	public int getLocX2() {
		return _locx2;
	}

	public int getLocY2() {
		return _locy2;
	}

	public int getMinRespawnDelay() {
		return _minRespawnDelay;
	}

	public int getMaxRespawnDelay() {
		return _maxRespawnDelay;
	}

	public void setAmount(int amount) {
		_maximumCount = amount;
	}

	public void setId(int id) {
		_id = id;
	}

	public void setGroupId(int i) {
		_groupId = i;
	}

	public void setLocation(String location) {
		_location = location;
	}

	public void setLocX(int locx) {
		_locx = locx;
	}

	public void setLocY(int locy) {
		_locy = locy;
	}

	public void setNpcid(int npcid) {
		_npcid = npcid;
	}

	public void setHeading(int heading) {
		_heading = heading;
	}

	public void setRandomx(int randomx) {
		Randomx = randomx;
	}

	public void setRandomy(int randomy) {
		Randomy = randomy;
	}

	public void setLocX1(int locx1) {
		_locx1 = locx1;
	}

	public void setLocY1(int locy1) {
		_locy1 = locy1;
	}

	public void setLocX2(int locx2) {
		_locx2 = locx2;
	}

	public void setLocY2(int locy2) {
		_locy2 = locy2;
	}

	public void setMinRespawnDelay(int i) {
		_minRespawnDelay = i;
	}

	public void setMaxRespawnDelay(int i) {
		_maxRespawnDelay = i;
	}

	private int calcRespawnDelay() {
		int respawnDelay = _minRespawnDelay * 1000;
		if (_delayInterval > 0) {
			respawnDelay += Random.nextInt(_delayInterval) * 1000;
		}
		LsimulatorGameTime currentTime = LsimulatorGameTimeClock.getInstance().currentTime();
		if ((_time != null) && !_time.getTimePeriod().includes(currentTime)) { // 指定時間外なら指定時間までの時間を足す
			long diff = (_time.getTimeStart().getTime() - currentTime.toTime().getTime());
			if (diff < 0) {
				diff += 24 * 1000L * 3600L;
			}
			diff /= 6; // real time to game time
			respawnDelay = (int) diff;
		}
		return respawnDelay;
	}

	/**
	 * SpawnTaskを起動する。
	 * 
	 * @param spawnNumber
	 *            LsimulatorSpawnで管理されている番号。ホームポイントが無ければ何を指定しても良い。
	 */
	public void executeSpawnTask(int spawnNumber, int objectId) {
		SpawnTask task = new SpawnTask(spawnNumber, objectId);
		GeneralThreadPool.getInstance().schedule(task, calcRespawnDelay());
	}

	private boolean _initSpawn = false;

	private boolean _spawnHomePoint;

	public void init() {
		if ((_time != null) && _time.isDeleteAtEndTime()) {
			// 時間外削除が指定されているなら、時間経過の通知を受ける。
			LsimulatorGameTimeClock.getInstance().addListener(this);
		}
		_delayInterval = _maxRespawnDelay - _minRespawnDelay;
		_initSpawn = true;
		// ホームポイントを持たせるか
		if (Config.SPAWN_HOME_POINT && (Config.SPAWN_HOME_POINT_COUNT <= getAmount()) && (Config.SPAWN_HOME_POINT_DELAY >= getMinRespawnDelay())
				&& isAreaSpawn()) {
			_spawnHomePoint = true;
			_homePoint = Maps.newMap();
		}

		int spawnNum = 0;
		while (spawnNum < _maximumCount) {
			// spawnNumは1～maxmumCountまで
			doSpawn(++spawnNum);
		}
		_initSpawn = false;
	}

	/**
	 * ホームポイントがある場合は、spawnNumberを基にspawnする。 それ以外の場合は、spawnNumberは未使用。
	 */
	protected void doSpawn(int spawnNumber) { // 初期配置
		// 指定時間外であれば、次spawnを予約して終わる。
		if ((_time != null) && !_time.getTimePeriod().includes(LsimulatorGameTimeClock.getInstance().currentTime())) {
			executeSpawnTask(spawnNumber, 0);
			return;
		}
		doSpawn(spawnNumber, 0);
	}

	protected void doSpawn(int spawnNumber, int objectId) { // 再出現
		NpcInstance mob = null;
		try {
			int newlocx = getLocX();
			int newlocy = getLocY();
			int tryCount = 0;

			mob = NpcTable.getInstance().newNpcInstance(_template);
			synchronized (_mobs) {
				_mobs.add(mob);
			}
			if (objectId == 0) {
				mob.setId(IdFactory.getInstance().nextId());
			}
			else {
				mob.setId(objectId); // オブジェクトID再利用
			}

			if ((0 <= getHeading()) && (getHeading() <= 7)) {
				mob.setHeading(getHeading());
			}
			else {
				// heading値が正しくない
				mob.setHeading(5);
			}

			int npcId = mob.getNpcTemplate().get_npcId();
			if ((npcId == 45488) && (getMapId() == 9)) { // 卡士伯
				mob.setMap((short) (getMapId() + Random.nextInt(2)));
			}
			else if ((npcId == 45601) && (getMapId() == 11)) { // 死亡騎士
				mob.setMap((short) (getMapId() + Random.nextInt(3)));
			}
			else if ((npcId == 81322) && (getMapId() == 25)) { // 黑騎士副隊長
				mob.setMap((short) (getMapId() + Random.nextInt(2)));
			}
			else {
				mob.setMap(getMapId());
			}
			mob.setMovementDistance(getMovementDistance());
			mob.setRest(isRest());
			while (tryCount <= 50) {
				switch (getSpawnType()) {
					case SPAWN_TYPE_PC_AROUND: // PC周辺に湧くタイプ
						if (!_initSpawn) { // 初期配置では無条件に通常spawn
							List<PcInstance> players = Lists.newList();
							for (PcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
								if (getMapId() == pc.getMapId()) {
									players.add(pc);
								}
							}
							if (players.size() > 0) {
								PcInstance pc = players.get(Random.nextInt(players.size()));
								LsimulatorLocation loc = pc.getLocation().randomLocation(PC_AROUND_DISTANCE, false);
								newlocx = loc.getX();
								newlocy = loc.getY();
								break;
							}
						}
						// フロアにPCがいなければ通常の出現方法
					default:
						if (isAreaSpawn()) { // 座標が範囲指定されている場合
							Point pt = null;
							if (_spawnHomePoint && (null != (pt = _homePoint.get(spawnNumber)))) { // ホームポイントを元に再出現させる場合
								LsimulatorLocation loc = new LsimulatorLocation(pt, getMapId()).randomLocation(Config.SPAWN_HOME_POINT_RANGE, false);
								newlocx = loc.getX();
								newlocy = loc.getY();
							}
							else {
								int rangeX = getLocX2() - getLocX1();
								int rangeY = getLocY2() - getLocY1();
								newlocx = Random.nextInt(rangeX) + getLocX1();
								newlocy = Random.nextInt(rangeY) + getLocY1();
							}
							if (tryCount > 49) { // 出現位置が決まらない時はlocx,locyの値
								newlocx = getLocX();
								newlocy = getLocY();
							}
						}
						else if (isRandomSpawn()) { // 座標のランダム値が指定されている場合
							newlocx = (getLocX() + (Random.nextInt(getRandomx()) - Random.nextInt(getRandomx())));
							newlocy = (getLocY() + (Random.nextInt(getRandomy()) - Random.nextInt(getRandomy())));
						}
						else { // どちらも指定されていない場合
							newlocx = getLocX();
							newlocy = getLocY();
						}
				}
				mob.setX(newlocx);
				mob.setHomeX(newlocx);
				mob.setY(newlocy);
				mob.setHomeY(newlocy);

				if (mob.getMap().isInMap(mob.getLocation()) && mob.getMap().isPassable(mob.getLocation())) {
					if (mob instanceof MonsterInstance) {
						if (isRespawnScreen()) {
							break;
						}
						MonsterInstance mobtemp = (MonsterInstance) mob;
						if (LsimulatorWorld.getInstance().getVisiblePlayer(mobtemp).isEmpty()) {
							break;
						}
						// 画面内にPCが居て出現できない場合は、3秒後にスケジューリングしてやり直し
						SpawnTask task = new SpawnTask(spawnNumber, mob.getId());
						GeneralThreadPool.getInstance().schedule(task, 3000L);
						return;
					}
				}
				tryCount++;
			}
			if (mob instanceof MonsterInstance) {
				((MonsterInstance) mob).initHide();
			}

			mob.setSpawn(this);
			mob.setreSpawn(true);
			mob.setSpawnNumber(spawnNumber); // LsimulatorSpawnでの管理番号(ホームポイントに使用)
			if (_initSpawn && _spawnHomePoint) { // 初期配置でホームポイントを設定
				Point pt = new Point(mob.getX(), mob.getY());
				_homePoint.put(spawnNumber, pt); // ここで保存したpointを再出現時に使う
			}

			if (mob instanceof MonsterInstance) {
				if (mob.getMapId() == 666) {
					((MonsterInstance) mob).set_storeDroped(true);
				}
			}
			if ((npcId == 45573) && (mob.getMapId() == 2)) { // バフォメット
				for (PcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
					if (pc.getMapId() == 2) {
						LsimulatorTeleport.teleport(pc, 32664, 32797, (short) 2, 0, true);
					}
				}
			}

			if (((npcId == 46142) && (mob.getMapId() == 73)) || ((npcId == 46141) && (mob.getMapId() == 74))) {
				for (PcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
					if ((pc.getMapId() >= 72) && (pc.getMapId() <= 74)) {
						LsimulatorTeleport.teleport(pc, 32840, 32833, (short) 72, pc.getHeading(), true);
					}
				}
			}
			if ((npcId == 81341) && ((mob.getMapId() == 2000) || (mob.getMapId() == 2001) || (mob.getMapId() == 2002) || (mob.getMapId() == 2003))) { // 再生之祭壇
				for (PcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
					if ((pc.getMapId() >= 2000) && (pc.getMapId() <= 2003)) {
						LsimulatorTeleport.teleport(pc, 32933, 32988, (short) 410, 5, true);
					}
				}
			}

			doCrystalCave(npcId);

			LsimulatorWorld.getInstance().storeObject(mob);
			LsimulatorWorld.getInstance().addVisibleObject(mob);

			if (mob instanceof MonsterInstance) {
				MonsterInstance mobtemp = (MonsterInstance) mob;
				if (!_initSpawn && (mobtemp.getHiddenStatus() == 0)) {
					mobtemp.onNpcAI(); // モンスターのＡＩを開始
				}
			}
			if (getGroupId() != 0) {
				LsimulatorMobGroupSpawn.getInstance().doSpawn(mob, getGroupId(), isRespawnScreen(), _initSpawn);
			}
			mob.turnOnOffLight();
			mob.startChat(NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public void setRest(boolean flag) {
		_rest = flag;
	}

	public boolean isRest() {
		return _rest;
	}

	private static final int SPAWN_TYPE_PC_AROUND = 1;

	private static final int PC_AROUND_DISTANCE = 30;

	private int getSpawnType() {
		return _spawnType;
	}

	public void setSpawnType(int type) {
		_spawnType = type;
	}

	private boolean isAreaSpawn() {
		return (getLocX1() != 0) && (getLocY1() != 0) && (getLocX2() != 0) && (getLocY2() != 0);
	}

	private boolean isRandomSpawn() {
		return (getRandomx() != 0) || (getRandomy() != 0);
	}

	public LsimulatorSpawnTime getTime() {
		return _time;
	}

	public void setTime(LsimulatorSpawnTime time) {
		_time = time;
	}

	@Override
	public void onMinuteChanged(LsimulatorGameTime time) {
		if (_time.getTimePeriod().includes(time)) {
			return;
		}
		synchronized (_mobs) {
			if (_mobs.isEmpty()) {
				return;
			}
			// 指定時間外になっていれば削除
			for (NpcInstance mob : _mobs) {
				mob.setCurrentHpDirect(0);
				mob.setDead(true);
				mob.setStatus(ActionCodes.ACTION_Die);
				mob.deleteMe();
			}
			_mobs.clear();
		}
	}

	public static void doCrystalCave(int npcId) {
		int[] npcId2 =
		{ 46143, 46144, 46145, 46146, 46147, 46148, 46149, 46150, 46151, 46152 };
		int[] doorId =
		{ 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5008, 5009, 5010 };

		for (int i = 0; i < npcId2.length; i++) {
			if (npcId == npcId2[i]) {
				closeDoorInCrystalCave(doorId[i]);
			}
		}
	}

	private static void closeDoorInCrystalCave(int doorId) {
		for (LsimulatorObject object : LsimulatorWorld.getInstance().getObject()) {
			if (object instanceof DoorInstance) {
				DoorInstance door = (DoorInstance) object;
				if (door.getDoorId() == doorId) {
					door.close();
				}
			}
		}
	}
}
