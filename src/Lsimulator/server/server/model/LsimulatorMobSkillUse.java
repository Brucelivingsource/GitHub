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

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.MobSkillTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.datatables.SprTable;
import Lsimulator.server.server.model.Instance.LsimulatorMonsterInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.model.Instance.LsimulatorSummonInstance;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_CharVisualUpdate;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.serverpackets.S_SkillSound;
import Lsimulator.server.server.templates.LsimulatorMobSkill;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.templates.LsimulatorSkills;
import Lsimulator.server.server.utils.Random;
import Lsimulator.server.server.utils.collections.Lists;
import Lsimulator.server.server.utils.collections.Maps;

public class LsimulatorMobSkillUse {
	private static Logger _log = Logger.getLogger(LsimulatorMobSkillUse.class.getName());

	private LsimulatorMobSkill _mobSkillTemplate = null;

	private LsimulatorNpcInstance _attacker = null;

	private LsimulatorCharacter _target = null;

	private int _sleepTime = 0;

	private int _skillUseCount[];

	public LsimulatorMobSkillUse(LsimulatorNpcInstance npc) {
		_sleepTime = 0;

		_mobSkillTemplate = MobSkillTable.getInstance().getTemplate(npc.getNpcTemplate().get_npcId());
		if (_mobSkillTemplate == null) {
			return;
		}
		_attacker = npc;
		_skillUseCount = new int[getMobSkillTemplate().getSkillSize()];
	}

	private int getSkillUseCount(int idx) {
		return _skillUseCount[idx];
	}

	private void skillUseCountUp(int idx) {
		_skillUseCount[idx]++;
	}

	public void resetAllSkillUseCount() {
		if (getMobSkillTemplate() == null) {
			return;
		}

		for (int i = 0; i < getMobSkillTemplate().getSkillSize(); i++) {
			_skillUseCount[i] = 0;
		}
	}

	public int getSleepTime() {
		return _sleepTime;
	}

	public void setSleepTime(int i) {
		_sleepTime = i;
	}

	public LsimulatorMobSkill getMobSkillTemplate() {
		return _mobSkillTemplate;
	}

