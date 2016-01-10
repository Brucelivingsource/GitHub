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

import Lsimulator.server.server.datatables.HouseTable;
import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.templates.LsimulatorHouse;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorHousekeeperInstance extends LsimulatorNpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param template
	 */
	public LsimulatorHousekeeperInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(LsimulatorPcInstance pc) {
		onAction(pc, 0);
	}

	@Override
	public void onAction(LsimulatorPcInstance pc, int skillId) {
		LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);
		attack.calcHit();
		attack.action();
		attack.addChaserAttack();
		attack.calcDamage();
		attack.calcStaffOfMana();
		attack.addPcPoisonAttack(pc, this);
		attack.commit();
	}

	@Override
	public void onTalkAction(LsimulatorPcInstance pc) {
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		String[] htmldata = null;
		boolean isOwner = false;

		if (talking != null) {
			// 話しかけたPCが所有者とそのクラン員かどうか調べる
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				int houseId = clan.getHouseId();
				if (houseId != 0) {
					LsimulatorHouse house = HouseTable.getInstance().getHouseTable(
							houseId);
					if (npcid == house.getKeeperId()) {
						isOwner = true;
					}
				}
			}

			// 所有者とそのクラン員以外なら会話内容を変える
			if (!isOwner) {
				// Housekeeperが属するアジトを取得する
				LsimulatorHouse targetHouse = null;
				for (LsimulatorHouse house : HouseTable.getInstance()
						.getHouseTableList()) {
					if (npcid == house.getKeeperId()) {
						targetHouse = house;
						break;
					}
				}

				// アジトがに所有者が居るかどうか調べる
				boolean isOccupy = false;
				String clanName = null;
				String leaderName = null;
				for (LsimulatorClan targetClan : LsimulatorWorld.getInstance().getAllClans()) {
					if (targetHouse.getHouseId() == targetClan.getHouseId()) {
						isOccupy = true;
						clanName = targetClan.getClanName();
						leaderName = targetClan.getLeaderName();
						break;
					}
				}

				// 会話内容を設定する
				if (isOccupy) { // 所有者あり
					htmlid = "agname";
					htmldata = new String[] { clanName, leaderName,
							targetHouse.getHouseName() };
				} else { // 所有者なし(競売中)
					htmlid = "agnoname";
					htmldata = new String[] { targetHouse.getHouseName() };
				}
			}

			// html表示パケット送信
			if (htmlid != null) { // htmlidが指定されている場合
				if (htmldata != null) { // html指定がある場合は表示
					pc.sendPackets(new S_NPCTalkReturn(objid, htmlid, htmldata));
				} else {
					pc.sendPackets(new S_NPCTalkReturn(objid, htmlid));
				}
			} else {
				if (pc.getLawful() < -1000) { // プレイヤーがカオティック
					pc.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				} else {
					pc.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		}
	}

	@Override
	public void onFinalAction(LsimulatorPcInstance pc, String action) {
	}

	public void doFinalAction(LsimulatorPcInstance pc) {
	}

}
