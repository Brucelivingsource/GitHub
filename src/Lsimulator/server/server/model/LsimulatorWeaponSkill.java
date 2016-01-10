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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ABSOLUTE_BARRIER;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BERSERKERS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COUNTER_MAGIC;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EARTH_BIND;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FREEZING_BLIZZARD;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FREEZING_BREATH;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ICE_LANCE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ILLUSION_AVATAR;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_FREEZE;
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.WarTimeController;
import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.datatables.WeaponSkillTable;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorMonsterInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.model.Instance.LsimulatorSummonInstance;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_EffectLocation;
import Lsimulator.server.server.serverpackets.S_Paralysis;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SkillSound;
import Lsimulator.server.server.serverpackets.S_UseAttackSkill;
import Lsimulator.server.server.templates.LsimulatorSkills;
import Lsimulator.server.server.utils.Random;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorPcInstance

public class LsimulatorWeaponSkill {

	private int _weaponId;

	private int _probability;

	private int _fixDamage;

	private int _randomDamage;

	private int _area;

	private int _skillId;

	private int _skillTime;

	private int _effectId;

	private int _effectTarget; // エフェクトの対象 0:相手 1:自分

	private boolean _isArrowType;

	private int _attr;

	public LsimulatorWeaponSkill(int weaponId, int probability, int fixDamage,
			int randomDamage, int area, int skillId, int skillTime,
			int effectId, int effectTarget, boolean isArrowType, int attr) {
		_weaponId = weaponId;
		_probability = probability;
		_fixDamage = fixDamage;
		_randomDamage = randomDamage;
		_area = area;
		_skillId = skillId;
		_skillTime = skillTime;
		_effectId = effectId;
		_effectTarget = effectTarget;
		_isArrowType = isArrowType;
		_attr = attr;
	}

	public int getWeaponId() {
		return _weaponId;
	}

	public int getProbability() {
		return _probability;
	}

	public int getFixDamage() {
		return _fixDamage;
	}

	public int getRandomDamage() {
		return _randomDamage;
	}

	public int getArea() {
		return _area;
	}

	public int getSkillId() {
		return _skillId;
	}

	public int getSkillTime() {
		return _skillTime;
	}

	public int getEffectId() {
		return _effectId;
	}

	public int getEffectTarget() {
		return _effectTarget;
	}

	public boolean isArrowType() {
		return _isArrowType;
	}

	public int getAttr() {
		return _attr;
	}