	/*
	 * トリガーの条件のみチェック。
	 */
	public boolean isSkillTrigger(LsimulatorCharacter tg) {
		if (_mobSkillTemplate == null) {
			return false;
		}
		_target = tg;

		int type;
		type = getMobSkillTemplate().getType(0);

		if (type == LsimulatorMobSkill.TYPE_NONE) {
			return false;
		}

		for (int i = 0; (i < getMobSkillTemplate().getSkillSize()) && (getMobSkillTemplate().getType(i) != LsimulatorMobSkill.TYPE_NONE); i++) {
			if (isSkillUseble(i, false)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * スキル攻撃 スキル攻撃可能ならばtrueを返す。 攻撃できなければfalseを返す。
	 */
	public boolean skillUse(LsimulatorCharacter tg, boolean isTriRnd) {
		if (_mobSkillTemplate == null) {
			return false;
		}
		_target = tg;

		int type;
		type = getMobSkillTemplate().getType(0);

		if (type == LsimulatorMobSkill.TYPE_NONE) {
			return false;
		}

		int[] skills = null;
		int skillSizeCounter = 0;
		int skillSize = getMobSkillTemplate().getSkillSize();
		if (skillSize >= 0) {
			skills = new int[skillSize];
		}

		for (int i = 0; (i < getMobSkillTemplate().getSkillSize()) && (getMobSkillTemplate().getType(i) != LsimulatorMobSkill.TYPE_NONE); i++) {
			if (   ! isSkillUseble(i, isTriRnd) ) {
				continue;
			}
			else { // 条件にあうスキルが存在する
				skills[skillSizeCounter] = i;
				skillSizeCounter++;
			}
		}

		if (skillSizeCounter != 0) {
			int num = Random.nextInt(skillSizeCounter);
			if (useSkill(skills[num])) { // スキル使用
				return true;
			}
		}

		return false;
	}

	private boolean useSkill(int i) {
		// 對自身施法判斷
		int changeType = getMobSkillTemplate().getChangeTarget(i);
		if (changeType == 2) {
			_target = changeTarget(changeType, i);
		}

		boolean isUseSkill = false;
		int type = getMobSkillTemplate().getType(i);
		if (type == LsimulatorMobSkill.TYPE_PHYSICAL_ATTACK) { // 物理攻撃
			if ( physicalAttack(i) ) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		else if (type == LsimulatorMobSkill.TYPE_MAGIC_ATTACK) { // 魔法攻撃
			if ( magicAttack(i) ) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		else if (type == LsimulatorMobSkill.TYPE_SUMMON) { // サモンする
			if ( summon(i) ) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		else if (type == LsimulatorMobSkill.TYPE_POLY) { // 強制変身させる
			if ( poly(i) ) {
				skillUseCountUp(i);
				isUseSkill = true;
			}
		}
		return isUseSkill;
	}

	private boolean summon(int idx) {
		int summonId = getMobSkillTemplate().getSummon(idx);
		int min = getMobSkillTemplate().getSummonMin(idx);
		int max = getMobSkillTemplate().getSummonMax(idx);
		int count = 0;
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);

		if (summonId == 0) {
			return false;
		}

		// 施法動作
		if (actId > 0) {
			S_DoActionGFX gfx = new S_DoActionGFX(_attacker.getId(), actId);
			_attacker.broadcastPacket(gfx);
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);
		}
		// 魔方陣
		if (gfxId > 0) {
			_attacker.broadcastPacket(new S_SkillSound(_attacker.getId(), gfxId));
		}
		count = Random.nextInt(max) + min;
		mobspawn(summonId, count);
		return true;
	}

	/*
	 * 15セル以内で射線が通るPCを指定したモンスターに強制変身させる。 対PCしか使えない。
	 */
	private boolean poly(int idx) {
		int polyId = getMobSkillTemplate().getPolyId(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		boolean usePoly = false;

		if (polyId == 0) {
			return false;
		}
		// 施法動作
		if (actId > 0) {
			S_DoActionGFX gfx = new S_DoActionGFX(_attacker.getId(), actId);
			_attacker.broadcastPacket(gfx);
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);
		}

		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getVisiblePlayer(_attacker)) {
			if (pc.isDead()) { // 死亡
				continue;
			}
			if (pc.isGhost()) {
				continue;
			}
			if (pc.isGmInvis()) {
				continue;
			}
			if (   ! _attacker.glanceCheck(pc.getX(), pc.getY()) ) {
				continue; // 射線が通らない
			}

			switch (_attacker.getNpcTemplate().get_npcId()) {
				case 81082: // 火焰之影
					pc.getInventory().takeoffEquip(945); // 將目標裝備卸下。
					break;
				default:
					break;
			}
			_attacker.broadcastPacket(new S_SkillSound(pc.getId(), 230));
			LsimulatorPolyMorph.doPoly(pc, polyId, 1800, LsimulatorPolyMorph.MORPH_BY_NPC);
			usePoly = true;
		}
		return usePoly;
	}

	private boolean magicAttack(int idx) {
		LsimulatorSkillUse skillUse = new LsimulatorSkillUse();
		int skillid = getMobSkillTemplate().getSkillId(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);
		int mpConsume = getMobSkillTemplate().getMpConsume(idx);
		boolean canUseSkill = false;

		if (skillid > 0) {
			skillUse.setSkillRanged(getMobSkillTemplate().getRange(idx)); // 變更技能施放距離
			skillUse.setSkillRanged(getMobSkillTemplate().getSkillArea(idx)); // 變更技能施放範圍
			canUseSkill = skillUse.checkUseSkill(null, skillid, _target.getId(), _target.getX(), _target.getY(), null, 0, LsimulatorSkillUse.TYPE_NORMAL,
					_attacker, actId, gfxId, mpConsume);
		}

		LsimulatorSkills skill = SkillsTable.getInstance().getTemplate(skillid);
		if (skill.getTarget().equals("buff") && _target.hasSkillEffect(skillid)) {
			return false;
		}

		if ( canUseSkill ) {
			if (getMobSkillTemplate().getLeverage(idx) > 0) {
				skillUse.setLeverage(getMobSkillTemplate().getLeverage(idx));
			}
			skillUse.handleCommands(null, skillid, _target.getId(), _target.getX(), _target.getX(), null, 0, LsimulatorSkillUse.TYPE_NORMAL, _attacker);

			// 延遲時間判斷
			if (actId == 0) {
				actId = skill.getActionId();
			}
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);

			return true;
		}
		return false;
	}

	/*
	 * 物理攻撃
	 */
	private boolean physicalAttack(int idx) {
		Map<Integer, Integer> targetList = Maps.newConcurrentMap();
		int areaWidth = getMobSkillTemplate().getAreaWidth(idx);
		int areaHeight = getMobSkillTemplate().getAreaHeight(idx);
		int range = getMobSkillTemplate().getRange(idx);
		int actId = getMobSkillTemplate().getActid(idx);
		int gfxId = getMobSkillTemplate().getGfxid(idx);

		// レンジ外
		if (_attacker.getLocation().getTileLineDistance(_target.getLocation()) > range) {
			return false;
		}

		// 障害物がある場合攻撃不可能
		if (!_attacker.glanceCheck(_target.getX(), _target.getY())) {
			return false;
		}

		_attacker.setHeading(_attacker.targetDirection(_target.getX(), _target.getY())); // 向きのセット

		if (areaHeight > 0) {
			// 範囲攻撃
			List<LsimulatorObject> objs = LsimulatorWorld.getInstance().getVisibleBoxObjects(_attacker, _attacker.getHeading(), areaWidth, areaHeight);

			for (LsimulatorObject obj : objs) {
				if (!(obj instanceof LsimulatorCharacter)) { // ターゲットがキャラクター以外の場合何もしない。
					continue;
				}

				LsimulatorCharacter cha = (LsimulatorCharacter) obj;
				if (cha.isDead()) { // 死んでるキャラクターは対象外
					continue;
				}

				// ゴースト状態は対象外
				if (cha instanceof LsimulatorPcInstance) {
					if (((LsimulatorPcInstance) cha).isGhost()) {
						continue;
					}
				}

				// 障害物がある場合は対象外
				if (!_attacker.glanceCheck(cha.getX(), cha.getY())) {
					continue;
				}

				if ((_target instanceof LsimulatorPcInstance) || (_target instanceof LsimulatorSummonInstance) || (_target instanceof LsimulatorPetInstance)) {
					// 対PC
					if (((obj instanceof LsimulatorPcInstance) && !((LsimulatorPcInstance) obj).isGhost() && !((LsimulatorPcInstance) obj).isGmInvis())
							|| (obj instanceof LsimulatorSummonInstance) || (obj instanceof LsimulatorPetInstance)) {
						targetList.put(obj.getId(), 0);
					}
				}
				else {
					// 対NPC
					if (obj instanceof LsimulatorMonsterInstance) {
						targetList.put(obj.getId(), 0);
					}
				}
			}
		}
		else {
			// 単体攻撃
			targetList.put(_target.getId(), 0); // ターゲットのみ追加
		}

		if (targetList.isEmpty()) {
			return false;
		}

		Iterator<Integer> ite = targetList.keySet().iterator();
		while (ite.hasNext()) {
			int targetId = ite.next();
			LsimulatorAttack attack = new LsimulatorAttack(_attacker, (LsimulatorCharacter) LsimulatorWorld.getInstance().findObject(targetId));
			if (attack.calcHit()) {
				if (getMobSkillTemplate().getLeverage(idx) > 0) {
					attack.setLeverage(getMobSkillTemplate().getLeverage(idx));
				}
				attack.calcDamage();
			}
			if (actId > 0) {
				attack.setActId(actId);
			}
			// 攻撃モーションは実際のターゲットに対してのみ行う
			if (targetId == _target.getId()) {
				if (gfxId > 0) {
					_attacker.broadcastPacket(new S_SkillSound(_attacker.getId(), gfxId));
				}
				attack.action();
			}
			attack.commit();
		}

		if (actId > 0) {
			_sleepTime = SprTable.getInstance().getSprSpeed(_attacker.getTempCharGfx(), actId);
		} else {
			_sleepTime = _attacker.getAtkspeed();
		}
		return true;
	}

	/*
	 * トリガーの条件のみチェック
	 */
	private boolean isSkillUseble(int skillIdx, boolean isTriRnd) {
		boolean useble = false;
		int type = getMobSkillTemplate().getType(skillIdx);
		int chance = Random.nextInt(100) + 1;

		if (chance > getMobSkillTemplate().getTriggerRandom(skillIdx)) {
			return false;
		}

		if (isTriRnd || (type == LsimulatorMobSkill.TYPE_SUMMON) || (type == LsimulatorMobSkill.TYPE_POLY)) {
			/*if (getMobSkillTemplate().getTriggerRandom(skillIdx) > 0) {
				int chance = Random.nextInt(100) + 1;
				if (chance < getMobSkillTemplate().getTriggerRandom(skillIdx)) {
					useble = true;
				}
				else {
					return false;
				}
			}*/ // 確定此修改後的模式是仿正的，再移除此註解掉的程式碼
			useble = true;
		}

		if (getMobSkillTemplate().getTriggerHp(skillIdx) > 0) {
			int hpRatio = (_attacker.getCurrentHp() * 100) / _attacker.getMaxHp();
			if (hpRatio <= getMobSkillTemplate().getTriggerHp(skillIdx)) {
				useble = true;
			}
			else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerCompanionHp(skillIdx) > 0) {
			LsimulatorNpcInstance companionNpc = searchMinCompanionHp();
			if (companionNpc == null) {
				return false;
			}

			int hpRatio = (companionNpc.getCurrentHp() * 100) / companionNpc.getMaxHp();
			if (hpRatio <= getMobSkillTemplate().getTriggerCompanionHp(skillIdx)) {
				useble = true;
				_target = companionNpc; // ターゲットの入れ替え
			}
			else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerRange(skillIdx) != 0) {
			int distance = _attacker.getLocation().getTileLineDistance(_target.getLocation());

			if (getMobSkillTemplate().isTriggerDistance(skillIdx, distance)) {
				useble = true;
			}
			else {
				return false;
			}
		}

		if (getMobSkillTemplate().getTriggerCount(skillIdx) > 0) {
			if (getSkillUseCount(skillIdx) < getMobSkillTemplate().getTriggerCount(skillIdx)) {
				useble = true;
			}
			else {
				return false;
			}
		}
		return useble;
	}

	private LsimulatorNpcInstance searchMinCompanionHp() {
		LsimulatorNpcInstance npc;
		LsimulatorNpcInstance minHpNpc = null;
		int hpRatio = 100;
		int companionHpRatio;
		int family = _attacker.getNpcTemplate().get_family();

		for (LsimulatorObject object : LsimulatorWorld.getInstance().getVisibleObjects(_attacker)) {
			if (object instanceof LsimulatorNpcInstance) {
				npc = (LsimulatorNpcInstance) object;
				if (npc.getNpcTemplate().get_family() == family) {
					companionHpRatio = (npc.getCurrentHp() * 100) / npc.getMaxHp();
					if (companionHpRatio < hpRatio) {
						hpRatio = companionHpRatio;
						minHpNpc = npc;
					}
				}
			}
		}
		return minHpNpc;
	}

	private void mobspawn(int summonId, int count) {
		int i;

		for (i = 0; i < count; i++) {
			mobspawn(summonId);
		}
	}

	private void mobspawn(int summonId) {
		try {
			LsimulatorNpc spawnmonster = NpcTable.getInstance().getTemplate(summonId);
			if (spawnmonster != null) {
				LsimulatorNpcInstance mob = null;
				try {
					String implementationName = spawnmonster.getImpl();
					Constructor<?> _constructor = Class.forName(
							(new StringBuilder()).append("Lsimulator.server.server.model.Instance.").append(implementationName).append("Instance")
									.toString()).getConstructors()[0];
					mob = (LsimulatorNpcInstance) _constructor.newInstance(new Object[]
					{ spawnmonster });
					mob.setId(IdFactory.getInstance().nextId());
					LsimulatorLocation loc = _attacker.getLocation().randomLocation(8, false);
					int heading = Random.nextInt(8);
					mob.setX(loc.getX());
					mob.setY(loc.getY());
					mob.setHomeX(loc.getX());
					mob.setHomeY(loc.getY());
					short mapid = _attacker.getMapId();
					mob.setMap(mapid);
					mob.setHeading(heading);
					LsimulatorWorld.getInstance().storeObject(mob);
					LsimulatorWorld.getInstance().addVisibleObject(mob);
					LsimulatorObject object = LsimulatorWorld.getInstance().findObject(mob.getId());
					LsimulatorMonsterInstance newnpc = (LsimulatorMonsterInstance) object;
					newnpc.set_storeDroped(true); // 召喚怪不會掉落道具
					if (newnpc.getTempCharGfx() == 145) { // 史巴托
						newnpc.setStatus(11);
						newnpc.broadcastPacket(new S_NPCPack(newnpc));
						newnpc.broadcastPacket(new S_DoActionGFX(newnpc.getId(), ActionCodes.ACTION_Appear));
						newnpc.setStatus(0);
						newnpc.broadcastPacket(new S_CharVisualUpdate(newnpc, newnpc.getStatus()));
					} else if (newnpc.getTempCharGfx() == 7591) { // 泥龍(地)
						newnpc.broadcastPacket(new S_NPCPack(newnpc));
						newnpc.broadcastPacket(new S_DoActionGFX(newnpc.getId(), ActionCodes.ACTION_AxeWalk));
					}
					newnpc.onNpcAI();
					newnpc.turnOnOffLight();
					newnpc.startChat(LsimulatorNpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
				}
				catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	// 現在ChangeTargetで有効な値は2,3のみ
	private LsimulatorCharacter changeTarget(int type, int idx) {
		LsimulatorCharacter target;

		switch (type) {
			case LsimulatorMobSkill.CHANGE_TARGET_ME:
				target = _attacker;
				break;
			case LsimulatorMobSkill.CHANGE_TARGET_RANDOM:
				// ターゲット候補の選定
				List<LsimulatorCharacter> targetList = Lists.newList();
				for (LsimulatorObject obj : LsimulatorWorld.getInstance().getVisibleObjects(_attacker)) {
					if ((obj instanceof LsimulatorPcInstance) || (obj instanceof LsimulatorPetInstance) || (obj instanceof LsimulatorSummonInstance)) {
						LsimulatorCharacter cha = (LsimulatorCharacter) obj;

						int distance = _attacker.getLocation().getTileLineDistance(cha.getLocation());

						// 発動範囲外のキャラクターは対象外
						if (!getMobSkillTemplate().isTriggerDistance(idx, distance)) {
							continue;
						}

						// 障害物がある場合は対象外
						if (!_attacker.glanceCheck(cha.getX(), cha.getY())) {
							continue;
						}

						if (!_attacker.getHateList().containsKey(cha)) { // ヘイトがない場合対象外
							continue;
						}

						if (cha.isDead()) { // 死んでるキャラクターは対象外
							continue;
						}

						// ゴースト状態は対象外
						if (cha instanceof LsimulatorPcInstance) {
							if (((LsimulatorPcInstance) cha).isGhost()) {
								continue;
							}
						}
						targetList.add((LsimulatorCharacter) obj);
					}
				}

				if (targetList.isEmpty()) {
					target = _target;
				}
				else {
					int randomSize = targetList.size() * 100;
					int targetIndex = Random.nextInt(randomSize) / 100;
					target = targetList.get(targetIndex);
				}
				break;

			default:
				target = _target;
				break;
		}
		return target;
	}
}
