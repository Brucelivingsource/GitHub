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
package Lsimulator.server.server.model.Instance;

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_BALANCE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_IGNITION_TO_ALLY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_IGNITION_TO_ENEMY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_QUAKE_TO_ALLY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_QUAKE_TO_ENEMY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_SHOCK_TO_ALLY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_SHOCK_TO_ENEMY;
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.WarTimeController;
import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorCube;
import Lsimulator.server.server.model.LsimulatorMagic;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.poison.LsimulatorDamagePoison;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_OwnCharAttrDef;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.serverpackets.S_SkillSound;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorEffectInstance extends LsimulatorNpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int FW_DAMAGE_INTERVAL = 1000;

	private static final int CUBE_INTERVAL = 500; // キューブ範囲内に居るキャラクターをチェックする間隔

	private static final int CUBE_TIME = 8000; // 効果時間8秒?

	private static final int POISON_INTERVAL = 1000;

	public LsimulatorEffectInstance(LsimulatorNpc template) {
		super(template);

		int npcId = getNpcTemplate().get_npcId();
		if (npcId == 81157) { // FW
			GeneralThreadPool.getInstance().schedule(new FwDamageTimer(this), 0);
		}
		else if ((npcId == 80149 // キューブ[イグニション]
				)
				|| (npcId == 80150 // キューブ[クエイク]
				) || (npcId == 80151 // キューブ[ショック]
				) || (npcId == 80152)) { // キューブ[バランス]
			GeneralThreadPool.getInstance().schedule(new CubeTimer(this), 0);
		}
		else if (npcId == 93002) { // 毒霧
			GeneralThreadPool.getInstance().schedule(new PoisonTimer(this), 0);
		}
	}

	@Override
	public void onAction(LsimulatorPcInstance pc) {
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		allTargetClear();
		_master = null;
		LsimulatorWorld.getInstance().removeVisibleObject(this);
		LsimulatorWorld.getInstance().removeObject(this);
		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}

	class FwDamageTimer implements Runnable {
		private LsimulatorEffectInstance _effect;

		public FwDamageTimer(LsimulatorEffectInstance effect) {
			_effect = effect;
		}

		@Override
		public void run() {
			while (!_destroyed) {
				try {
					for (LsimulatorObject objects : LsimulatorWorld.getInstance().getVisibleObjects(_effect, 0)) {
						if (objects instanceof LsimulatorPcInstance) {
							LsimulatorPcInstance pc = (LsimulatorPcInstance) objects;
							if (pc.isDead()) {
								continue;
							}
							if (pc.getZoneType() == 1) {
								boolean isNowWar = false;
								int castleId = LsimulatorCastleLocation.getCastleIdByArea(pc);
								if (castleId > 0) {
									isNowWar = WarTimeController.getInstance().isNowWar(castleId);
								}
								if (!isNowWar) {
									continue;
								}
							}
							LsimulatorMagic magic = new LsimulatorMagic(_effect, pc);
							int damage = magic.calcPcFireWallDamage();
							if (damage == 0) {
								continue;
							}
							pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
							pc.broadcastPacket(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
							pc.receiveDamage(_effect, damage, false);
						}
						else if (objects instanceof LsimulatorMonsterInstance) {
							LsimulatorMonsterInstance mob = (LsimulatorMonsterInstance) objects;
							if (mob.isDead()) {
								continue;
							}
							LsimulatorMagic magic = new LsimulatorMagic(_effect, mob);
							int damage = magic.calcNpcFireWallDamage();
							if (damage == 0) {
								continue;
							}
							mob.broadcastPacket(new S_DoActionGFX(mob.getId(), ActionCodes.ACTION_Damage));
							mob.receiveDamage(_effect, damage);
						}
					}
					Thread.sleep(FW_DAMAGE_INTERVAL);
				}
				catch (InterruptedException ignore) {
					// ignore
				}
			}
		}
	}

	class CubeTimer implements Runnable {
		private LsimulatorEffectInstance _effect;

		public CubeTimer(LsimulatorEffectInstance effect) {
			_effect = effect;
		}

		@Override
		public void run() {
			while (!_destroyed) {
				try {
					for (LsimulatorObject objects : LsimulatorWorld.getInstance().getVisibleObjects(_effect, 3)) {
						if (objects instanceof LsimulatorPcInstance) {
							LsimulatorPcInstance pc = (LsimulatorPcInstance) objects;
							if (pc.isDead()) {
								continue;
							}
							LsimulatorPcInstance user = getUser(); // Cube使用者
							if (pc.getId() == user.getId()) {
								cubeToAlly(pc, _effect);
								continue;
							}
							if ((pc.getClanid() != 0) && (user.getClanid() == pc.getClanid())) {
								cubeToAlly(pc, _effect);
								continue;
							}
							if (pc.isInParty() && pc.getParty().isMember(user)) {
								cubeToAlly(pc, _effect);
								continue;
							}
							if (pc.getZoneType() == 1) { // セーフティーゾーンでは戦争中を除き敵には無効
								boolean isNowWar = false;
								int castleId = LsimulatorCastleLocation.getCastleIdByArea(pc);
								if (castleId > 0) {
									isNowWar = WarTimeController.getInstance().isNowWar(castleId);
								}
								if (!isNowWar) {
									continue;
								}
								cubeToEnemy(pc, _effect);
							}
							else {
								cubeToEnemy(pc, _effect);
							}
						}
						else if (objects instanceof LsimulatorMonsterInstance) {
							LsimulatorMonsterInstance mob = (LsimulatorMonsterInstance) objects;
							if (mob.isDead()) {
								continue;
							}
							cubeToEnemy(mob, _effect);
						}
					}
					Thread.sleep(CUBE_INTERVAL);
				}
				catch (InterruptedException ignore) {
					// ignore
				}
			}
		}
	}

	class PoisonTimer implements Runnable {
		private LsimulatorEffectInstance _effect;

		public PoisonTimer(LsimulatorEffectInstance effect) {
			_effect = effect;
		}

		@Override
		public void run() {
			while (!_destroyed) {
				try {
					for (LsimulatorObject objects : LsimulatorWorld.getInstance().getVisibleObjects(_effect, 0)) {
						if (!(objects instanceof LsimulatorMonsterInstance)) {
							LsimulatorCharacter cha = (LsimulatorCharacter) objects;
							LsimulatorDamagePoison.doInfection(_effect, cha, 3000, 20);
						}
					}
					Thread.sleep(POISON_INTERVAL);
				}
				catch (InterruptedException ignore) {
					// ignore
				}
			}
		}
	}

	private void cubeToAlly(LsimulatorCharacter cha, LsimulatorCharacter effect) {
		int npcId = getNpcTemplate().get_npcId();
		int castGfx = SkillsTable.getInstance().getTemplate(getSkillId()).getCastGfx();
		LsimulatorPcInstance pc = null;

		if (npcId == 80149) { // キューブ[イグニション]
			if (!cha.hasSkillEffect(STATUS_CUBE_IGNITION_TO_ALLY)) {
				cha.addFire(30);
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx));
				cha.setSkillEffect(STATUS_CUBE_IGNITION_TO_ALLY, CUBE_TIME);
			}
		}
		else if (npcId == 80150) { // キューブ[クエイク]
			if (!cha.hasSkillEffect(STATUS_CUBE_QUAKE_TO_ALLY)) {
				cha.addEarth(30);
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx));
				cha.setSkillEffect(STATUS_CUBE_QUAKE_TO_ALLY, CUBE_TIME);
			}
		}
		else if (npcId == 80151) { // キューブ[ショック]
			if (!cha.hasSkillEffect(STATUS_CUBE_SHOCK_TO_ALLY)) {
				cha.addWind(30);
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_OwnCharAttrDef(pc));
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx));
				cha.setSkillEffect(STATUS_CUBE_SHOCK_TO_ALLY, CUBE_TIME);
			}
		}
		else if (npcId == 80152) { // キューブ[バランス]
			if (!cha.hasSkillEffect(STATUS_CUBE_BALANCE)) {
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx));
				cha.setSkillEffect(STATUS_CUBE_BALANCE, CUBE_TIME);
				LsimulatorCube cube = new LsimulatorCube(effect, cha, STATUS_CUBE_BALANCE);
				cube.begin();
			}
		}
	}

	private void cubeToEnemy(LsimulatorCharacter cha, LsimulatorCharacter effect) {
		int npcId = getNpcTemplate().get_npcId();
		int castGfx2 = SkillsTable.getInstance().getTemplate(getSkillId()).getCastGfx2();
		LsimulatorPcInstance pc = null;
		if (npcId == 80149) { // キューブ[イグニション]
			if (!cha.hasSkillEffect(STATUS_CUBE_IGNITION_TO_ENEMY)) {
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx2));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx2));
				cha.setSkillEffect(STATUS_CUBE_IGNITION_TO_ENEMY, CUBE_TIME);
				LsimulatorCube cube = new LsimulatorCube(effect, cha, STATUS_CUBE_IGNITION_TO_ENEMY);
				cube.begin();
			}
		}
		else if (npcId == 80150) { // キューブ[クエイク]
			if (!cha.hasSkillEffect(STATUS_CUBE_QUAKE_TO_ENEMY)) {
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx2));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx2));
				cha.setSkillEffect(STATUS_CUBE_QUAKE_TO_ENEMY, CUBE_TIME);
				LsimulatorCube cube = new LsimulatorCube(effect, cha, STATUS_CUBE_QUAKE_TO_ENEMY);
				cube.begin();
			}
		}
		else if (npcId == 80151) { // キューブ[ショック]
			if (!cha.hasSkillEffect(STATUS_CUBE_SHOCK_TO_ENEMY)) {
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx2));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx2));
				cha.setSkillEffect(STATUS_CUBE_SHOCK_TO_ENEMY, CUBE_TIME);
				LsimulatorCube cube = new LsimulatorCube(effect, cha, STATUS_CUBE_SHOCK_TO_ENEMY);
				cube.begin();
			}
		}
		else if (npcId == 80152) { // キューブ[バランス]
			if (!cha.hasSkillEffect(STATUS_CUBE_BALANCE)) {
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
					pc.sendPackets(new S_SkillSound(pc.getId(), castGfx2));
				}
				cha.broadcastPacket(new S_SkillSound(cha.getId(), castGfx2));
				cha.setSkillEffect(STATUS_CUBE_BALANCE, CUBE_TIME);
				LsimulatorCube cube = new LsimulatorCube(effect, cha, STATUS_CUBE_BALANCE);
				cube.begin();
			}
		}
	}

	private LsimulatorPcInstance _pc;

	public void setUser(LsimulatorPcInstance pc) {
		_pc = pc;
	}

	public LsimulatorPcInstance getUser() {
		return _pc;
	}

	private int _skillId;

	public void setSkillId(int i) {
		_skillId = i;
	}

	public int getSkillId() {
		return _skillId;
	}

}