	public static double getWeaponSkillDamage(LsimulatorPcInstance pc, LsimulatorCharacter cha,
			int weaponId) {
		LsimulatorWeaponSkill weaponSkill = WeaponSkillTable.getInstance().getTemplate(
				weaponId);
		if ((pc == null) || (cha == null) || (weaponSkill == null)) {
			return 0;
		}

		int chance = Random.nextInt(100) + 1;
		if (weaponSkill.getProbability() < chance) {
			return 0;
		}

		int skillId = weaponSkill.getSkillId();
		if (skillId != 0) {
			LsimulatorSkills skill = SkillsTable.getInstance().getTemplate(skillId);
			if ((skill != null) && skill.getTarget().equals("buff")) {
				if (!isFreeze(cha)) { // 凍結状態orカウンターマジック中
					cha.setSkillEffect(skillId,
							weaponSkill.getSkillTime() * 1000);
				}
			}
		}

		int effectId = weaponSkill.getEffectId();
		if (effectId != 0) {
			int chaId = 0;
			if (weaponSkill.getEffectTarget() == 0) {
				chaId = cha.getId();
			} else {
				chaId = pc.getId();
			}
			boolean isArrowType = weaponSkill.isArrowType();
			if (!isArrowType) {
				pc.sendPackets(new S_SkillSound(chaId, effectId));
				pc.broadcastPacket(new S_SkillSound(chaId, effectId));
			} else {
				int[] data = {ActionCodes.ACTION_Attack, 0, effectId, 6};
				S_UseAttackSkill packet = new S_UseAttackSkill(pc, cha.getId(), cha.getX(), cha.getY(), data, false);
				pc.sendPackets(packet);
				pc.broadcastPacket(packet);
			}
		}

		double damage = 0;
		int randomDamage = weaponSkill.getRandomDamage();
		if (randomDamage != 0) {
			damage = Random.nextInt(randomDamage);
		}
		damage += weaponSkill.getFixDamage();

		int area = weaponSkill.getArea();
		if ((area > 0) || (area == -1)) { // 範囲の場合
			for (LsimulatorObject object : LsimulatorWorld.getInstance().getVisibleObjects(cha,
					area)) {
				if (object == null) {
					continue;
				}
				if (!(object instanceof LsimulatorCharacter)) {
					continue;
				}
				if (object.getId() == pc.getId()) {
					continue;
				}
				if (object.getId() == cha.getId()) { // 攻撃対象はLsimulatorAttackで処理するため除外
					continue;
				}

				// 攻撃対象がMOBの場合は、範囲内のMOBにのみ当たる
				// 攻撃対象がPC,Summon,Petの場合は、範囲内のPC,Summon,Pet,MOBに当たる
				if (cha instanceof LsimulatorMonsterInstance) {
					if (!(object instanceof LsimulatorMonsterInstance)) {
						continue;
					}
				}
				if ((cha instanceof LsimulatorPcInstance)
						|| (cha instanceof LsimulatorSummonInstance)
						|| (cha instanceof LsimulatorPetInstance)) {
					if (!((object instanceof LsimulatorPcInstance)
							|| (object instanceof LsimulatorSummonInstance)
							|| (object instanceof LsimulatorPetInstance) || (object instanceof LsimulatorMonsterInstance))) {
						continue;
					}
				}

				// 判斷是否在攻城戰中
				boolean isNowWar = false;
				int castleId = LsimulatorCastleLocation.getCastleIdByArea((LsimulatorCharacter)object);
				if (castleId > 0) {
					isNowWar = WarTimeController.getInstance().isNowWar(castleId);
				}
				if (!isNowWar) { // 非攻城戰區域
					// 對象不是怪物 且在安全區 不會打到
					if ( !(object instanceof LsimulatorMonsterInstance) && ((LsimulatorCharacter)object).getZoneType()== 1 ) 
						continue;
					// 寵物減傷
					if (object instanceof LsimulatorPetInstance)
						damage /= 8;
					else if (object instanceof LsimulatorSummonInstance) {
						LsimulatorSummonInstance summon = (LsimulatorSummonInstance) object;
						if (summon.isExsistMaster())
							damage /= 8;
					}
				}
				
				damage = calcDamageReduction(pc, (LsimulatorCharacter) object, damage,
						weaponSkill.getAttr());
				if (damage <= 0) {
					continue;
				}
				if (object instanceof LsimulatorPcInstance) {
					LsimulatorPcInstance targetPc = (LsimulatorPcInstance) object;
					targetPc.sendPackets(new S_DoActionGFX(targetPc.getId(),
							ActionCodes.ACTION_Damage));
					targetPc.broadcastPacket(new S_DoActionGFX(
							targetPc.getId(), ActionCodes.ACTION_Damage));
					targetPc.receiveDamage(pc, (int) damage, false);
				} else if ((object instanceof LsimulatorSummonInstance)
						|| (object instanceof LsimulatorPetInstance)
						|| (object instanceof LsimulatorMonsterInstance)) {
					LsimulatorNpcInstance targetNpc = (LsimulatorNpcInstance) object;
					targetNpc.broadcastPacket(new S_DoActionGFX(targetNpc
							.getId(), ActionCodes.ACTION_Damage));
					targetNpc.receiveDamage(pc, (int) damage);
				}
			}
		}

		return calcDamageReduction(pc, cha, damage, weaponSkill.getAttr());
	}

	public static double getBaphometStaffDamage(LsimulatorPcInstance pc, LsimulatorCharacter cha) {
		double dmg = 0;
		int chance = Random.nextInt(100) + 1;
		if (14 >= chance) {
			int locx = cha.getX();
			int locy = cha.getY();
			int sp = pc.getSp();
			int intel = pc.getInt();
			double bsk = 0;
			if (pc.hasSkillEffect(BERSERKERS)) {
				bsk = 0.2;
			}
			dmg = (intel + sp) * (1.8 + bsk) + Random.nextInt(intel + sp) * 1.8;
			S_EffectLocation packet = new S_EffectLocation(locx, locy, 129);
			pc.sendPackets(packet);
			pc.broadcastPacket(packet);
		}
		return calcDamageReduction(pc, cha, dmg, LsimulatorSkills.ATTR_EARTH);
	}

