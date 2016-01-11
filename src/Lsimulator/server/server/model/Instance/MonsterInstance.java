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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_BLOODSTAIN_OF_ANTHARAS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FOG_OF_SLEEPING;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.DropTable;
import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.SprTable;
import Lsimulator.server.server.datatables.UBTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorDragonSlayer;
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorUltimateBattle;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.skill.LsimulatorBuffUtil;
import Lsimulator.server.server.serverpackets.S_ChangeName;
import Lsimulator.server.server.serverpackets.S_CharVisualUpdate;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.serverpackets.S_NpcChangeShape;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SkillBrave;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.CalcExp;
import Lsimulator.server.server.utils.Random;

public class MonsterInstance extends NpcInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger.getLogger(MonsterInstance.class
			.getName());

	private boolean _storeDroped; // ドロップアイテムの読込が完了したか

	private boolean isDoppel;

	// アイテム使用処理
	@Override
	public void onItemUse() {
		if (!isActived() && (_target != null)) {
			useItem(USEITEM_HASTE, 40); // ４０％使用加速藥水
			// 變形判斷
			onDoppel(true);
		}
		if (getCurrentHp() * 100 / getMaxHp() < 40) { // ＨＰが４０％きったら
			useItem(USEITEM_HEAL, 50); // ５０％の確率で回復ポーション使用
		}
	}

	// 變形怪變成玩家判斷
	@Override
	public void onDoppel(boolean isChangeShape) {
		if (getNpcTemplate().is_doppel()) {
			boolean updateObject = false;

			if (!isChangeShape) { // 復原
				updateObject = true;
				// setName(getNpcTemplate().get_name());
				// setNameId(getNpcTemplate().get_nameid());
				setTempLawful(getNpcTemplate().get_lawful());
				setGfxId(getNpcTemplate().get_gfxid());
				setTempCharGfx(getNpcTemplate().get_gfxid());
			} else if (!isDoppel && (_target instanceof PcInstance)) { // 未變形
				setSleepTime(300);
				PcInstance targetPc = (PcInstance) _target;
				isDoppel = true;
				updateObject = true;
				setName(targetPc.getName());
				setNameId(targetPc.getName());
				setTempLawful(targetPc.getLawful());
				setGfxId(targetPc.getClassId());
				setTempCharGfx(targetPc.getClassId());

				if (targetPc.getClassId() != 6671) { // 非幻術師拿劍
					setStatus(4);
				} else { // 幻術師拿斧頭
					setStatus(11);
				}
			}
			// 移動、攻擊速度
			setPassispeed(SprTable.getInstance().getMoveSpeed(getTempCharGfx(),
					getStatus()));
			setAtkspeed(SprTable.getInstance().getAttackSpeed(getTempCharGfx(),
					getStatus() + 1));
			// 變形
			if (updateObject) {
				for (PcInstance pc : LsimulatorWorld.getInstance()
						.getRecognizePlayer(this)) {
					if (!isChangeShape) {
						pc.sendPackets(new S_ChangeName(getId(),getNpcTemplate().get_nameid()));
					} else {
						pc.sendPackets(new S_ChangeName(getId(), getNameId()));
					}
					pc.sendPackets(new S_NpcChangeShape(getId(), getGfxId(),getTempLawful(), getStatus()));
				}
			}
		}
	}

	@Override
	public void onPerceive(PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		if (0 < getCurrentHp()) {
			perceivedFrom.sendPackets(new S_NPCPack(this));
			onNpcAI(); // モンスターのＡＩを開始
			if (getBraveSpeed() == 1) { // 二段加速狀態
				perceivedFrom.sendPackets(new S_SkillBrave(getId(), 1, 600000));
				setBraveSpeed(1);
			}
		} else {
			// 水龍 階段一、二 死亡隱形
			if (getGfxId() != 7864 && getGfxId() != 7869) {
				perceivedFrom.sendPackets(new S_NPCPack(this));
			}
		}
	}

	// ターゲットを探す
	public static int[][] _classGfxId = { { 0, 1 }, { 48, 61 }, { 37, 138 },
			{ 734, 1186 }, { 2786, 2796 } };

	@Override
	public void searchTarget() {
		// 目標捜索
		PcInstance lastTarget = null;
		PcInstance targetPlayer = null;

		if (_target != null && _target instanceof PcInstance ) {
			lastTarget = (PcInstance) _target;
			tagertClear();
		}

		for (PcInstance pc : LsimulatorWorld.getInstance().getVisiblePlayer(this)) {

			if ( pc == lastTarget || (pc.getCurrentHp() <= 0) || pc.isDead() || pc.isGm()
					|| pc.isMonitor() || pc.isGhost()) {
				continue;
			}

			// 闘技場内は変身／未変身に限らず全てアクティブ
			int mapId = getMapId();
			if ((mapId == 88) || (mapId == 98) || (mapId == 92)
					|| (mapId == 91) || (mapId == 95)) {
				if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) { // インビジチェック
					targetPlayer = pc;
					break;
				}
			}

			if (getNpcId() == 45600) { // カーツ
				if (pc.isCrown() || pc.isDarkelf()
						|| (pc.getTempCharGfx() != pc.getClassId())) { // 未変身の君主、DEにはアクティブ
					targetPlayer = pc;
					break;
				}
			}

			// どちらかの条件を満たす場合、友好と見なされ先制攻撃されない。
			// ・モンスターのカルマがマイナス値（バルログ側モンスター）でPCのカルマレベルが1以上（バルログ友好）
			// ・モンスターのカルマがプラス値（ヤヒ側モンスター）でPCのカルマレベルが-1以下（ヤヒ友好）
			if (((getNpcTemplate().getKarma() < 0) && (pc.getKarmaLevel() >= 1))
					|| ((getNpcTemplate().getKarma() > 0) && (pc
							.getKarmaLevel() <= -1))) {
				continue;
			}
			// 見棄てられた者たちの地 カルマクエストの変身中は、各陣営のモンスターから先制攻撃されない
			if (((pc.getTempCharGfx() == 6034) && (getNpcTemplate().getKarma() < 0))
					|| ((pc.getTempCharGfx() == 6035) && (getNpcTemplate()
							.getKarma() > 0))
					|| ((pc.getTempCharGfx() == 6035) && (getNpcTemplate()
							.get_npcId() == 46070))
					|| ((pc.getTempCharGfx() == 6035) && (getNpcTemplate()
							.get_npcId() == 46072))) {
				continue;
			}

			if (!getNpcTemplate().is_agro() && !getNpcTemplate().is_agrososc()
					&& (getNpcTemplate().is_agrogfxid1() < 0)
					&& (getNpcTemplate().is_agrogfxid2() < 0)) { // 完全なノンアクティブモンスター
				if (pc.getLawful() < -1000) { // プレイヤーがカオティック
					targetPlayer = pc;
					break;
				}
				continue;
			}

			if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) { // インビジチェック
				if (pc.hasSkillEffect(67)) { // 変身してる
					if (getNpcTemplate().is_agrososc()) { // 変身に対してアクティブ
						targetPlayer = pc;
						break;
					}
				} else if (getNpcTemplate().is_agro()) { // アクティブモンスター
					targetPlayer = pc;
					break;
				}

				// 特定のクラスorグラフィックＩＤにアクティブ
				if ((getNpcTemplate().is_agrogfxid1() >= 0)
						&& (getNpcTemplate().is_agrogfxid1() <= 4)) { // クラス指定
					if ((_classGfxId[getNpcTemplate().is_agrogfxid1()][0] == pc
							.getTempCharGfx())
							|| (_classGfxId[getNpcTemplate().is_agrogfxid1()][1] == pc
									.getTempCharGfx())) {
						targetPlayer = pc;
						break;
					}
				} else if (pc.getTempCharGfx() == getNpcTemplate()
						.is_agrogfxid1()) { // グラフィックＩＤ指定
					targetPlayer = pc;
					break;
				}

				if ((getNpcTemplate().is_agrogfxid2() >= 0)
						&& (getNpcTemplate().is_agrogfxid2() <= 4)) { // クラス指定
					if ((_classGfxId[getNpcTemplate().is_agrogfxid2()][0] == pc
							.getTempCharGfx())
							|| (_classGfxId[getNpcTemplate().is_agrogfxid2()][1] == pc
									.getTempCharGfx())) {
						targetPlayer = pc;
						break;
					}
				} else if (pc.getTempCharGfx() == getNpcTemplate()
						.is_agrogfxid2()) { // グラフィックＩＤ指定
					targetPlayer = pc;
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

	public MonsterInstance(LsimulatorNpc template) {
		super(template);
		_storeDroped = false;
	}

	@Override
	public void onNpcAI() {
		if (isAiRunning()) {
			return;
		}
		if (!_storeDroped) // 無駄なオブジェクトＩＤを発行しないようにここでセット
		{
			DropTable.getInstance().setDrop(this, getInventory());
			getInventory().shuffle();
			_storeDroped = true;
		}
		setActived(false);
		startAI();
	}

	@Override
	public void onTalkAction(PcInstance pc) {
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());

		// html表示パケット送信
		if (pc.getLawful() < -1000) { // プレイヤーがカオティック
			pc.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
		} else {
			pc.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
		}
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
				attack.calcStaffOfMana();
				attack.addPcPoisonAttack(pc, this);
				attack.addChaserAttack();
			}
			attack.action();
			attack.commit();
		}
	}

	@Override
	public void ReceiveManaDamage(LsimulatorCharacter attacker, int mpDamage) { // 攻撃でＭＰを減らすときはここを使用
		if ((mpDamage > 0) && !isDead()) {
			// int Hate = mpDamage / 10 + 10; // 注意！計算適当 ダメージの１０分の１＋ヒットヘイト１０
			// setHate(attacker, Hate);
			setHate(attacker, mpDamage);

			onNpcAI();

			if (attacker instanceof PcInstance) { // 仲間意識をもつモンスターのターゲットに設定
				serchLink((PcInstance) attacker, getNpcTemplate()
						.get_family());
			}

			int newMp = getCurrentMp() - mpDamage;
			if (newMp < 0) {
				newMp = 0;
			}
			setCurrentMp(newMp);
		}
	}

	@Override
	public void receiveDamage(LsimulatorCharacter attacker, int damage) { // 攻撃でＨＰを減らすときはここを使用
		if ((getCurrentHp() > 0) && !isDead()) {
			if ((getHiddenStatus() == HIDDEN_STATUS_SINK)
					|| (getHiddenStatus() == HIDDEN_STATUS_FLY)) {
				return;
			}
			if (damage >= 0) {
				if (!(attacker instanceof EffectInstance)) { // FWはヘイトなし
					setHate(attacker, damage);
				}
			}
			if (damage > 0) {
				removeSkillEffect(FOG_OF_SLEEPING);
			}

			onNpcAI();

			if (attacker instanceof PcInstance) { // 仲間意識をもつモンスターのターゲットに設定
				serchLink((PcInstance) attacker, getNpcTemplate()
						.get_family());
			}

			// 血痕相剋傷害增加 1.5倍
			if ((getNpcTemplate().get_npcId() == 97044
					|| getNpcTemplate().get_npcId() == 97045 || getNpcTemplate()
					.get_npcId() == 97046)
					&& (attacker.hasSkillEffect(EFFECT_BLOODSTAIN_OF_ANTHARAS))) { // 有安塔瑞斯的血痕時對法利昂增傷
				damage *= 1.5;
			}

			if ((attacker instanceof PcInstance) && (damage > 0)) {
				PcInstance player = (PcInstance) attacker;
				player.setPetTarget(this);
			}

			int newHp = getCurrentHp() - damage;
			if ((newHp <= 0) && !isDead()) {
				int transformId = getNpcTemplate().getTransformId();
				// 変身しないモンスター
				if (transformId == -1) {
					if (getPortalNumber() != -1) {
						if (getNpcTemplate().get_npcId() == 97006
								|| getNpcTemplate().get_npcId() == 97044) {
							// 準備階段二
							LsimulatorDragonSlayer.getInstance().startDragonSlayer2rd(
									getPortalNumber());
						} else if (getNpcTemplate().get_npcId() == 97007
								|| getNpcTemplate().get_npcId() == 97045) {
							// 準備階段三
							LsimulatorDragonSlayer.getInstance().startDragonSlayer3rd(
									getPortalNumber());
						} else if (getNpcTemplate().get_npcId() == 97008
								|| getNpcTemplate().get_npcId() == 97046) {
							bloodstain();
							// 結束屠龍副本
							LsimulatorDragonSlayer.getInstance().endDragonSlayer(
									getPortalNumber());
						}
					}
					setCurrentHpDirect(0);
					setDead(true);
					setStatus(ActionCodes.ACTION_Die);
					openDoorWhenNpcDied(this);
					Death death = new Death(attacker);
					GeneralThreadPool.getInstance().execute(death);
					// Death(attacker);
					if (getPortalNumber() == -1
							&& (getNpcTemplate().get_npcId() == 97006
									|| getNpcTemplate().get_npcId() == 97007
									|| getNpcTemplate().get_npcId() == 97044 || getNpcTemplate()
									.get_npcId() == 97045)) {
						doNextDragonStep(attacker, getNpcTemplate().get_npcId());
					}
				} else { // 変身するモンスター
							// distributeExpDropKarma(attacker);
					transform(transformId);
				}
			}
			if (newHp > 0) {
				setCurrentHp(newHp);
				hide();
			}
		} else if (!isDead()) { // 念のため
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);
			Death death = new Death(attacker);
			GeneralThreadPool.getInstance().execute(death);
			// Death(attacker);
			if (getPortalNumber() == -1
					&& (getNpcTemplate().get_npcId() == 97006
							|| getNpcTemplate().get_npcId() == 97007
							|| getNpcTemplate().get_npcId() == 97044 || getNpcTemplate()
							.get_npcId() == 97045)) {
				doNextDragonStep(attacker, getNpcTemplate().get_npcId());
			}
		}
	}

	private static void openDoorWhenNpcDied(NpcInstance npc) {
		int[] npcId = { 46143, 46144, 46145, 46146, 46147, 46148, 46149, 46150,
				46151, 46152 };
		int[] doorId = { 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5008, 5009,
				5010 };

		for (int i = 0; i < npcId.length; i++) {
			if (npc.getNpcTemplate().get_npcId() == npcId[i]) {
				openDoorInCrystalCave(doorId[i]);
				break;
			}
		}
	}

	private static void openDoorInCrystalCave(int doorId) {
		for (LsimulatorObject object : LsimulatorWorld.getInstance().getObject()) {
			if (object instanceof DoorInstance) {
				DoorInstance door = (DoorInstance) object;
				if (door.getDoorId() == doorId) {
					door.open();
				}
			}
		}
	}

	/**
	 * 距離が5以上離れているpcを距離3～4の位置に引き寄せる。
	 * 
	 * @param pc
	 */
	/*
	 * private void recall(PcInstance pc) { if (getMapId() != pc.getMapId()) {
	 * return; } if (getLocation().getTileLineDistance(pc.getLocation()) > 4) {
	 * for (int count = 0; count < 10; count++) { LsimulatorLocation newLoc =
	 * getLocation().randomLocation(3, 4, false); if (glanceCheck(newLoc.getX(),
	 * newLoc.getY())) { LsimulatorTeleport.teleport(pc, newLoc.getX(), newLoc.getY(),
	 * getMapId(), 5, true); break; } } } }
	 */

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

	class Death implements Runnable {
		LsimulatorCharacter _lastAttacker;

		public Death(LsimulatorCharacter lastAttacker) {
			_lastAttacker = lastAttacker;
		}

		@Override
		public void run() {
			setDeathProcessing(true);
			setCurrentHpDirect(0);
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);

			getMap().setPassable(getLocation(), true);

			broadcastPacket(new S_DoActionGFX(getId(), ActionCodes.ACTION_Die));
			// 變形判斷
			onDoppel(false);

			startChat(CHAT_TIMING_DEAD);

			distributeExpDropKarma(_lastAttacker);
			giveUbSeal();

			setDeathProcessing(false);

			setExp(0);
			setKarma(0);
			allTargetClear();

			startDeleteTimer();
		}
	}

	private void distributeExpDropKarma(LsimulatorCharacter lastAttacker) {
		if (lastAttacker == null) {
			return;
		}
		PcInstance pc = null;
		if (lastAttacker instanceof PcInstance) {
			pc = (PcInstance) lastAttacker;
		} else if (lastAttacker instanceof PetInstance) {
			pc = (PcInstance) ((PetInstance) lastAttacker).getMaster();
		} else if (lastAttacker instanceof SummonInstance) {
			pc = (PcInstance) ((SummonInstance) lastAttacker).getMaster();
		}

		if (pc != null) {
			ArrayList<LsimulatorCharacter> targetList = _hateList.toTargetArrayList();
			ArrayList<Integer> hateList = _hateList.toHateArrayList();
			int exp = getExp();
			CalcExp.calcExp(pc, getId(), targetList, hateList, exp);
			// 死亡した場合はドロップとカルマも分配、死亡せず変身した場合はEXPのみ
			if (isDead()) {
				distributeDrop();
				giveKarma(pc);
			}
		} else if (lastAttacker instanceof EffectInstance) { // FWが倒した場合
			ArrayList<LsimulatorCharacter> targetList = _hateList.toTargetArrayList();
			ArrayList<Integer> hateList = _hateList.toHateArrayList();
			// ヘイトリストにキャラクターが存在する
			if (!hateList.isEmpty()) {
				// 最大ヘイトを持つキャラクターが倒したものとする
				int maxHate = 0;
				for (int i = hateList.size() - 1; i >= 0; i--) {
					if (maxHate < (hateList.get(i))) {
						maxHate = (hateList.get(i));
						lastAttacker = targetList.get(i);
					}
				}
				if (lastAttacker instanceof PcInstance) {
					pc = (PcInstance) lastAttacker;
				} else if (lastAttacker instanceof PetInstance) {
					pc = (PcInstance) ((PetInstance) lastAttacker)
							.getMaster();
				} else if (lastAttacker instanceof SummonInstance) {
					pc = (PcInstance) ((SummonInstance) lastAttacker)
							.getMaster();
				}
				if (pc != null) {
					int exp = getExp();
					CalcExp.calcExp(pc, getId(), targetList, hateList, exp);
					// 死亡した場合はドロップとカルマも分配、死亡せず変身した場合はEXPのみ
					if (isDead()) {
						distributeDrop();
						giveKarma(pc);
					}
				}
			}
		}
	}

	private void distributeDrop() {
		ArrayList<LsimulatorCharacter> dropTargetList = _dropHateList
				.toTargetArrayList();
		ArrayList<Integer> dropHateList = _dropHateList.toHateArrayList();
		try {
			int npcId = getNpcTemplate().get_npcId();
			if ((npcId != 45640)
					|| ((npcId == 45640) && (getTempCharGfx() == 2332))) {
				DropTable.getInstance().dropShare(MonsterInstance.this,
						dropTargetList, dropHateList);
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private void giveKarma(PcInstance pc) {
		int karma = getKarma();
		if (karma != 0) {
			int karmaSign = Integer.signum(karma);
			int pcKarmaLevel = pc.getKarmaLevel();
			int pcKarmaLevelSign = Integer.signum(pcKarmaLevel);
			// カルマ背信行為は5倍
			if ((pcKarmaLevelSign != 0) && (karmaSign != pcKarmaLevelSign)) {
				karma *= 5;
			}
			// カルマは止めを刺したプレイヤーに設定。ペットorサモンで倒した場合も入る。
			pc.addKarma((int) (karma * Config.RATE_KARMA));
		}
	}

	private void giveUbSeal() {
		if (getUbSealCount() != 0) { // UBの勇者の証
			LsimulatorUltimateBattle ub = UBTable.getInstance().getUb(getUbId());
			if (ub != null) {
				for (PcInstance pc : ub.getMembersArray()) {
					if ((pc != null) && !pc.isDead() && !pc.isGhost()) {
						ItemInstance item = pc.getInventory().storeItem(
								41402, getUbSealCount());
						pc.sendPackets(new S_ServerMessage(403, item
								.getLogName())); // %0を手に入れました。
					}
				}
			}
		}
	}

	public boolean is_storeDroped() {
		return _storeDroped;
	}

	public void set_storeDroped(boolean flag) {
		_storeDroped = flag;
	}

	private int _ubSealCount = 0; // UBで倒された時、参加者に与えられる勇者の証の個数

	public int getUbSealCount() {
		return _ubSealCount;
	}

	public void setUbSealCount(int i) {
		_ubSealCount = i;
	}

	private int _ubId = 0; // UBID

	public int getUbId() {
		return _ubId;
	}

	public void setUbId(int i) {
		_ubId = i;
	}

	private void hide() {
		int npcid = getNpcTemplate().get_npcId();
		if ((npcid == 45061 // カーズドスパルトイ
				)
				|| (npcid == 45161 // スパルトイ
				) || (npcid == 45181 // スパルトイ
				) || (npcid == 45455)) { // デッドリースパルトイ
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = Random.nextInt(10);
				if (2 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_SINK);
					broadcastPacket(new S_DoActionGFX(getId(),
							ActionCodes.ACTION_Hide));
					setStatus(11);
					broadcastPacket(new S_CharVisualUpdate(this, getStatus()));
				}
			}
		} else if (npcid == 45682) { // アンタラス
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = Random.nextInt(50);
				if (1 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_SINK);
					broadcastPacket(new S_DoActionGFX(getId(),
							ActionCodes.ACTION_AntharasHide));
					setStatus(20);
					broadcastPacket(new S_CharVisualUpdate(this, getStatus()));
				}
			}
		} else if ((npcid == 45067 // バレーハーピー
				)
				|| (npcid == 45264 // ハーピー
				) || (npcid == 45452 // ハーピー
				) || (npcid == 45090 // バレーグリフォン
				) || (npcid == 45321 // グリフォン
				) || (npcid == 45445)) { // グリフォン
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = Random.nextInt(10);
				if (2 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_FLY);
					broadcastPacket(new S_DoActionGFX(getId(),
							ActionCodes.ACTION_Moveup));
				}
			}
		} else if (npcid == 45681) { // リンドビオル
			if (getMaxHp() / 3 > getCurrentHp()) {
				int rnd = Random.nextInt(50);
				if (1 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_FLY);
					broadcastPacket(new S_DoActionGFX(getId(),
							ActionCodes.ACTION_Moveup));
				}
			}
		} else if ((npcid == 46107 // テーベ マンドラゴラ(白)
				)
				|| (npcid == 46108)) { // テーベ マンドラゴラ(黒)
			if ( ( getMaxHp() >> 2 )  > getCurrentHp()) {
				int rnd = Random.nextInt(10);
				if (2 > rnd) {
					allTargetClear();
					setHiddenStatus(HIDDEN_STATUS_SINK);
					broadcastPacket(new S_DoActionGFX(getId(),
							ActionCodes.ACTION_Hide));
					setStatus(11);
					broadcastPacket(new S_CharVisualUpdate(this, getStatus()));
				}
			}
		}
	}

	public void initHide() {
		// 出現直後の隠れる動作
		// 潜るMOBは一定の確率で地中に潜った状態に、
		// 飛ぶMOBは飛んだ状態にしておく
		int npcid = getNpcTemplate().get_npcId();
		if ((npcid == 45061 // カーズドスパルトイ
				)
				|| (npcid == 45161 // スパルトイ
				) || (npcid == 45181 // スパルトイ
				) || (npcid == 45455)) { // デッドリースパルトイ
			int rnd = Random.nextInt(3);
			if (1 > rnd) {
				setHiddenStatus(HIDDEN_STATUS_SINK);
				setStatus(11);
			}
		} else if ((npcid == 45045 // クレイゴーレム
				)
				|| (npcid == 45126 // ストーンゴーレム
				) || (npcid == 45134 // ストーンゴーレム
				) || (npcid == 45281)) { // ギランストーンゴーレム
			int rnd = Random.nextInt(3);
			if (1 > rnd) {
				setHiddenStatus(HIDDEN_STATUS_SINK);
				setStatus(4);
			}
		} else if ((npcid == 45067 // バレーハーピー
				)
				|| (npcid == 45264 // ハーピー
				) || (npcid == 45452 // ハーピー
				) || (npcid == 45090 // バレーグリフォン
				) || (npcid == 45321 // グリフォン
				) || (npcid == 45445)) { // グリフォン
			setHiddenStatus(HIDDEN_STATUS_FLY);
		} else if (npcid == 45681) { // リンドビオル
			setHiddenStatus(HIDDEN_STATUS_FLY);
		} else if ((npcid == 46107 // テーベ マンドラゴラ(白)
				)
				|| (npcid == 46108)) { // テーベ マンドラゴラ(黒)
			int rnd = Random.nextInt(3);
			if (1 > rnd) {
				setHiddenStatus(HIDDEN_STATUS_SINK);
				setStatus(11);
			}
		} else if ((npcid >= 46125) && (npcid <= 46128)) {
			setHiddenStatus(NpcInstance.HIDDEN_STATUS_ICE);
			setStatus(4);
		}
	}

	public void initHideForMinion(NpcInstance leader) {
		// グループに属するモンスターの出現直後の隠れる動作（リーダーと同じ動作にする）
		int npcid = getNpcTemplate().get_npcId();
		if (leader.getHiddenStatus() == HIDDEN_STATUS_SINK) {
			if ((npcid == 45061 // カーズドスパルトイ
					)
					|| (npcid == 45161 // スパルトイ
					) || (npcid == 45181 // スパルトイ
					) || (npcid == 45455)) { // デッドリースパルトイ
				setHiddenStatus(HIDDEN_STATUS_SINK);
				setStatus(11);
			} else if ((npcid == 45045 // クレイゴーレム
					)
					|| (npcid == 45126 // ストーンゴーレム
					) || (npcid == 45134 // ストーンゴーレム
					) || (npcid == 45281)) { // ギランストーンゴーレム
				setHiddenStatus(HIDDEN_STATUS_SINK);
				setStatus(4);
			} else if ((npcid == 46107 // テーベ マンドラゴラ(白)
					)
					|| (npcid == 46108)) { // テーベ マンドラゴラ(黒)
				setHiddenStatus(HIDDEN_STATUS_SINK);
				setStatus(11);
			}
		} else if (leader.getHiddenStatus() == HIDDEN_STATUS_FLY) {
			if ((npcid == 45067 // バレーハーピー
					)
					|| (npcid == 45264 // ハーピー
					) || (npcid == 45452 // ハーピー
					) || (npcid == 45090 // バレーグリフォン
					) || (npcid == 45321 // グリフォン
					) || (npcid == 45445)) { // グリフォン
				setHiddenStatus(HIDDEN_STATUS_FLY);
				setStatus(4);
			} else if (npcid == 45681) { // リンドビオル
				setHiddenStatus(HIDDEN_STATUS_FLY);
				setStatus(11);
			}
		} else if ((npcid >= 46125) && (npcid <= 46128)) {
			setHiddenStatus(NpcInstance.HIDDEN_STATUS_ICE);
			setStatus(4);
		}
	}

	@Override
	protected void transform(int transformId) {
		super.transform(transformId);

		// DROPの再設定
		getInventory().clearItems();
		DropTable.getInstance().setDrop(this, getInventory());
		getInventory().shuffle();
	}

	private boolean _nextDragonStepRunning = false;

	protected void setNextDragonStepRunning(boolean nextDragonStepRunning) {
		_nextDragonStepRunning = nextDragonStepRunning;
	}

	protected boolean isNextDragonStepRunning() {
		return _nextDragonStepRunning;
	}

	// 龍之血痕
	private void bloodstain() {
		for (PcInstance pc : LsimulatorWorld.getInstance().getVisiblePlayer(this, 50)) {
			if (getNpcTemplate().get_npcId() == 97008) {
				pc.sendPackets(new S_ServerMessage(1580)); // 安塔瑞斯：黑暗的詛咒將會降臨到你們身上！席琳，
															// 我的母親，請讓我安息吧...
				LsimulatorBuffUtil.bloodstain(pc, (byte) 0, 4320, true);
			} else if (getNpcTemplate().get_npcId() == 97046) {
				pc.sendPackets(new S_ServerMessage(1668)); // 法利昂：莎爾...你這個傢伙...怎麼...對得起我的母親...席琳啊...請拿走...我的生命吧...
				LsimulatorBuffUtil.bloodstain(pc, (byte) 1, 4320, true);
			}
		}
	}

	private void doNextDragonStep(LsimulatorCharacter attacker, int npcid) {
		if (!isNextDragonStepRunning()) {
			int[] dragonId = { 97006, 97007, 97044, 97045 };
			int[] nextStepId = { 97007, 97008, 97045, 97046 };
			int nextSpawnId = 0;
			for (int i = 0; i < dragonId.length; i++) {
				if (npcid == dragonId[i]) {
					nextSpawnId = nextStepId[i];
					break;
				}
			}
			if (attacker != null && nextSpawnId > 0) {
				PcInstance _pc = null;
				if (attacker instanceof PcInstance) {
					_pc = (PcInstance) attacker;
				} else if (attacker instanceof PetInstance) {
					PetInstance pet = (PetInstance) attacker;
					LsimulatorCharacter cha = pet.getMaster();
					if (cha instanceof PcInstance) {
						_pc = (PcInstance) cha;
					}
				} else if (attacker instanceof SummonInstance) {
					SummonInstance summon = (SummonInstance) attacker;
					LsimulatorCharacter cha = summon.getMaster();
					if (cha instanceof PcInstance) {
						_pc = (PcInstance) cha;
					}
				}
				if (_pc != null) {
					NextDragonStep nextDragonStep = new NextDragonStep(_pc,
							this, nextSpawnId);
					GeneralThreadPool.getInstance().execute(nextDragonStep);
				}
			}
		}
	}

	class NextDragonStep implements Runnable {
		PcInstance _pc;
		MonsterInstance _mob;
		int _npcid;
		int _transformId;
		int _x;
		int _y;
		int _h;
		short _m;
		LsimulatorLocation _loc = new LsimulatorLocation();

		public NextDragonStep(PcInstance pc, MonsterInstance mob,
				int transformId) {
			_pc = pc;
			_mob = mob;
			_transformId = transformId;
			_x = mob.getX();
			_y = mob.getY();
			_h = mob.getHeading();
			_m = mob.getMapId();
			_loc = mob.getLocation();
		}

		@Override
		public void run() {
			setNextDragonStepRunning(true);
			try {
				Thread.sleep(10500);
				NpcInstance npc = NpcTable.getInstance().newNpcInstance(
						_transformId);
				npc.setId(IdFactory.getInstance().nextId());
				npc.setMap((short) _m);
				npc.setHomeX(_x);
				npc.setHomeY(_y);
				npc.setHeading(_h);
				npc.getLocation().set(_loc);
				npc.getLocation().forward(_h);
				npc.setPortalNumber(getPortalNumber());

				broadcastPacket(new S_NPCPack(npc));
				broadcastPacket(new S_DoActionGFX(npc.getId(),
						ActionCodes.ACTION_Hide));

				LsimulatorWorld.getInstance().storeObject(npc);
				LsimulatorWorld.getInstance().addVisibleObject(npc);
				npc.turnOnOffLight();
				npc.startChat(NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
				setNextDragonStepRunning(false);
			} catch (InterruptedException e) {
			}
		}
	}
}
