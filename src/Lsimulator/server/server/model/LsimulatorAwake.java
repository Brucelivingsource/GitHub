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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AWAKEN_ANTHARAS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AWAKEN_FAFURION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AWAKEN_VALAKAS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.SHAPE_CHANGE;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ChangeShape;
import Lsimulator.server.server.serverpackets.S_HPUpdate;
import Lsimulator.server.server.serverpackets.S_OwnCharAttrDef;
import Lsimulator.server.server.serverpackets.S_OwnCharStatus2;
import Lsimulator.server.server.serverpackets.S_SPMR;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorCooking

public class LsimulatorAwake {
	private LsimulatorAwake() {
	}

	public static void start(PcInstance pc, int skillId) {
		if (skillId == pc.getAwakeSkillId()) { // 再次咏唱時解除覺醒狀態
			stop(pc);
		}
		else if (pc.getAwakeSkillId() != 0) { // 無法與其他覺醒狀態並存
			return;
		}
		else {
			if (skillId == AWAKEN_ANTHARAS) { // 覺醒：安塔瑞斯
				pc.addMaxHp(127);
				pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
				if (pc.isInParty()) { // 組隊中
					pc.getParty().updateMiniHP(pc);
				}
				pc.addAc(-12);
				pc.sendPackets(new S_OwnCharStatus2(pc, 0));
			}
			else if (skillId == AWAKEN_FAFURION) { // 覺醒：法力昂
				pc.addMr(30);
				pc.sendPackets(new S_SPMR(pc));
				pc.addWind(30);
				pc.addWater(30);
				pc.addFire(30);
				pc.addEarth(30);
				pc.sendPackets(new S_OwnCharAttrDef(pc));
			}
			else if (skillId == AWAKEN_VALAKAS) { // 覺醒：巴拉卡斯
				pc.addStr(5);
				pc.addCon(5);
				pc.addDex(5);
				pc.addCha(5);
				pc.addInt(5);
				pc.addWis(5);
				pc.sendPackets(new S_OwnCharStatus2(pc, 0));
			}
			pc.setAwakeSkillId(skillId);
			doPoly(pc);
			pc.startMpReductionByAwake();
		}
	}

	public static void stop(PcInstance pc) {
		int skillId = pc.getAwakeSkillId();
		if (skillId == AWAKEN_ANTHARAS) { // 覺醒：安塔瑞斯
			pc.addMaxHp(-127);
			pc.sendPackets(new S_HPUpdate(pc.getCurrentHp(), pc.getMaxHp()));
			if (pc.isInParty()) { // パーティー中
				pc.getParty().updateMiniHP(pc);
			}
			pc.addAc(12);
			pc.sendPackets(new S_OwnCharAttrDef(pc));
		}
		else if (skillId == AWAKEN_FAFURION) { // 覺醒：法力昂
			pc.addMr(-30);
			pc.addWind(-30);
			pc.addWater(-30);
			pc.addFire(-30);
			pc.addEarth(-30);
			pc.sendPackets(new S_SPMR(pc));
			pc.sendPackets(new S_OwnCharAttrDef(pc));
		}
		else if (skillId == AWAKEN_VALAKAS) { // 覺醒：巴拉卡斯
			pc.addStr(-5);
			pc.addCon(-5);
			pc.addDex(-5);
			pc.addCha(-5);
			pc.addInt(-5);
			pc.addWis(-5);
			pc.sendPackets(new S_OwnCharStatus2(pc, 0));
		}
		pc.setAwakeSkillId(0);
		undoPoly(pc);
		pc.stopMpReductionByAwake();
	}

	// 變身
	public static void doPoly(PcInstance pc) {
		int polyId = 6894;
		if (pc.hasSkillEffect(SHAPE_CHANGE)) {
			pc.killSkillEffectTimer(SHAPE_CHANGE);
		}
		ItemInstance weapon = pc.getWeapon();
		boolean weaponTakeoff = (weapon != null && !LsimulatorPolyMorph.isEquipableWeapon(polyId, weapon.getItem().getType()));
		if (weaponTakeoff) { // 解除武器時
			pc.setCurrentWeapon(0);
		}
		pc.setTempCharGfx(polyId);
		pc.sendPackets(new S_ChangeShape(pc.getId(), polyId, pc.getCurrentWeapon()));
		if (pc.isGmInvis()) { // GM隱身
		} else if (pc.isInvisble()) { // 一般隱身
			pc.broadcastPacketForFindInvis(new S_ChangeShape(pc.getId(), polyId, pc.getCurrentWeapon()), true);
		} else {
			pc.broadcastPacket(new S_ChangeShape(pc.getId(), polyId, pc.getCurrentWeapon()));
		}
		pc.getInventory().takeoffEquip(polyId); // 是否將裝備的武器強制解除。
	}

	// 解除變身
	public static void undoPoly(PcInstance pc) {
		int classId = pc.getClassId();
		pc.setTempCharGfx(classId);
		if (!pc.isDead()) {
			pc.sendPackets(new S_ChangeShape(pc.getId(), classId, pc.getCurrentWeapon()));
			pc.broadcastPacket(new S_ChangeShape(pc.getId(), classId, pc.getCurrentWeapon()));
		}
	}

}