	/** 骰子匕首 */
	public static double getDiceDaggerDamage(LsimulatorPcInstance pc, LsimulatorCharacter cha,
			LsimulatorItemInstance weapon) {
		double dmg = 0;
		int chance = Random.nextInt(100) + 1;
		if (2 >= chance) {
			dmg = cha.getCurrentHp() * 2 / 3;
			if (cha.getCurrentHp() - dmg < 0) {
				dmg = 0;
			}
			String msg = weapon.getLogName();
			pc.sendPackets(new S_ServerMessage(158, msg));
			// \f1%0%s 消失。
			pc.getInventory().removeItem(weapon, 1);
		}
		return dmg;
	}

	public static double getKiringkuDamage(LsimulatorPcInstance pc, LsimulatorCharacter cha) {
		int dmg = 0;
		int dice = 5;
		int diceCount = 2;
		int value = 0;
		int kiringkuDamage = 0;
		int charaIntelligence = 0;
		if (pc.getWeapon().getItem().getItemId() == 270) {
			value = 16;
		} else {
			value = 14;
		}

		for (int i = 0; i < diceCount; i++) {
			kiringkuDamage += (Random.nextInt(dice) + 1);
		}
		kiringkuDamage += value;

		int spByItem = pc.getSp() - pc.getTrueSp(); // アイテムによるSP変動
		charaIntelligence = pc.getInt() + spByItem - 12;
		if (charaIntelligence < 1) {
			charaIntelligence = 1;
		}
		double kiringkuCoefficientA = (1.0 + charaIntelligence * 3.0 / 32.0);

		kiringkuDamage *= kiringkuCoefficientA;

		double kiringkuFloor = Math.floor(kiringkuDamage);

		dmg += kiringkuFloor + pc.getWeapon().getEnchantLevel()
				+ pc.getOriginalMagicDamage();

		if (pc.hasSkillEffect(ILLUSION_AVATAR)) {
			dmg += 10;
		}

		if (pc.getWeapon().getItem().getItemId() == 270) {
			pc.sendPackets(new S_SkillSound(pc.getId(), 6983));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 6983));
		} else {
			pc.sendPackets(new S_SkillSound(pc.getId(), 7049));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 7049));
		}

		return calcDamageReduction(pc, cha, dmg, 0);
	}

	public static double getAreaSkillWeaponDamage(LsimulatorPcInstance pc,
			LsimulatorCharacter cha, int weaponId) {
		double dmg = 0;
		int probability = 0;
		int attr = 0;
		int chance = Random.nextInt(100) + 1;
		if (weaponId == 263 || weaponId == 287) { // フリージングランサー
			probability = 5;
			attr = LsimulatorSkills.ATTR_WATER;
		} else if (weaponId == 260) { // レイジングウィンド
			probability = 4;
			attr = LsimulatorSkills.ATTR_WIND;
		}
		if (probability >= chance) {
			int sp = pc.getSp();
			int intel = pc.getInt();
			int area = 0;
			int effectTargetId = 0;
			int effectId = 0;
			LsimulatorCharacter areaBase = cha;
			double damageRate = 0;

			if (weaponId == 263 || weaponId == 290) { // フリージングランサー
				area = 3;
				damageRate = 1.4D;
				effectTargetId = cha.getId();
				effectId = 1804;
				areaBase = cha;
			} else if (weaponId == 260) { // レイジングウィンド
				area = 4;
				damageRate = 1.5D;
				effectTargetId = pc.getId();
				effectId = 758;
				areaBase = pc;
			}
			double bsk = 0;
			if (pc.hasSkillEffect(BERSERKERS)) {
				bsk = 0.2;
			}
			dmg = (intel + sp) * (damageRate + bsk)
					+ Random.nextInt(intel + sp) * damageRate;
			pc.sendPackets(new S_SkillSound(effectTargetId, effectId));
			pc.broadcastPacket(new S_SkillSound(effectTargetId, effectId));

			for (LsimulatorObject object : LsimulatorWorld.getInstance().getVisibleObjects(
					areaBase, area)) {
				if (object == null) {
					continue;
				}
				if (!(object instanceof LsimulatorCharacter)) {
					continue;
				}
				if (object.getId() == pc.getId()) {
					continue;
				}
				if (object.getId() == cha.getId()) { // 攻撃対象は除外
					continue;
				}

				// 攻撃対象がMOBの場合は、範囲内のMOBにのみ当たる
				// 攻撃対象がPC,Summon,Petの場合は、範囲内のPC,Summon,Pet,MOBに当たる
				if (cha instanceof LsimulatorMonsterInstance) {
					if (!(object instanceof LsimulatorMonsterInstance)) {
						continue;
					}
				}
				if ((cha instanceof LsimulatorPcInstance)
						|| (cha instanceof LsimulatorSummonInstance)
						|| (cha instanceof LsimulatorPetInstance)) {
					if (!((object instanceof LsimulatorPcInstance)
							|| (object instanceof LsimulatorSummonInstance)
							|| (object instanceof LsimulatorPetInstance) || (object instanceof LsimulatorMonsterInstance))) {
						continue;
					}
				}

				dmg = calcDamageReduction(pc, (LsimulatorCharacter) object, dmg, attr);
				if (dmg <= 0) {
					continue;
				}
				if (object instanceof LsimulatorPcInstance) {
					LsimulatorPcInstance targetPc = (LsimulatorPcInstance) object;
					targetPc.sendPackets(new S_DoActionGFX(targetPc.getId(),
							ActionCodes.ACTION_Damage));
					targetPc.broadcastPacket(new S_DoActionGFX(
							targetPc.getId(), ActionCodes.ACTION_Damage));
					targetPc.receiveDamage(pc, (int) dmg, false);
				} else if ((object instanceof LsimulatorSummonInstance)
						|| (object instanceof LsimulatorPetInstance)
						|| (object instanceof LsimulatorMonsterInstance)) {
					LsimulatorNpcInstance targetNpc = (LsimulatorNpcInstance) object;
					targetNpc.broadcastPacket(new S_DoActionGFX(targetNpc
							.getId(), ActionCodes.ACTION_Damage));
					targetNpc.receiveDamage(pc, (int) dmg);
				}
			}
		}
		return calcDamageReduction(pc, cha, dmg, attr);
	}

	public static double getLightningEdgeDamage(LsimulatorPcInstance pc, LsimulatorCharacter cha) {
		double dmg = 0;
		int chance = Random.nextInt(100) + 1;
		if (4 >= chance) {
			int sp = pc.getSp();
			int intel = pc.getInt();
			double bsk = 0;
			if (pc.hasSkillEffect(BERSERKERS)) {
				bsk = 0.2;
			}
			dmg = (intel + sp) * (2 + bsk) + Random.nextInt(intel + sp) * 2;

			pc.sendPackets(new S_SkillSound(cha.getId(), 10));
			pc.broadcastPacket(new S_SkillSound(cha.getId(), 10));
		}
		return calcDamageReduction(pc, cha, dmg, LsimulatorSkills.ATTR_WIND);
	}

	public static void giveArkMageDiseaseEffect(LsimulatorPcInstance pc, LsimulatorCharacter cha) {
		int chance = Random.nextInt(1000) + 1;
		int probability = (5 - ((cha.getMr() / 10) * 5)) * 10;
		if (probability == 0) {
			probability = 10;
		}
		if (probability >= chance) {
			LsimulatorSkillUse l1skilluse = new LsimulatorSkillUse();
			l1skilluse.handleCommands(pc, 56, cha.getId(), cha.getX(),
					cha.getY(), null, 0, LsimulatorSkillUse.TYPE_GMBUFF);
		}
	}

	public static void giveFettersEffect(LsimulatorPcInstance pc, LsimulatorCharacter cha) {
		int fettersTime = 8000;
		if (isFreeze(cha)) { // 凍結状態orカウンターマジック中
			return;
		}
		if ((Random.nextInt(100) + 1) <= 2) {
			LsimulatorEffectSpawn.getInstance().spawnEffect(81182, fettersTime,
					cha.getX(), cha.getY(), cha.getMapId());
			if (cha instanceof LsimulatorPcInstance) {
				LsimulatorPcInstance targetPc = (LsimulatorPcInstance) cha;
				targetPc.setSkillEffect(STATUS_FREEZE, fettersTime);
				targetPc.sendPackets(new S_SkillSound(targetPc.getId(), 4184));
				targetPc.broadcastPacket(new S_SkillSound(targetPc.getId(),
						4184));
				targetPc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND,
						true));
			} else if ((cha instanceof LsimulatorMonsterInstance)
					|| (cha instanceof LsimulatorSummonInstance)
					|| (cha instanceof LsimulatorPetInstance)) {
				LsimulatorNpcInstance npc = (LsimulatorNpcInstance) cha;
				npc.setSkillEffect(STATUS_FREEZE, fettersTime);
				npc.broadcastPacket(new S_SkillSound(npc.getId(), 4184));
				npc.setParalyzed(true);
			}
		}
	}

	public static double calcDamageReduction(LsimulatorPcInstance pc, LsimulatorCharacter cha,
			double dmg, int attr) {
		// 凍結状態orカウンターマジック中
		if (isFreeze(cha)) {
			return 0;
		}

		// MRによるダメージ軽減
		int mr = cha.getMr();
		double mrFloor = 0;
		if (mr <= 100) {
			mrFloor = Math.floor((mr - pc.getOriginalMagicHit()) / 2);
		} else if (mr >= 100) {
			mrFloor = Math.floor((mr - pc.getOriginalMagicHit()) / 10);
		}
		double mrCoefficient = 0;
		if (mr <= 100) {
			mrCoefficient = 1 - 0.01 * mrFloor;
		} else if (mr >= 100) {
			mrCoefficient = 0.6 - 0.01 * mrFloor;
		}
		dmg *= mrCoefficient;

		// 属性によるダメージ軽減
		int resist = 0;
		if (attr == LsimulatorSkills.ATTR_EARTH) {
			resist = cha.getEarth();
		} else if (attr == LsimulatorSkills.ATTR_FIRE) {
			resist = cha.getFire();
		} else if (attr == LsimulatorSkills.ATTR_WATER) {
			resist = cha.getWater();
		} else if (attr == LsimulatorSkills.ATTR_WIND) {
			resist = cha.getWind();
		}
		int resistFloor = (int) (0.32 * Math.abs(resist));
		if (resist >= 0) {
			resistFloor *= 1;
		} else {
			resistFloor *= -1;
		}
		double attrDeffence = resistFloor / 32.0;
		dmg = (1.0 - attrDeffence) * dmg;

		return dmg;
	}

	private static boolean isFreeze(LsimulatorCharacter cha) {
		if (cha.hasSkillEffect(STATUS_FREEZE)) {
			return true;
		}
		if (cha.hasSkillEffect(ABSOLUTE_BARRIER)) {
			return true;
		}
		if (cha.hasSkillEffect(ICE_LANCE)) {
			return true;
		}
		if (cha.hasSkillEffect(FREEZING_BLIZZARD)) {
			return true;
		}
		if (cha.hasSkillEffect(FREEZING_BREATH)) {
			return true;
		}
		if (cha.hasSkillEffect(EARTH_BIND)) {
			return true;
		}

		// カウンターマジック判定
		if (cha.hasSkillEffect(COUNTER_MAGIC)) {
			cha.removeSkillEffect(COUNTER_MAGIC);
			int castgfx = SkillsTable.getInstance().getTemplate(COUNTER_MAGIC)
					.getCastGfx();
			cha.broadcastPacket(new S_SkillSound(cha.getId(), castgfx));
			if (cha instanceof LsimulatorPcInstance) {
				LsimulatorPcInstance pc = (LsimulatorPcInstance) cha;
				pc.sendPackets(new S_SkillSound(pc.getId(), castgfx));
			}
			return true;
		}
		return false;
	}

}
