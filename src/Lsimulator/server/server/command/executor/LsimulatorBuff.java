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

import java.util.Collection;
import java.util.StringTokenizer;

import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorSkills;
import Lsimulator.server.server.utils.collections.Lists;

/**
 * GM指令：輔助魔法
 */
public class LsimulatorBuff implements LsimulatorCommandExecutor {
	private LsimulatorBuff() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorBuff();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			Collection<LsimulatorPcInstance> players = null;
			String s = tok.nextToken();
			if (s.equalsIgnoreCase("me")) {
				players = Lists.newList();
				players.add(pc);
				s = tok.nextToken();
			}
			else if (s.equalsIgnoreCase("all")) {
				players = LsimulatorWorld.getInstance().getAllPlayers();
				s = tok.nextToken();
			}
			else {
				players = LsimulatorWorld.getInstance().getVisiblePlayer(pc);
			}

			int skillId = Integer.parseInt(s);
			int time = 0;
			if (tok.hasMoreTokens()) {
				time = Integer.parseInt(tok.nextToken());
			}

			LsimulatorSkills skill = SkillsTable.getInstance().getTemplate(skillId);

			if (skill.getTarget().equals("buff")) {
				for (LsimulatorPcInstance tg : players) {
					new LsimulatorSkillUse().handleCommands(pc, skillId, tg.getId(), tg.getX(), tg.getY(), null, time, LsimulatorSkillUse.TYPE_SPELLSC);
				}
			}
			else if (skill.getTarget().equals("none")) {
				for (LsimulatorPcInstance tg : players) {
					new LsimulatorSkillUse().handleCommands(tg, skillId, tg.getId(), tg.getX(), tg.getY(), null, time, LsimulatorSkillUse.TYPE_GMBUFF);
				}
			}
			else {
				pc.sendPackets(new S_SystemMessage("非buff類型的魔法。"));
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 " + cmdName + " [all|me] skillId time。"));
		}
	}
}
