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

import Lsimulator.server.server.Account;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_Disconnect;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

/**
 * GM指令：踢掉且禁止帳號登入
 */
public class LsimulatorAccountBanKick implements LsimulatorCommandExecutor {
	private LsimulatorAccountBanKick() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorAccountBanKick();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			PcInstance target = LsimulatorWorld.getInstance().getPlayer(arg);

			if (target != null) {
				// アカウントをBANする
				Account.ban(target.getAccountName());
				pc.sendPackets(new S_SystemMessage(target.getName() + "被您強制踢除遊戲並封鎖IP"));
				target.sendPackets(new S_Disconnect());
			}
			else {
				pc.sendPackets(new S_SystemMessage(arg + "不在線上。"));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 " + cmdName + " 玩家名稱。"));
		}
	}
}
