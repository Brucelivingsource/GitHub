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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.storage.TrapStorage;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.types.Point;
import Lsimulator.server.server.utils.collections.Lists;

public class LsimulatorMonsterTrap extends LsimulatorTrap {
	private static Logger _log = Logger.getLogger(LsimulatorMonsterTrap.class.getName());

	private final int _npcId;

	private final int _count;

	private LsimulatorNpc _npcTemp = null; // パフォーマンスのためにキャッシュ

	private Constructor<?> _constructor = null; // パフォーマンスのためにキャッシュ

	public LsimulatorMonsterTrap(TrapStorage storage) {
		super(storage);

		_npcId = storage.getInt("monsterNpcId");
		_count = storage.getInt("monsterCount");
	}

	private void addListIfPassable(List<Point> list, LsimulatorMap map, Point pt) {
		if (map.isPassable(pt)) {
			list.add(pt);
		}
	}

	private List<Point> getSpawnablePoints(LsimulatorLocation loc, int d) {
		List<Point> result = Lists.newList();
		LsimulatorMap m = loc.getMap();
		int x = loc.getX();
		int y = loc.getY();
		// locを中心に、1辺dタイルの正方形を描くPointリストを作る
		for (int i = 0; i < d; i++) {
			addListIfPassable(result, m, new Point(d - i + x, i + y));
			addListIfPassable(result, m, new Point(-(d - i) + x, -i + y));
			addListIfPassable(result, m, new Point(-i + x, d - i + y));
			addListIfPassable(result, m, new Point(i + x, -(d - i) + y));
		}
		return result;
	}

	private Constructor<?> getConstructor(LsimulatorNpc npc) throws ClassNotFoundException {
		return Class.forName("Lsimulator.server.server.model.Instance." + npc.getImpl() + "Instance").getConstructors()[0];
	}

	private NpcInstance createNpc() throws Exception {
		if (_npcTemp == null) {
			_npcTemp = NpcTable.getInstance().getTemplate(_npcId);
		}
		if (_constructor == null) {
			_constructor = getConstructor(_npcTemp);
		}

		return (NpcInstance) _constructor.newInstance(new Object[]
		{ _npcTemp });
	}

	private void spawn(LsimulatorLocation loc) throws Exception {
		NpcInstance npc = createNpc();
		npc.setId(IdFactory.getInstance().nextId());
		npc.getLocation().set(loc);
		npc.setHomeX(loc.getX());
		npc.setHomeY(loc.getY());
		LsimulatorWorld.getInstance().storeObject(npc);
		LsimulatorWorld.getInstance().addVisibleObject(npc);

		npc.onNpcAI();
		npc.turnOnOffLight();
		npc.startChat(NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
	}

	@Override
	public void onTrod(PcInstance trodFrom, LsimulatorObject trapObj) {
		sendEffect(trapObj);

		List<Point> points = getSpawnablePoints(trapObj.getLocation(), 5);

		// 沸ける場所が無ければ終了
		if (points.isEmpty()) {
			return;
		}

		try {
			int cnt = 0;
			while (true) {
				for (Point pt : points) {
					spawn(new LsimulatorLocation(pt, trapObj.getMap()));
					cnt++;
					if (_count <= cnt) {
						return;
					}
				}
			}
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
