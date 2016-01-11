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

import Lsimulator.server.server.model.LsimulatorPolyMorph;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorPoly implements LsimulatorCommandExecutor {
	private LsimulatorPoly() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorPoly();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String name = st.nextToken();
			int polyid = Integer.parseInt(st.nextToken());

			PcInstance tg = LsimulatorWorld.getInstance().getPlayer(name);

			if (tg == null) {
				pc.sendPackets(new S_ServerMessage(73, name)); // \f1%0はゲームをしていません。
			}
			else {
				try {
					LsimulatorPolyMorph.doPoly(tg, polyid, 7200, LsimulatorPolyMorph.MORPH_BY_GM);
				}
				catch (Exception exception) {
					pc.sendPackets(new S_SystemMessage("請輸入 .poly 玩家名稱 變身代碼。"));
				}
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " 請輸入  玩家名稱 變身代碼。"));
		}
	}
}
