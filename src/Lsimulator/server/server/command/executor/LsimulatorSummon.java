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
package Lsimulator.server.server.command.executor;

import java.util.StringTokenizer;

import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorSummonInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorSummon implements LsimulatorCommandExecutor {
	private LsimulatorSummon() {
	}

	public static LsimulatorSummon getInstance() {
		return new LsimulatorSummon();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			String nameid = tok.nextToken();
			int npcid = 0;
			try {
				npcid = Integer.parseInt(nameid);
			}
			catch (NumberFormatException e) {
				npcid = NpcTable.getInstance().findNpcIdByNameWithoutSpace(nameid);
				if (npcid == 0) {
					pc.sendPackets(new S_SystemMessage("找不到符合條件的NPC。"));
					return;
				}
			}
			int count = 1;
			if (tok.hasMoreTokens()) {
				count = Integer.parseInt(tok.nextToken());
			}
			LsimulatorNpc npc = NpcTable.getInstance().getTemplate(npcid);
			for (int i = 0; i < count; i++) {
				LsimulatorSummonInstance summonInst = new LsimulatorSummonInstance(npc, pc);
				summonInst.setPetcost(0);
			}
			nameid = NpcTable.getInstance().getTemplate(npcid).get_name();
			pc.sendPackets(new S_SystemMessage(nameid + "(ID:" + npcid + ") (" + count + ") 召喚了。"));
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入" + cmdName + " npcid|name [數量] 。"));
		}
	}
}
