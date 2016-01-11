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

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.WarTimeController;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWar;
import Lsimulator.server.server.model.LsimulatorWarSpawn;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class TowerInstance extends NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TowerInstance(LsimulatorNpc template) {
		super(template);
	}

	private LsimulatorCharacter _lastattacker;

	private int _castle_id;

	private int _crackStatus;

	@Override
	public void onPerceive(PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_NPCPack(this));
	}

	@Override
	public void onAction(PcInstance pc) {
		onAction(pc, 0);
	}

	@Override
	public void onAction(PcInstance pc, int skillId) {
		if ((getCurrentHp() > 0) && !isDead()) {
			LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);
			if (attack.calcHit()) {
				attack.calcDamage();
				attack.addPcPoisonAttack(pc, this);
				attack.addChaserAttack();
			}
			attack.action();
			attack.commit();
		}
	}

	@Override
	public void receiveDamage(LsimulatorCharacter attacker, int damage) { // 攻撃でＨＰを減らすときはここを使用
		if (_castle_id == 0) { // 初期設定で良いがいい場所がない
			if (isSubTower()) {
				_castle_id = LsimulatorCastleLocation.ADEN_CASTLE_ID;
			}
			else {
				_castle_id = LsimulatorCastleLocation.getCastleId(getX(), getY(), getMapId());
			}
		}

		if ((_castle_id > 0) && WarTimeController.getInstance().isNowWar(_castle_id)) { // 戦争時間内

			// アデン城のメインタワーはサブタワーが3つ以上破壊されている場合のみ攻撃可能
			if ((_castle_id == LsimulatorCastleLocation.ADEN_CASTLE_ID) && !isSubTower()) {
				int subTowerDeadCount = 0;
				for (LsimulatorObject l1object : LsimulatorWorld.getInstance().getObject()) {
					if (l1object instanceof TowerInstance) {
						TowerInstance tower = (TowerInstance) l1object;
						if (tower.isSubTower() && tower.isDead()) {
							subTowerDeadCount++;
							if (subTowerDeadCount == 4) {
								break;
							}
						}
					}
				}
				if (subTowerDeadCount < 3) {
					return;
				}
			}

			PcInstance pc = null;
			if (attacker instanceof PcInstance) {
				pc = (PcInstance) attacker;
			}
			else if (attacker instanceof PetInstance) {
				pc = (PcInstance) ((PetInstance) attacker).getMaster();
			}
			else if (attacker instanceof SummonInstance) {
				pc = (PcInstance) ((SummonInstance) attacker).getMaster();
			}
			if (pc == null) {
				return;
			}

			// 布告しているかチェック。但し、城主が居ない場合は布告不要
			boolean existDefenseClan = false;
			for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
				int clanCastleId = clan.getCastleId();
				if (clanCastleId == _castle_id) {
					existDefenseClan = true;
					break;
				}
			}
			boolean isProclamation = false;
			// 全戦争リストを取得
			for (LsimulatorWar war : LsimulatorWorld.getInstance().getWarList()) {
				if (_castle_id == war.GetCastleId()) { // 今居る城の戦争
					isProclamation = war.CheckClanInWar(pc.getClanname());
					break;
				}
			}
			if (  existDefenseClan &&   ! isProclamation ) { // 城主が居て、布告していない場合
				return;
			}

			if ((getCurrentHp() > 0) && ! isDead() ) {
				int newHp = getCurrentHp() - damage;
				if ((newHp <= 0) && !isDead()) {
					setCurrentHpDirect(0);
					setDead(true);
					setStatus(ActionCodes.ACTION_TowerDie);
					_lastattacker = attacker;
					_crackStatus = 0;
					Death death = new Death();
					GeneralThreadPool.getInstance().execute(death);
					// Death(attacker);
				}
				if (newHp > 0) {
					setCurrentHp(newHp);
					if ((  getMaxHp() >> 2 ) > getCurrentHp()) {
						if (_crackStatus != 3) {
							broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_TowerCrack3));
							setStatus(ActionCodes.ACTION_TowerCrack3);
							_crackStatus = 3;
						}
					}
					else if ( (getMaxHp() >> 1 ) > getCurrentHp()) {
						if (_crackStatus != 2) {
							broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_TowerCrack2));
							setStatus(ActionCodes.ACTION_TowerCrack2);
							_crackStatus = 2;
						}
					}
					else if ( ( ( getMaxHp() * 3) >> 2 ) > getCurrentHp()) {
						if (_crackStatus != 1) {
							broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_TowerCrack1));
							setStatus(ActionCodes.ACTION_TowerCrack1);
							_crackStatus = 1;
						}
					}
				}
			}
			else if (!isDead()) { // 念のため
				setDead(true);
				setStatus(ActionCodes.ACTION_TowerDie);
				_lastattacker = attacker;
				Death death = new Death();
				GeneralThreadPool.getInstance().execute(death);
				// Death(attacker);
			}
		}
	}

	@Override
	public void setCurrentHp(int i) {
		int currentHp = i;
		if (currentHp >= getMaxHp()) {
			currentHp = getMaxHp();
		}
		setCurrentHpDirect(currentHp);
	}

	class Death implements Runnable {
		LsimulatorCharacter lastAttacker = _lastattacker;

		LsimulatorObject object = LsimulatorWorld.getInstance().findObject(getId());

		TowerInstance npc = (TowerInstance) object;

		@Override
		public void run() {
			setCurrentHpDirect(0);
			setDead(true);
			setStatus(ActionCodes.ACTION_TowerDie);
			int targetobjid = npc.getId();

			npc.getMap().setPassable(npc.getLocation(), true);

			npc.broadcastPacket(new S_DoActionGFX(targetobjid, ActionCodes.ACTION_TowerDie));

			// クラウンをspawnする
			if (!isSubTower()) {
				LsimulatorWarSpawn warspawn = new LsimulatorWarSpawn();
				warspawn.SpawnCrown(_castle_id);
			}
		}
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
		for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}

	public boolean isSubTower() {
		return ((getNpcTemplate().get_npcId() == 81190) || (getNpcTemplate().get_npcId() == 81191) || (getNpcTemplate().get_npcId() == 81192) || (getNpcTemplate()
				.get_npcId() == 81193));
	}

}
