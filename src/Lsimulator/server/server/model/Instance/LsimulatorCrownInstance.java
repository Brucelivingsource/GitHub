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

import java.util.List;

import Lsimulator.server.server.datatables.ClanTable;
import Lsimulator.server.server.datatables.DoorTable;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWar;
import Lsimulator.server.server.model.LsimulatorWarSpawn;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.serverpackets.S_CastleMaster;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorCrownInstance extends LsimulatorNpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LsimulatorCrownInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(LsimulatorPcInstance player) {
		boolean in_war = false;
		if (player.getClanid() == 0) { // クラン未所属
			return;
		}
		String playerClanName = player.getClanname();
		LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(playerClanName);
		if (clan == null) {
			return;
		}
		if (!player.isCrown()) { // 君主以外
			return;
		}
		if ((player.getTempCharGfx() != 0) && // 変身中
				(player.getTempCharGfx() != 1)) {
			return;
		}
		if (player.getId() != clan.getLeaderId()) { // 血盟主以外
			return;
		}
		if (!checkRange(player)) { // クラウンの1セル以内
			return;
		}
		if (clan.getCastleId() != 0) {
			// 城主クラン
			// あなたはすでに城を所有しているので、他の城を取ることは出来ません。
			player.sendPackets(new S_ServerMessage(474));
			return;
		}

		// クラウンの座標からcastle_idを取得
		int castle_id = LsimulatorCastleLocation.getCastleId(getX(), getY(), getMapId());

		// 布告しているかチェック。但し、城主が居ない場合は布告不要
		boolean existDefenseClan = false;
		LsimulatorClan defence_clan = null;
		for (LsimulatorClan defClan : LsimulatorWorld.getInstance().getAllClans()) {
			if (castle_id == defClan.getCastleId()) {
				// 元の城主クラン
				defence_clan = LsimulatorWorld.getInstance().getClan(defClan.getClanName());
				existDefenseClan = true;
				break;
			}
		}
		List<LsimulatorWar> wars = LsimulatorWorld.getInstance().getWarList(); // 全戦争リストを取得
		for (LsimulatorWar war : wars) {
			if (castle_id == war.GetCastleId()) { // 今居る城の戦争
				in_war = war.CheckClanInWar(playerClanName);
				break;
			}
		}
		if (existDefenseClan && (  !in_war )) { // 城主が居て、布告していない場合
			return;
		}

		// clan_dataのhascastleを更新し、キャラクターにクラウンを付ける
		if (existDefenseClan && (defence_clan != null)) { // 元の城主クランが居る
			defence_clan.setCastleId(0);
			ClanTable.getInstance().updateClan(defence_clan);
			LsimulatorPcInstance defence_clan_member[] = defence_clan.getOnlineClanMember();
			for (LsimulatorPcInstance element : defence_clan_member) {
				if (element.getId() == defence_clan.getLeaderId()) { // 元の城主クランの君主
					element.sendPackets(new S_CastleMaster(0, element.getId()));
					element.broadcastPacket(new S_CastleMaster(0, element.getId()));
					break;
				}
			}
		}
		clan.setCastleId(castle_id);
		ClanTable.getInstance().updateClan(clan);
		player.sendPackets(new S_CastleMaster(castle_id, player.getId()));
		player.broadcastPacket(new S_CastleMaster(castle_id, player.getId()));

		// クラン員以外を街に強制テレポート
		int[] loc = new int[3];
		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getAllPlayers()) {
			if ((pc.getClanid() != player.getClanid()) && !pc.isGm()) {

				if (LsimulatorCastleLocation.checkInWarArea(castle_id, pc)) {
					// 旗内に居る
					loc = LsimulatorCastleLocation.getGetBackLoc(castle_id);
					int locx = loc[0];
					int locy = loc[1];
					short mapid = (short) loc[2];
					LsimulatorTeleport.teleport(pc, locx, locy, mapid, 5, true);
				}
			}
		}

		// メッセージ表示
		for (LsimulatorWar war : wars) {
			if (war.CheckClanInWar(playerClanName) && existDefenseClan) {
				// 自クランが参加中で、城主が交代
				war.WinCastleWar(playerClanName);
				break;
			}
		}
		LsimulatorPcInstance[] clanMember = clan.getOnlineClanMember();

		if (clanMember.length > 0) {
			// 城を占拠しました。
			S_ServerMessage s_serverMessage = new S_ServerMessage(643);
			for (LsimulatorPcInstance pc : clanMember) {
				pc.sendPackets(s_serverMessage);
			}
		}

		// クラウンを消す
		deleteMe();

		// タワーを消して再出現させる
		for (LsimulatorObject l1object : LsimulatorWorld.getInstance().getObject()) {
			if (l1object instanceof LsimulatorTowerInstance) {
				LsimulatorTowerInstance tower = (LsimulatorTowerInstance) l1object;
				if (LsimulatorCastleLocation.checkInWarArea(castle_id, tower)) {
					tower.deleteMe();
				}
			}
		}
		LsimulatorWarSpawn warspawn = new LsimulatorWarSpawn();
		warspawn.SpawnTower(castle_id);

		// 城門を元に戻す
		for (LsimulatorDoorInstance door : DoorTable.getInstance().getDoorList()) {
			if (LsimulatorCastleLocation.checkInWarArea(castle_id, door)) {
				door.repairGate();
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
		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}

	private boolean checkRange(LsimulatorPcInstance pc) {
		return ((getX() - 1 <= pc.getX()) && (pc.getX() <= getX() + 1) && (getY() - 1 <= pc.getY()) && (pc.getY() <= getY() + 1));
	}
}
