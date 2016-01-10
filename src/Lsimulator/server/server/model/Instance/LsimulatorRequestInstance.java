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

import java.util.logging.Logger;

import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorRequestInstance extends LsimulatorNpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger _log = Logger.getLogger(LsimulatorRequestInstance.class
			.getName());

	public LsimulatorRequestInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(LsimulatorPcInstance player) {
		int objid = getId();

		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());

		if (talking != null) {
			if (player.getLawful() < -1000) { // プレイヤーがカオティック
				player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
			} else {
				player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
			}
		} else {
			_log.finest("No actions for npc id : " + objid);
		}
	}

	@Override
	public void onFinalAction(LsimulatorPcInstance player, String action) {

	}

	public void doFinalAction(LsimulatorPcInstance player) {

	}
}
