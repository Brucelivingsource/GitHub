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

import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

/**
 * GM指令：增加金幣
 */
public class LsimulatorAdena implements LsimulatorCommandExecutor {
	private LsimulatorAdena() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorAdena();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer stringtokenizer = new StringTokenizer(arg);
			int count = Integer.parseInt(stringtokenizer.nextToken());

			LsimulatorItemInstance adena = pc.getInventory().storeItem(LsimulatorItemId.ADENA, count);
			if (adena != null) {
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(count).append(" 金幣產生。").toString()));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage((new StringBuilder()).append("請輸入 .adena 數量||.金幣  數量。").toString()));
		}
	}
}
