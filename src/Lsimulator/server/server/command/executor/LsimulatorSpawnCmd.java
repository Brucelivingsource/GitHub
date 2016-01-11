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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.LsimulatorSpawnUtil;

public class LsimulatorSpawnCmd implements LsimulatorCommandExecutor {
	private static Logger _log = Logger.getLogger(LsimulatorSpawnCmd.class.getName());

	private LsimulatorSpawnCmd() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorSpawnCmd();
	}

	private void sendErrorMessage(PcInstance pc, String cmdName) {
		String errorMsg = "請輸入: " + cmdName + " npcid|name [數量] [範圍] 。";
		pc.sendPackets(new S_SystemMessage(errorMsg));
	}

	private int parseNpcId(String nameId) {
		int npcid = 0;
		try {
			npcid = Integer.parseInt(nameId);
		} catch (NumberFormatException e) {
			npcid = NpcTable.getInstance().findNpcIdByNameWithoutSpace(nameId);
		}
		return npcid;
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			String nameId = tok.nextToken();
			int count = 1;
			if (tok.hasMoreTokens()) {
				count = Integer.parseInt(tok.nextToken());
			}
			int randomrange = 0;
			if (tok.hasMoreTokens()) {
				randomrange = Integer.parseInt(tok.nextToken(), 10);
			}
			int npcid = parseNpcId(nameId);

			LsimulatorNpc npc = NpcTable.getInstance().getTemplate(npcid);
			if (npc == null) {
				pc.sendPackets(new S_SystemMessage("找不到符合條件的NPC。"));
				return;
			}
			for (int i = 0; i < count; i++) {
				LsimulatorSpawnUtil.spawn(pc, npcid, randomrange, 0);
			}
			String msg = String.format("%s(%d) (%d) 召喚了。 (範圍:%d)", npc
					.get_name(), npcid, count, randomrange);
			pc.sendPackets(new S_SystemMessage(msg));
		} catch (NoSuchElementException e) {
			sendErrorMessage(pc, cmdName);
		} catch (NumberFormatException e) {
			sendErrorMessage(pc, cmdName);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			pc.sendPackets(new S_SystemMessage(cmdName + " 内部錯誤。"));
		}
	}
}
