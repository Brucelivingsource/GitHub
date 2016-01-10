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

import java.util.logging.Logger;

import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_Disconnect;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorSKick implements LsimulatorCommandExecutor {
	private static Logger _log = Logger.getLogger(LsimulatorSKick.class.getName());

	private LsimulatorSKick() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorSKick();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			LsimulatorPcInstance target = LsimulatorWorld.getInstance().getPlayer(arg);
			if (target != null) {
				pc.sendPackets(new S_SystemMessage((new StringBuilder()).append(target.getName()).append("已被您強制踢除遊戲。").toString()));
				// SKTへ移動させる
				target.setX(33080);
				target.setY(33392);
				target.setMap((short) 4);
				target.sendPackets(new S_Disconnect());
				ClientThread targetClient = target.getNetConnection();
				targetClient.kick();
				_log.warning("GM的踢除指令使得(" + targetClient.getAccountName()+ ":" + targetClient.getHostname() + ")的連線被強制中斷。");
			} else {
				pc.sendPackets(new S_SystemMessage("指定的ID不存在。"));
			}
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入: "+cmdName + " 玩家名稱。"));
		}
	}
}
