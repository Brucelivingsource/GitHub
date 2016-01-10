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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FOG_OF_SLEEPING;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.datatables.DropTable;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.identity.LsimulatorSystemMessageId;
import Lsimulator.server.server.serverpackets.S_ChangeHeading;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.serverpackets.S_NpcChatPacket;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorItem;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.CalcExp;
import Lsimulator.server.server.utils.Random;

public class LsimulatorGuardianInstance extends LsimulatorNpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger.getLogger(LsimulatorGuardianInstance.class
			.getName());

	private LsimulatorGuardianInstance _npc = this;

	private int GDROPITEM_TIME = Config.GDROPITEM_TIME;

	/**
	 * @param template
	 */
	public LsimulatorGuardianInstance(LsimulatorNpc template) {
		super(template);
		if (!isDropitems()) {
			doGDropItem(0);
		}
	}

	@Override
	public void searchTarget() {
		// ターゲット検索
		LsimulatorPcInstance targetPlayer = null;

		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getVisiblePlayer(this)) {
			if ((pc.getCurrentHp() <= 0) || pc.isDead() || pc.isGm()
					|| pc.isGhost()) {
				continue;
			}
			if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) { // インビジチェック
				if (!pc.isElf()) { // エルフ以外
					targetPlayer = pc;
					wideBroadcastPacket(new S_NpcChatPacket(this, "$804", 2)); // エルフ以外の者よ、命が惜しければ早くここから去れ。ここは神聖な場所だ。
					break;
				} else if (pc.isElf() && pc.isWantedForElf()) {
					targetPlayer = pc;
					wideBroadcastPacket(new S_NpcChatPacket(this, "$815", 1)); // 同族を殺したものは、己の血でその罪をあがなうことになるだろう。
					break;
				}
			}
		}
		if (targetPlayer != null) {
			_hateList.add(targetPlayer, 0);
			_target = targetPlayer;
		}
	}

	// リンクの設定
	@Override
	public void setLink(LsimulatorCharacter cha) {
		if ((cha != null) && _hateList.isEmpty()) { // ターゲットがいない場合のみ追加
			_hateList.add(cha, 0);
			checkTarget();
		}
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onAction(LsimulatorPcInstance pc) {
		onAction(pc, 0);
	}

	public void doGDropItem(int timer) {
		GDropItemTask task = new GDropItemTask();
		GeneralThreadPool.getInstance().schedule(task, timer * 60000);
	}

	private class GDropItemTask implements Runnable {
		int npcId = getNpcTemplate().get_npcId();

		private GDropItemTask() {
		}

		@Override
		public void run() {
			try {
				if (GDROPITEM_TIME > 0 && !isDropitems()) {
					if (npcId == 70848) { // 安特
						if (!_inventory.checkItem(40505)
								&& !_inventory.checkItem(40506)
								&& !_inventory.checkItem(40507)) {
							_inventory.storeItem(40506, 1);
							_inventory.storeItem(40507, 66);
							_inventory.storeItem(40505, 8);
						}
					}
					if (npcId == 70850) { // 潘
						if (!_inventory.checkItem(40519)) {
							_inventory.storeItem(40519, 30);
						}
					}
					setDropItems(true);
					giveDropItems(true);
					doGDropItem(GDROPITEM_TIME);
				} else {
					giveDropItems(false);
				}
			} catch (Exception e) {
				_log.log(Level.SEVERE, "資料載入錯誤", e);
			}
		}
	}

	@Override
	public void onAction(LsimulatorPcInstance pc, int skillId) {
		if ((pc.getType() == 2) && (pc.getCurrentWeapon() == 0) && pc.isElf()) {
			LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);

			if (attack.calcHit()) {
				try {
					int chance = 0;
					int npcId = getNpcTemplate().get_npcId();
					String npcName = getNpcTemplate().get_name();
					String itemName = "";
					int itemCount = 0;
					LsimulatorItem item40499 = ItemTable.getInstance().getTemplate(
							40499);
					LsimulatorItem item40503 = ItemTable.getInstance().getTemplate(
							40503);
					LsimulatorItem item40505 = ItemTable.getInstance().getTemplate(
							40505);
					LsimulatorItem item40506 = ItemTable.getInstance().getTemplate(
							40506);
					LsimulatorItem item40507 = ItemTable.getInstance().getTemplate(
							40507);
					LsimulatorItem item40519 = ItemTable.getInstance().getTemplate(
							40519);
					if (npcId == 70848) { // 安特
						if (_inventory.checkItem(40499)
								&& !_inventory.checkItem(40505)) { // 蘑菇汁 換
																	// 安特之樹皮
							itemName = item40505.getName();
							itemCount = _inventory.countItems(40499);
							if (itemCount > 1) {
								itemName += " (" + itemCount + ")";
							}
							_inventory.consumeItem(40499, itemCount);
							pc.getInventory().storeItem(40505, itemCount);
							pc.sendPackets(new S_ServerMessage(
									LsimulatorSystemMessageId.$143, npcName, itemName));
							if (!isDropitems()) {
								doGDropItem(3);
							}
						}
						if (_inventory.checkItem(40505)) { // 安特之樹皮
							chance = Random.nextInt(100) + 1;
							if (chance <= 60 && chance >= 50) {
								itemName = item40505.getName();
								_inventory.consumeItem(40505, 1);
								pc.getInventory().storeItem(40505, 1);
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$143, npcName,
										itemName));
							} else {
								itemName = item40499.getName();
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$337, itemName));
							}
						} else if (_inventory.checkItem(40507)
								&& !_inventory.checkItem(40505)) { // 安特之樹枝
							chance = Random.nextInt(100) + 1;
							if (chance <= 40 && chance >= 25) {
								itemName = item40507.getName();
								itemName += " (6)";
								_inventory.consumeItem(40507, 6);
								pc.getInventory().storeItem(40507, 6);
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$143, npcName,
										itemName));
							} else {
								itemName = item40499.getName();
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$337, itemName));
							}
						} else if (_inventory.checkItem(40506)
								&& !_inventory.checkItem(40507)) { // 安特的水果
							chance = Random.nextInt(100) + 1;
							if (chance <= 90 && chance >= 85) {
								itemName = item40506.getName();
								_inventory.consumeItem(40506, 1);
								pc.getInventory().storeItem(40506, 1);
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$143, npcName,
										itemName));
							} else {
								itemName = item40499.getName();
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$337, itemName));
							}
						} else {
							if (!forDropitems()) {
								setDropItems(false);
								doGDropItem(GDROPITEM_TIME);
							}
							chance = Random.nextInt(100) + 1;
							if (chance <= 80 && chance >= 40) {
								broadcastPacket(new S_NpcChatPacket(_npc,
										"$822", 0));
							} else {
								itemName = item40499.getName();
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$337, itemName));
							}
						}
					}
					if (npcId == 70850) { // 潘
						if (_inventory.checkItem(40519)) { // 潘的鬃毛
							chance = Random.nextInt(100) + 1;
							if (chance <= 25) {
								itemName = item40519.getName();
								itemName += " (5)";
								_inventory.consumeItem(40519, 5);
								pc.getInventory().storeItem(40519, 5);
								pc.sendPackets(new S_ServerMessage(
										LsimulatorSystemMessageId.$143, npcName,
										itemName));
							}
						} else {
							if (!forDropitems()) {
								setDropItems(false);
								doGDropItem(GDROPITEM_TIME);
							}
							chance = Random.nextInt(100) + 1;
							if (chance <= 80 && chance >= 40) {
								broadcastPacket(new S_NpcChatPacket(_npc,
										"$824", 0));
							}
						}
					}
					if (npcId == 70846) { // 芮克妮
						if (_inventory.checkItem(40507)) { // 安特之樹枝 換 芮克妮的網
							itemName = item40503.getName();
							itemCount = _inventory.countItems(40507);
							if (itemCount > 1) {
								itemName += " (" + itemCount + ")";
							}
							_inventory.consumeItem(40507, itemCount);
							pc.getInventory().storeItem(40503, itemCount);
							pc.sendPackets(new S_ServerMessage(
									LsimulatorSystemMessageId.$143, npcName, itemName));
						} else {
							itemName = item40507.getName();
							pc.sendPackets(new S_ServerMessage(
									LsimulatorSystemMessageId.$337, itemName)); // \\f1%0不足%s。
						}
					}
				} catch (Exception e) {
					_log.log(Level.SEVERE, "發生錯誤", e);
				}
				attack.calcDamage();
				attack.calcStaffOfMana();
				attack.addPcPoisonAttack(pc, this);
				attack.addChaserAttack();
			}
			attack.action();
			attack.commit();
		} else if ((getCurrentHp() > 0) && !isDead()) {
			LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);
			if (attack.calcHit()) {
				attack.calcDamage();
				attack.calcStaffOfMana();
				attack.addPcPoisonAttack(pc, this);
				attack.addChaserAttack();
			}
			attack.action();
			attack.commit();
		}
	}

	@Override
	public void onTalkAction(LsimulatorPcInstance player) {
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());
		LsimulatorObject object = LsimulatorWorld.getInstance().findObject(getId());
		LsimulatorNpcInstance target = (LsimulatorNpcInstance) object;

		if (talking != null) {
			int pcx = player.getX(); // PCのX座標
			int pcy = player.getY(); // PCのY座標
			int npcx = target.getX(); // NPCのX座標
			int npcy = target.getY(); // NPCのY座標

			if ((pcx == npcx) && (pcy < npcy)) {
				setHeading(0);
			} else if ((pcx > npcx) && (pcy < npcy)) {
				setHeading(1);
			} else if ((pcx > npcx) && (pcy == npcy)) {
				setHeading(2);
			} else if ((pcx > npcx) && (pcy > npcy)) {
				setHeading(3);
			} else if ((pcx == npcx) && (pcy > npcy)) {
				setHeading(4);
			} else if ((pcx < npcx) && (pcy > npcy)) {
				setHeading(5);
			} else if ((pcx < npcx) && (pcy == npcy)) {
				setHeading(6);
			} else if ((pcx < npcx) && (pcy < npcy)) {
				setHeading(7);
			}
			broadcastPacket(new S_ChangeHeading(this));

			// html表示パケット送信
			if (player.getLawful() < -1000) { // プレイヤーがカオティック
				player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
			} else {
				player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
			}

			// 動かないようにする
			synchronized (this) {
				if (_monitor != null) {
					_monitor.cancel();
				}
				setRest(true);
				_monitor = new RestMonitor();
				_restTimer.schedule(_monitor, REST_MILLISEC);
			}
		}
	}

	@Override
	public void receiveDamage(LsimulatorCharacter attacker, int damage) { // 攻撃でＨＰを減らすときはここを使用
		if ((attacker instanceof LsimulatorPcInstance) && (damage > 0)) {
			LsimulatorPcInstance pc = (LsimulatorPcInstance) attacker;
			if ((pc.getType() == 2) && // 素手ならダメージなし
					(pc.getCurrentWeapon() == 0)) {
			} else {
				if ((getCurrentHp() > 0) && !isDead()) {
					if (damage >= 0) {
						setHate(attacker, damage);
					}
					if (damage > 0) {
						removeSkillEffect(FOG_OF_SLEEPING);
					}
					onNpcAI();
					// 仲間意識をもつモンスターのターゲットに設定
					serchLink(pc, getNpcTemplate().get_family());
					if (damage > 0) {
						pc.setPetTarget(this);
					}

					int newHp = getCurrentHp() - damage;
					if ((newHp <= 0) && !isDead()) {
						setCurrentHpDirect(0);
						setDead(true);
						setStatus(ActionCodes.ACTION_Die);
						_lastattacker = attacker;
						Death death = new Death();
						GeneralThreadPool.getInstance().execute(death);
					}
					if (newHp > 0) {
						setCurrentHp(newHp);
					}
				} else if (!isDead()) { // 念のため
					setDead(true);
					setStatus(ActionCodes.ACTION_Die);
					_lastattacker = attacker;
					Death death = new Death();
					GeneralThreadPool.getInstance().execute(death);
				}
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

		if (getMaxHp() > getCurrentHp()) {
			startHpRegeneration();
		}
	}

	@Override
	public void setCurrentMp(int i) {
		int currentMp = i;
		if (currentMp >= getMaxMp()) {
			currentMp = getMaxMp();
		}
		setCurrentMpDirect(currentMp);

		if (getMaxMp() > getCurrentMp()) {
			startMpRegeneration();
		}
	}

	private LsimulatorCharacter _lastattacker;

	class Death implements Runnable {
		LsimulatorCharacter lastAttacker = _lastattacker;

		@Override
		public void run() {
			setDeathProcessing(true);
			setCurrentHpDirect(0);
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);
			int targetobjid = getId();
			getMap().setPassable(getLocation(), true);
			broadcastPacket(new S_DoActionGFX(targetobjid,
					ActionCodes.ACTION_Die));

			LsimulatorPcInstance player = null;
			if (lastAttacker instanceof LsimulatorPcInstance) {
				player = (LsimulatorPcInstance) lastAttacker;
			} else if (lastAttacker instanceof LsimulatorPetInstance) {
				player = (LsimulatorPcInstance) ((LsimulatorPetInstance) lastAttacker)
						.getMaster();
			} else if (lastAttacker instanceof LsimulatorSummonInstance) {
				player = (LsimulatorPcInstance) ((LsimulatorSummonInstance) lastAttacker)
						.getMaster();
			}
			if (player != null) {
				List<LsimulatorCharacter> targetList = _hateList.toTargetArrayList();
				List<Integer> hateList = _hateList.toHateArrayList();
				int exp = getExp();
				CalcExp.calcExp(player, targetobjid, targetList, hateList, exp);

				List<LsimulatorCharacter> dropTargetList = _dropHateList
						.toTargetArrayList();
				List<Integer> dropHateList = _dropHateList.toHateArrayList();
				try {
					DropTable.getInstance().dropShare(_npc, dropTargetList, dropHateList);
				} catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
				// カルマは止めを刺したプレイヤーに設定。ペットorサモンで倒した場合も入る。
				player.addKarma((int) (getKarma() * Config.RATE_KARMA));
			}
			setDeathProcessing(false);

			setKarma(0);
			setExp(0);
			allTargetClear();

			startDeleteTimer();
		}
	}

	@Override
	public void onFinalAction(LsimulatorPcInstance player, String action) {
	}

	public void doFinalAction(LsimulatorPcInstance player) {
	}

	private static final long REST_MILLISEC = 10000;

	private static final Timer _restTimer = new Timer(true);

	private RestMonitor _monitor;

	public class RestMonitor extends TimerTask {
		@Override
		public void run() {
			setRest(false);
		}
	}
}
