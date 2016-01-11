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
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.types.Point;

public class GuardInstance extends NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ターゲットを探す
	@Override
	public void searchTarget() {
		// ターゲット捜索
		PcInstance targetPlayer = null;
		for (PcInstance pc : LsimulatorWorld.getInstance().getVisiblePlayer(this)) {
			if ((pc.getCurrentHp() <= 0) || pc.isDead() || pc.isGm() || pc.isGhost()) {
				continue;
			}
			if (!pc.isInvisble() || getNpcTemplate().is_agrocoi()) // インビジチェック
			{
				if (pc.isWanted()) { // PKで手配中か
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

	public void setTarget(PcInstance targetPlayer) {
		if (targetPlayer != null) {
			_hateList.add(targetPlayer, 0);
			_target = targetPlayer;
		}
	}

	// ターゲットがいない場合の処理
	@Override
	public boolean noTarget() {
		if (getLocation().getTileLineDistance(new Point(getHomeX(), getHomeY())) > 0) {
			int dir = moveDirection(getHomeX(), getHomeY());
			if (dir != -1) {
				setDirectionMove(dir);
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			}
			else // 遠すぎるor経路が見つからない場合はテレポートして帰る
			{
				teleport(getHomeX(), getHomeY(), 1);
			}
		}
		else {
			if (LsimulatorWorld.getInstance().getRecognizePlayer(this).size() == 0) {
				return true; // 周りにプレイヤーがいなくなったらＡＩ処理終了
			}
		}
		return false;
	}

	public GuardInstance(LsimulatorNpc template) {
		super(template);
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
	public void onAction(PcInstance pc) {
		onAction(pc, 0);
	}

	@Override
	public void onAction(PcInstance pc, int skillId) {
		if (!isDead()) {
			if (getCurrentHp() > 0) {
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
			else {
				LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);
				attack.calcHit();
				attack.action();
			}
		}
	}

	@Override
	public void onTalkAction(PcInstance player) {
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		String[] htmldata = null;
		boolean hascastle = false;
		String clan_name = "";
		String pri_name = "";

		if (talking != null) {
			// キーパー
			if ((npcid == 70549) || // ケント城左外門キーパー
					(npcid == 70985)) { // ケント城右外門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.KENT_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gateokeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if (npcid == 70656) { // ケント城内門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.KENT_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gatekeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if ((npcid == 70600) || // オークの森外門キーパー
					(npcid == 70986)) {
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.OT_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "orckeeper";
				}
				else {
					htmlid = "orckeeperop";
				}
			}
			else if ((npcid == 70687) || // ウィンダウッド城外門キーパー
					(npcid == 70987)) {
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.WW_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gateokeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if (npcid == 70778) { // ウィンダウッド城内門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.WW_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gatekeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if ((npcid == 70800) || // ギラン城外門キーパー
					(npcid == 70988) || (npcid == 70989) || (npcid == 70990) || (npcid == 70991)) {
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.GIRAN_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gateokeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if (npcid == 70817) { // ギラン城内門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.GIRAN_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gatekeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if ((npcid == 70862) || // ハイネ城外門キーパー
					(npcid == 70992)) {
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.HEINE_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gateokeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if (npcid == 70863) { // ハイネ城内門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.HEINE_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gatekeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if ((npcid == 70993) || // ドワーフ城外門キーパー
					(npcid == 70994)) {
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.DOWA_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gateokeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if (npcid == 70995) { // ドワーフ城内門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.DOWA_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gatekeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}
			else if (npcid == 70996) { // アデン城内門キーパー
				hascastle = checkHasCastle(player, LsimulatorCastleLocation.ADEN_CASTLE_ID);
				if (hascastle) { // 城主クラン員
					htmlid = "gatekeeper";
					htmldata = new String[]
					{ player.getName() };
				}
				else {
					htmlid = "gatekeeperop";
				}
			}

			// 近衛兵
			else if (npcid == 60514) { // ケント城近衛兵
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.KENT_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "ktguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if (npcid == 60560) { // オーク近衛兵
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.OT_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "orcguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if (npcid == 60552) { // ウィンダウッド城近衛兵
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.WW_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "wdguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if ((npcid == 60524) || // ギラン街入り口近衛兵(弓)
					(npcid == 60525) || // ギラン街入り口近衛兵
					(npcid == 60529)) { // ギラン城近衛兵
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.GIRAN_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "grguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if (npcid == 70857) { // ハイネ城ハイネ ガード
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.HEINE_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "heguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if ((npcid == 60530) || // ドワーフ城ドワーフ ガード
					(npcid == 60531)) {
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.DOWA_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "dcguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if ((npcid == 60533) || // アデン城 ガード
					(npcid == 60534)) {
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.ADEN_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "adguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}
			else if (npcid == 81156) { // アデン偵察兵（ディアド要塞）
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
					if (clan.getCastleId() // 城主クラン
					== LsimulatorCastleLocation.DIAD_CASTLE_ID) {
						clan_name = clan.getClanName();
						pri_name = clan.getLeaderName();
						break;
					}
				}
				htmlid = "ktguard6";
				htmldata = new String[]
				{ getName(), clan_name, pri_name };
			}

			// html表示パケット送信
			if (htmlid != null) { // htmlidが指定されている場合
				if (htmldata != null) { // html指定がある場合は表示
					player.sendPackets(new S_NPCTalkReturn(objid, htmlid, htmldata));
				}
				else {
					player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
				}
			}
			else {
				if (player.getLawful() < -1000) { // プレイヤーがカオティック
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				}
				else {
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		}
	}

	public void onFinalAction() {

	}

	public void doFinalAction() {

	}

	@Override
	public void setLink(LsimulatorCharacter cha) {
		if ((cha != null) && _hateList.isEmpty()) {
			_hateList.add(cha, 0);
			checkTarget();
		}
	}

	@Override
	public void receiveDamage(LsimulatorCharacter attacker, int damage) { // 攻撃でＨＰを減らすときはここを使用
		if ((getCurrentHp() > 0) && !isDead()) {
			if (damage >= 0) {
				if (!(attacker instanceof EffectInstance)) { // FWはヘイトなし
					setHate(attacker, damage);
				}
			}
			if (damage > 0) {
				removeSkillEffect(FOG_OF_SLEEPING);
			}

			onNpcAI();

			if ((attacker instanceof PcInstance) && (damage > 0)) {
				PcInstance pc = (PcInstance) attacker;
				pc.setPetTarget(this);
				serchLink(pc, getNpcTemplate().get_family());
			}

			int newHp = getCurrentHp() - damage;
			if ((newHp <= 0) && !isDead()) {
				setCurrentHpDirect(0);
				setDead(true);
				setStatus(ActionCodes.ACTION_Die);
				Death death = new Death(attacker);
				GeneralThreadPool.getInstance().execute(death);
			}
			if (newHp > 0) {
				setCurrentHp(newHp);
			}
		}
		else if ((getCurrentHp() == 0) && !isDead()) {}
		else if (!isDead()) { // 念のため
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);
			Death death = new Death(attacker);
			GeneralThreadPool.getInstance().execute(death);
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

			startChat(CHAT_TIMING_DEAD);

			setDeathProcessing(false);

			allTargetClear();

			startDeleteTimer();
		}
	}

	private boolean checkHasCastle(PcInstance pc, int castleId) {
		boolean isExistDefenseClan = false;
		for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
			if (castleId == clan.getCastleId()) {
				isExistDefenseClan = true;
				break;
			}
		}
		if (!isExistDefenseClan) { // 城主クランが居ない
			return true;
		}

		if (pc.getClanid() != 0) { // クラン所属中
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (clan.getCastleId() == castleId) {
					return true;
				}
			}
		}
		return false;
	}

}
