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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FIRE_WALL;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.model.Instance.EffectInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.templates.LsimulatorNpc;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorEffectSpawn

public class LsimulatorEffectSpawn {

	private static final Logger _log = Logger.getLogger(LsimulatorEffectSpawn.class.getName());

	private static LsimulatorEffectSpawn _instance;

	private Constructor<?> _constructor;

	private LsimulatorEffectSpawn() {
	}

	public static LsimulatorEffectSpawn getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorEffectSpawn();
		}
		return _instance;
	}

	/**
	 * エフェクトオブジェクトを生成し設置する
	 * 
	 * @param npcId
	 *            エフェクトNPCのテンプレートID
	 * @param time
	 *            存在時間(ms)
	 * @param locX
	 *            設置する座標X
	 * @param locY
	 *            設置する座標Y
	 * @param mapId
	 *            設置するマップのID
	 * @return 生成されたエフェクトオブジェクト
	 */
	public EffectInstance spawnEffect(int npcId, int time, int locX, int locY, short mapId) {
		return spawnEffect(npcId, time, locX, locY, mapId, null, 0);
	}

	public EffectInstance spawnEffect(int npcId, int time, int locX, int locY, short mapId, PcInstance user, int skiiId) {
		LsimulatorNpc template = NpcTable.getInstance().getTemplate(npcId);
		EffectInstance effect = null;

		if (template == null) {
			return null;
		}

		String className = (new StringBuilder()).append("Lsimulator.server.server.model.Instance.").append(template.getImpl()).append("Instance").toString();

		try {
			_constructor = Class.forName(className).getConstructors()[0];
			Object obj[] =
			{ template };
			effect = (EffectInstance) _constructor.newInstance(obj);

			effect.setId(IdFactory.getInstance().nextId());
			effect.setGfxId(template.get_gfxid());
			effect.setX(locX);
			effect.setY(locY);
			effect.setHomeX(locX);
			effect.setHomeY(locY);
			effect.setHeading(0);
			effect.setMap(mapId);
			effect.setUser(user);
			effect.setSkillId(skiiId);
			LsimulatorWorld.getInstance().storeObject(effect);
			LsimulatorWorld.getInstance().addVisibleObject(effect);

			for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(effect)) {
				effect.addKnownObject(pc);
				pc.addKnownObject(effect);
				pc.sendPackets(new S_NPCPack(effect));
				pc.broadcastPacket(new S_NPCPack(effect));
			}
			LsimulatorNpcDeleteTimer timer = new LsimulatorNpcDeleteTimer(effect, time);
			timer.begin();
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		return effect;
	}

	public void doSpawnFireWall(LsimulatorCharacter cha, int targetX, int targetY) {
		LsimulatorNpc firewall = NpcTable.getInstance().getTemplate(81157); // ファイアーウォール
		int duration = SkillsTable.getInstance().getTemplate(FIRE_WALL).getBuffDuration();

		if (firewall == null) {
			throw new NullPointerException("FireWall data not found:npcid=81157");
		}

		LsimulatorCharacter base = cha;
		for (int i = 0; i < 8; i++) {
			int a = base.targetDirection(targetX, targetY);
			int x = base.getX();
			int y = base.getY();
			if (a == 1) {
				x++;
				y--;
			}
			else if (a == 2) {
				x++;
			}
			else if (a == 3) {
				x++;
				y++;
			}
			else if (a == 4) {
				y++;
			}
			else if (a == 5) {
				x--;
				y++;
			}
			else if (a == 6) {
				x--;
			}
			else if (a == 7) {
				x--;
				y--;
			}
			else if (a == 0) {
				y--;
			}
			if (!base.isAttackPosition(x, y, 1)) {
				x = base.getX();
				y = base.getY();
			}
			LsimulatorMap map = LsimulatorWorldMap.getInstance().getMap(cha.getMapId());
			if (!map.isArrowPassable(x, y, cha.getHeading())) {
				break;
			}

			EffectInstance effect = spawnEffect(81157, duration * 1000, x, y, cha.getMapId());
			if (effect == null) {
				break;
			}
			for (LsimulatorObject objects : LsimulatorWorld.getInstance().getVisibleObjects(effect, 0)) {
				if (objects instanceof EffectInstance) {
					EffectInstance npc = (EffectInstance) objects;
					if (npc.getNpcTemplate().get_npcId() == 81157) {
						npc.deleteMe();
					}
				}
			}
			if ((targetX == x) && (targetY == y)) {
				break;
			}
			base = effect;
		}

	}
}
