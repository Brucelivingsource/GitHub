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
package Lsimulator.server.server.model.item.action;

import Lsimulator.server.Config;
import Lsimulator.server.server.datatables.MagicDollTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.Instance.LsimulatorDollInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_OwnCharStatus;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SkillIconGFX;
import Lsimulator.server.server.serverpackets.S_SkillSound;
import Lsimulator.server.server.templates.LsimulatorMagicDoll;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class MagicDoll {

	public static void useMagicDoll(LsimulatorPcInstance pc, int itemId, int itemObjectId) {
		LsimulatorMagicDoll magic_doll = MagicDollTable.getInstance().getTemplate((itemId));
		if (magic_doll != null) {
			boolean isAppear = true;
			LsimulatorDollInstance doll = null;

			for (LsimulatorDollInstance curdoll : pc.getDollList().values()) {
				doll = curdoll;
				if (doll.getItemObjId() == itemObjectId) {
					isAppear = false;
					break;
				}
			}

			if (isAppear) {
				if (!pc.getInventory().checkItem(41246, 50)) {
					pc.sendPackets(new S_ServerMessage(337, "$5240")); // 魔法結晶體不足
					return;
				}
				if (pc.getDollList().size() >= Config.MAX_DOLL_COUNT) {
					pc.sendPackets(new S_ServerMessage(79)); // 沒有任何事情發生
					return;
				}
				int npcId = magic_doll.getDollId();

				LsimulatorNpc template = NpcTable.getInstance().getTemplate(npcId);
				doll = new LsimulatorDollInstance(template, pc, itemId, itemObjectId);
				pc.sendPackets(new S_SkillSound(doll.getId(), 5935));
				pc.broadcastPacket(new S_SkillSound(doll.getId(), 5935));
				pc.sendPackets(new S_SkillIconGFX(56, 1800));
				pc.sendPackets(new S_OwnCharStatus(pc));
				pc.getInventory().consumeItem(41246, 50);
			} else {
				pc.sendPackets(new S_SkillSound(doll.getId(), 5936));
				pc.broadcastPacket(new S_SkillSound(doll.getId(), 5936));
				doll.deleteDoll();
				pc.sendPackets(new S_SkillIconGFX(56, 0));
				pc.sendPackets(new S_OwnCharStatus(pc));
			}
		}
	}

}
