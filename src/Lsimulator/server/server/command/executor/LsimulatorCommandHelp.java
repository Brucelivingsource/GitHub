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

import java.util.List;

import Lsimulator.server.server.command.LsimulatorCommands;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorCommand;

/**
 * GM指令：取得所有指令
 */
public class LsimulatorCommandHelp implements LsimulatorCommandExecutor {
	private LsimulatorCommandHelp() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorCommandHelp();
	}

	private String join(List<LsimulatorCommand> list, String with) {
		StringBuilder result = new StringBuilder();
		for (LsimulatorCommand cmd : list) {
			if (result.length() > 0) {
				result.append(with);
			}
			result.append(cmd.getName());
		}
		return result.toString();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		List<LsimulatorCommand> list = LsimulatorCommands.availableCommandList(pc.getAccessLevel());
		pc.sendPackets(new S_SystemMessage(join(list, ", ")));
	}
}
