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
package Lsimulator.server.server.utils;

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.MEDITATION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.WIND_SHACKLE;

import java.util.HashSet;

import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorDragonSlayer;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorDollInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.model.Instance.LsimulatorSummonInstance;
import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;
import Lsimulator.server.server.serverpackets.S_CharVisualUpdate;
import Lsimulator.server.server.serverpackets.S_DollPack;
import Lsimulator.server.server.serverpackets.S_MapID;
import Lsimulator.server.server.serverpackets.S_OtherCharPacks;
import Lsimulator.server.server.serverpackets.S_OwnCharPack;
import Lsimulator.server.server.serverpackets.S_PetPack;
import Lsimulator.server.server.serverpackets.S_SkillIconWindShackle;
import Lsimulator.server.server.serverpackets.S_SummonPack;

// Referenced classes of package Lsimulator.server.server.utils:
// FaceToFace

public class Teleportation {
	private Teleportation() {
	}

	public static void actionTeleportation(final LsimulatorPcInstance pc) {
		if (pc.isDead() || pc.isTeleport()) {
			return;
		}

		int x = pc.getTeleportX();
		int y = pc.getTeleportY();
		short mapId = pc.getTeleportMapId();
		final int head = pc.getTeleportHeading();

		// テレポート先が不正であれば元の座標へ(GMは除く)
		final LsimulatorMap map = LsimulatorWorldMap.getInstance().getMap(mapId);

		if (!map.isInMap(x, y) && !pc.isGm()) {
			x = pc.getX();
			y = pc.getY();
			mapId = pc.getMapId();
		}

		pc.setTeleport(true);

		final LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
		if (clan != null) {
			if (clan.getWarehouseUsingChar() == pc.getId()) { // 自キャラがクラン倉庫使用中
				clan.setWarehouseUsingChar(0); // クラン倉庫のロックを解除
			}
		}

		LsimulatorWorld.getInstance().moveVisibleObject(pc, mapId);
		pc.setLocation(x, y, mapId);
		pc.setHeading(head);
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));

		if (pc.isReserveGhost()) { // ゴースト状態解除
			pc.endGhost();
		}
		if (pc.isGhost() || pc.isGmInvis()) {}
		else if (pc.isInvisble()) {
			pc.broadcastPacketForFindInvis(new S_OtherCharPacks(pc, true), true);
		}
		else {
			pc.broadcastPacket(new S_OtherCharPacks(pc));
		}
		pc.sendPackets(new S_OwnCharPack(pc));

		pc.removeAllKnownObjects();
		pc.sendVisualEffectAtTeleport(); // クラウン、毒、水中等の視覚効果を表示
		pc.updateObject();
		// spr番号6310, 5641の変身中にテレポートするとテレポート後に移動できなくなる
		// 武器を着脱すると移動できるようになるため、S_CharVisualUpdateを送信する
		pc.sendPackets(new S_CharVisualUpdate(pc));

		pc.killSkillEffectTimer(MEDITATION);
		pc.setCallClanId(0); // コールクランを唱えた後に移動すると召喚無効

		/*
		 * subjects ペットとサモンのテレポート先画面内へ居たプレイヤー。
		 * 各ペット毎にUpdateObjectを行う方がコード上ではスマートだが、
		 * ネットワーク負荷が大きくなる為、一旦Setへ格納して最後にまとめてUpdateObjectする。
		 */
		final HashSet<LsimulatorPcInstance> subjects = new HashSet<LsimulatorPcInstance>();
		subjects.add(pc);

		if (!pc.isGhost()) {
			if (pc.getMap().isTakePets()) {
				// ペットとサモンも一緒に移動させる。
				for (final LsimulatorNpcInstance petNpc : pc.getPetList().values()) {
					// テレポート先の設定
					final LsimulatorLocation loc = pc.getLocation().randomLocation(3, false);
					int nx = loc.getX();
					int ny = loc.getY();
					if ((pc.getMapId() == 5125) || (pc.getMapId() == 5131) || (pc.getMapId() == 5132) || (pc.getMapId() == 5133)
							|| (pc.getMapId() == 5134)) { // ペットマッチ会場
						nx = 32799 + Random.nextInt(5) - 3;
						ny = 32864 + Random.nextInt(5) - 3;
					}
					teleport(petNpc, nx, ny, mapId, head);
					if (petNpc instanceof LsimulatorSummonInstance) { // サモンモンスター
						final LsimulatorSummonInstance summon = (LsimulatorSummonInstance) petNpc;
						pc.sendPackets(new S_SummonPack(summon, pc));
					}
					else if (petNpc instanceof LsimulatorPetInstance) { // ペット
						final LsimulatorPetInstance pet = (LsimulatorPetInstance) petNpc;
						pc.sendPackets(new S_PetPack(pet, pc));
					}

					for (final LsimulatorPcInstance visiblePc : LsimulatorWorld.getInstance().getVisiblePlayer(petNpc)) {
						// テレポート元と先に同じPCが居た場合、正しく更新されない為、一度removeする。
						visiblePc.removeKnownObject(petNpc);
						subjects.add(visiblePc);
					}
				}

				// マジックドールも一緒に移動させる。
				for (final LsimulatorDollInstance doll : pc.getDollList().values()) {
					// テレポート先の設定
					final LsimulatorLocation loc = pc.getLocation().randomLocation(3, false);
					final int nx = loc.getX();
					final int ny = loc.getY();

					teleport(doll, nx, ny, mapId, head);
					pc.sendPackets(new S_DollPack(doll));

					for (final LsimulatorPcInstance visiblePc : LsimulatorWorld.getInstance().getVisiblePlayer(doll)) {
						// テレポート元と先に同じPCが居た場合、正しく更新されない為、一度removeする。
						visiblePc.removeKnownObject(doll);
						subjects.add(visiblePc);
					}
				}
			}
			else {
				for (final LsimulatorDollInstance doll : pc.getDollList().values()) {
					// テレポート先の設定
					final LsimulatorLocation loc = pc.getLocation().randomLocation(3, false);
					final int nx = loc.getX();
					final int ny = loc.getY();

					teleport(doll, nx, ny, mapId, head);
					pc.sendPackets(new S_DollPack(doll));

					for (final LsimulatorPcInstance visiblePc : LsimulatorWorld.getInstance().getVisiblePlayer(doll)) {
						// テレポート元と先に同じPCが居た場合、正しく更新されない為、一度removeする。
						visiblePc.removeKnownObject(doll);
						subjects.add(visiblePc);
					}
				}
			}
		}

		for (final LsimulatorPcInstance updatePc : subjects) {
			updatePc.updateObject();
		}

		pc.setTeleport(false);

		if (pc.hasSkillEffect(WIND_SHACKLE)) {
			pc.sendPackets(new S_SkillIconWindShackle(pc.getId(), pc.getSkillEffectTimeSec(WIND_SHACKLE)));
		}

		// 副本編號與副本地圖不符
		if (pc.getPortalNumber() != -1
				&& (pc.getMapId() !=  (1005 + pc.getPortalNumber()))) {
			LsimulatorDragonSlayer.getInstance().removePlayer(pc, pc.getPortalNumber());
			pc.setPortalNumber(-1);
		}
		// 離開旅館地圖，旅館鑰匙歸零
		if (pc.getMapId() <= 10000 && pc.getInnKeyId() != 0) {
			pc.setInnKeyId(0);
		}
	}

	private static void teleport(LsimulatorNpcInstance npc, int x, int y, short map, int head) {
		LsimulatorWorld.getInstance().moveVisibleObject(npc, map);
		LsimulatorWorldMap.getInstance().getMap(npc.getMapId()).setPassable(npc.getX(), npc.getY(), true);
		npc.setX(x);
		npc.setY(y);
		npc.setMap(map);
		npc.setHeading(head);
		LsimulatorWorldMap.getInstance().getMap(npc.getMapId()).setPassable(npc.getX(), npc.getY(), false);
	}

}
