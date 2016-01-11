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

import Lsimulator.server.Config;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.serverpackets.S_SkillSound;
import Lsimulator.server.server.serverpackets.S_Teleport;
import Lsimulator.server.server.utils.Teleportation;

public class LsimulatorTeleport {

	// テレポートスキルの種類
	public static final int TELEPORT = 0;

	public static final int CHANGE_POSITION = 1;

	public static final int ADVANCED_MASS_TELEPORT = 2;

	public static final int CALL_CLAN = 3;

	// 順番にteleport(白), change position e(青), ad mass teleport e(赤), call clan(緑)
	public static final int[] EFFECT_SPR =  { 169, 2235, 2236, 2281 };

	public static final int[] EFFECT_TIME = { 280, 440, 440, 1120 };

	private LsimulatorTeleport() {
	}

	public static void teleport(PcInstance pc, LsimulatorLocation loc, int head, boolean effectable) {
		teleport(pc, loc.getX(), loc.getY(), (short) loc.getMapId(), head, effectable, TELEPORT);
	}

	public static void teleport(PcInstance pc, LsimulatorLocation loc, int head, boolean effectable, int skillType) {
		teleport(pc, loc.getX(), loc.getY(), (short) loc.getMapId(), head, effectable, skillType);
	}

	public static void teleport(PcInstance pc, int x, int y, short mapid, int head, boolean effectable) {
		teleport(pc, x, y, mapid, head, effectable, TELEPORT);
	}

	public static void teleport(PcInstance pc, int x, int y, short mapId, int head, boolean effectable, int skillType) {
		// 瞬移, 取消交易
		if (pc.getTradeID() != 0) {
			LsimulatorTrade trade = new LsimulatorTrade();
	        trade.TradeCancel(pc);
		}
		
		//pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK, false));

		// エフェクトの表示
		if (effectable && ((skillType >= 0) && (skillType <= EFFECT_SPR.length))) {
			S_SkillSound packet = new S_SkillSound(pc.getId(), EFFECT_SPR[skillType]);
			pc.sendPackets(packet);
			pc.broadcastPacket(packet);

			// テレポート以外のsprはキャラが消えないので見た目上送っておきたいが
			// 移動中だった場合クラ落ちすることがある
			// if (skillType != TELEPORT) {
			// pc.sendPackets(new S_DeleteNewObject(pc));
			// pc.broadcastPacket(new S_DeleteObjectFromScreen(pc));
			// }

			try {
				Thread.sleep((int) (EFFECT_TIME[skillType] * 0.7));
			}
			catch (Exception e) {}
		}

		pc.setTeleportX(x);
		pc.setTeleportY(y);
		pc.setTeleportMapId(mapId);
		pc.setTeleportHeading(head);
		if (Config.SEND_PACKET_BEFORE_TELEPORT) {
			pc.sendPackets(new S_Teleport(pc));
		}
		else {
			Teleportation.actionTeleportation(pc);
		}
	}

	/*
	 * targetキャラクターのdistanceで指定したマス分前にテレポートする。指定されたマスがマップでない場合何もしない。
	 */
	public static void teleportToTargetFront(LsimulatorCharacter cha, LsimulatorCharacter target, int distance) {
		int locX = target.getX();
		int locY = target.getY();
		int heading = target.getHeading();
		LsimulatorMap map = target.getMap();
		short mapId = target.getMapId();

		// ターゲットの向きからテレポート先の座標を決める。
		switch (heading) {
			case 1:
				locX += distance;
				locY -= distance;
				break;

			case 2:
				locX += distance;
				break;

			case 3:
				locX += distance;
				locY += distance;
				break;

			case 4:
				locY += distance;
				break;

			case 5:
				locX -= distance;
				locY += distance;
				break;

			case 6:
				locX -= distance;
				break;

			case 7:
				locX -= distance;
				locY -= distance;
				break;

			case 0:
				locY -= distance;
				break;

			default:
				break;

		}

		if (map.isPassable(locX, locY)) {
			if (cha instanceof PcInstance) {
				teleport((PcInstance) cha, locX, locY, mapId, cha.getHeading(), true);
			}
			else if (cha instanceof NpcInstance) {
				((NpcInstance) cha).teleport(locX, locY, cha.getHeading());
			}
		}
	}

	public static void randomTeleport(PcInstance pc, boolean effectable) {
		// まだ本サーバのランテレ処理と違うところが結構あるような・・・
		LsimulatorLocation newLocation = pc.getLocation().randomLocation(200, true);
		int newX = newLocation.getX();
		int newY = newLocation.getY();
		short mapId = (short) newLocation.getMapId();

		LsimulatorTeleport.teleport(pc, newX, newY, mapId, 5, effectable);
	}
}
