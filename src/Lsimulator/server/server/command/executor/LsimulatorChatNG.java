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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CHAT_PROHIBITED;

import java.util.StringTokenizer;

import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SkillIconGFX;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

/**
 * GM指令：禁言
 */
public class LsimulatorChatNG implements LsimulatorCommandExecutor {
	private LsimulatorChatNG() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorChatNG();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String name = st.nextToken();
			int time = Integer.parseInt(st.nextToken());

			PcInstance tg = LsimulatorWorld.getInstance().getPlayer(name);

			if (tg != null) {
				tg.setSkillEffect(STATUS_CHAT_PROHIBITED, time * 60 * 1000);
				tg.sendPackets(new S_SkillIconGFX(36, time * 60));
				tg.sendPackets(new S_ServerMessage(286, String.valueOf(time))); // \f3ゲームに適合しない行動であるため、今後%0分間チャットを禁じます。
				pc.sendPackets(new S_ServerMessage(287, name)); // %0のチャットを禁じました。
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 " + cmdName + " 玩家名稱 時間(分)。"));
		}
	}
}
