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
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_BLOODSTAIN_OF_FAFURION;

import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.model.LsimulatorDragonSlayer;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class DragonPortalInstance extends NpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param template
	 */
	public DragonPortalInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onTalkAction(PcInstance player) {
		int npcid = getNpcTemplate().get_npcId();
		int portalNumber = getPortalNumber(); // 龍門編號
		int X = 32599;
		int Y = 32742;
		short mapId = 1005;
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(npcid);
		String htmlid = null;
		String[] htmldata = null;
		if ((npcid >= 81273 && npcid <= 81276)) { // 龍之門扉
			if (portalNumber == -1) {
				return;
			}
			mapId = (short) (1005 + portalNumber); // 地圖判斷
			if (LsimulatorDragonSlayer.getInstance().getPlayersCount(portalNumber) >= 32) {
				player.sendPackets(new S_ServerMessage(1536)); // 參與人員已額滿，目前無法再入場。
			} else if (LsimulatorDragonSlayer.getInstance().getDragonSlayerStatus()[portalNumber] >= 5) {
				player.sendPackets(new S_ServerMessage(1537)); // 攻略已經開始，目前無法入場。
			} else {
				if (portalNumber >= 0 && portalNumber <= 5) { // 安塔瑞斯副本
					if (player.hasSkillEffect(EFFECT_BLOODSTAIN_OF_ANTHARAS)) {
						player.sendPackets(new S_ServerMessage(1626)); // 龍之血痕已穿透全身，在血痕的氣味消失之前，無法再進入龍之門扉。
						return;
					}
				} else if (portalNumber >= 6 && portalNumber <= 11) { // 法利昂副本
					if (player.hasSkillEffect(EFFECT_BLOODSTAIN_OF_FAFURION)) {
						player.sendPackets(new S_ServerMessage(1626)); // 龍之血痕已穿透全身，在血痕的氣味消失之前，無法再進入龍之門扉。
						return;
					}
					X = 32927;
					Y = 32741;
				}
				player.setPortalNumber(portalNumber);
				LsimulatorDragonSlayer.getInstance().addPlayerList(player, portalNumber);
				LsimulatorTeleport.teleport(player, X, Y, mapId, 2, true);
			}
		}
		else if (npcid == 81301) { // 傳送進入安塔瑞斯棲息地
			LsimulatorDragonSlayer.getInstance().startDragonSlayer(player.getPortalNumber());
			LsimulatorTeleport.teleport(player, 32795, 32665, player.getMapId(), 4, true);
		}
		else if (npcid == 81302) { // 傳送出去安塔瑞斯棲息地
			LsimulatorTeleport.teleport(player, 32700, 32671, player.getMapId(), 6, true);
		}
		else if (npcid == 81303) { // 傳送進入法利昂棲息地
			LsimulatorDragonSlayer.getInstance().startDragonSlayer(player.getPortalNumber());
			LsimulatorTeleport.teleport(player, 32988, 32843, player.getMapId(), 6, true);
		}
		else if (npcid == 81304) { // 傳送出去法利昂棲息地
			LsimulatorTeleport.teleport(player, 32937, 32672, player.getMapId(), 6, true);
		}
		else if (npcid == 81305) { // 傳送進入安塔瑞斯洞穴
		}
		else if (npcid == 81306) { // 傳送到安塔瑞斯 洞穴入口(階段型)
			LsimulatorTeleport.teleport(player, 32677, 32746, player.getMapId(), 6, true);
		}
		else if (npcid == 81277) { // 隱匿的巨龍谷入口
			int playerLv = player.getLevel();//角色等級
			if (playerLv >= 30 && playerLv <= 51) {
				htmlid = "dsecret1";
			} else if (playerLv >= 52) {
				htmlid = "dsecret2";
			} else {
				htmlid = "dsecret3";
			}
		}

		if (htmlid != null) {
			player.sendPackets(new S_NPCTalkReturn(objid, htmlid, htmldata));
		} else {
			player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
		}
	}
}
