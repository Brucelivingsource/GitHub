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
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.datatables.NpcSpawnTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.SpawnTable;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.LsimulatorSpawnUtil;

public class LsimulatorInsertSpawn implements LsimulatorCommandExecutor {
	private static Logger _log = Logger
			.getLogger(LsimulatorInsertSpawn.class.getName());

	private LsimulatorInsertSpawn() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorInsertSpawn();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		String msg = null;

		try {
			StringTokenizer tok = new StringTokenizer(arg);
			String type = tok.nextToken();
			int npcId = Integer.parseInt(tok.nextToken().trim());
			LsimulatorNpc template = NpcTable.getInstance().getTemplate(npcId);

			if (template == null) {
				msg = "找不到符合條件的NPC。";
				return;
			}
			if (type.equalsIgnoreCase("mob")) {
				if (!template.getImpl().equals("LsimulatorMonster")) {
					msg = "指定的NPC不是LsimulatorMonster類型。";
					return;
				}
				SpawnTable.storeSpawn(pc, template);
			} else if (type.equalsIgnoreCase("npc")) {
				NpcSpawnTable.getInstance().storeSpawn(pc, template);
			}
			LsimulatorSpawnUtil.spawn(pc, npcId, 0, 0);
			msg = new StringBuilder().append(template.get_name())
					.append(" (" + npcId + ") ").append("新增到資料庫中。").toString();
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
			msg = "請輸入 : "+ cmdName + " mob|npc NPCID 。";
		} finally {
			if (msg != null) {
				pc.sendPackets(new S_SystemMessage(msg));
			}
		}
	}
}
