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

import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

/**
 * GM指令：全體聊天
 */
public class LsimulatorChat implements LsimulatorCommandExecutor {
	private LsimulatorChat() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorChat();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			if (st.hasMoreTokens()) {
				String flag = st.nextToken();
				String msg;
				if (flag.compareToIgnoreCase("on") == 0) {
					LsimulatorWorld.getInstance().set_worldChatElabled(true);
					msg = "開啟全體聊天。";
				}
				else if (flag.compareToIgnoreCase("off") == 0) {
					LsimulatorWorld.getInstance().set_worldChatElabled(false);
					msg = "關閉全體聊天。";
				}
				else {
					throw new Exception();
				}
				pc.sendPackets(new S_SystemMessage(msg));
			}
			else {
				String msg;
				if (LsimulatorWorld.getInstance().isWorldChatElabled()) {
					msg = "全體聊天已開啟。.chat off 能使其關閉。";
				}
				else {
					msg = "全體聊天已關閉。.chat on 能使其開啟。";
				}
				pc.sendPackets(new S_SystemMessage(msg));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 " + cmdName + " [on|off]"));
		}
	}
}
