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

import Lsimulator.server.server.datatables.ExpTable;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.utils.IntRange;

public class LsimulatorLevel implements LsimulatorCommandExecutor {
	private LsimulatorLevel() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorLevel();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			int level = Integer.parseInt(tok.nextToken());
			if (level == pc.getLevel()) {
				return;
			}
			if (!IntRange.includes(level, 1, 99)) {
				pc.sendPackets(new S_SystemMessage("請在1-99範圍內指定"));
				return;
			}
			pc.setExp(ExpTable.getExpByLevel(level));
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 : " + cmdName + " lv "));
		}
	}
}
