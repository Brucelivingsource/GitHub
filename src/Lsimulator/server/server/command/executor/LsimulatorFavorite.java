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

import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.GMCommands;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.utils.collections.Maps;

/**
 * GM指令：我的最愛
 */
public class LsimulatorFavorite implements LsimulatorCommandExecutor {
	private static Logger _log = Logger.getLogger(LsimulatorFavorite.class.getName());

	private static final Map<Integer, String> _faviCom = Maps.newMap();

	private LsimulatorFavorite() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorFavorite();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			if (!_faviCom.containsKey(pc.getId())) {
				_faviCom.put(pc.getId(), "");
			}
			String faviCom = _faviCom.get(pc.getId());
			if (arg.charAt(0) == 's'
                                                         &&arg.charAt(1) == 'e'
                                                         &&arg.charAt(2) == 't'
                                                        ) {
				// コマンドの登録
				StringTokenizer st = new StringTokenizer(arg);
				st.nextToken();
				if (!st.hasMoreTokens()) {
					pc.sendPackets(new S_SystemMessage("指令不存在。"));
					return;
				}
				StringBuilder cmd = new StringBuilder();
				String temp = st.nextToken(); // コマンドタイプ
				if (temp.equalsIgnoreCase(cmdName)) {
					pc.sendPackets(new S_SystemMessage(cmdName + " 不能加入自己的名字。"));
					return;
				}
				cmd.append(temp + " ");
				while (st.hasMoreTokens()) {
					cmd.append(st.nextToken() + " ");
				}
				faviCom = cmd.toString().trim();
				_faviCom.put(pc.getId(), faviCom);
				pc.sendPackets(new S_SystemMessage(faviCom + " 被登記在好友名單。"));
			}
			else if (arg.charAt(0) == 's'
                                                         &&arg.charAt(1) == 'h'
                                                         &&arg.charAt(2) == 'o'
                                                         &&arg.charAt(2) == 'w' ) {
				pc.sendPackets(new S_SystemMessage("目前登記的指令: " + faviCom));
			}
			else if (faviCom.isEmpty()) {
				pc.sendPackets(new S_SystemMessage("沒有被登記的名字。"));
			}
			else {
				StringBuilder cmd = new StringBuilder();
				StringTokenizer st = new StringTokenizer(arg);
				StringTokenizer st2 = new StringTokenizer(faviCom);
				while (st2.hasMoreTokens()) {
					String temp = st2.nextToken();
					if (temp.charAt(0) =='%') {
						cmd.append(st.nextToken() + " ");
					}
					else {
						cmd.append(temp + " ");
					}
				}
				while (st.hasMoreTokens()) {
					cmd.append(st.nextToken() + " ");
				}
				pc.sendPackets(new S_SystemMessage(cmd + " 實行。"));
				GMCommands.getInstance().handleCommands(pc, cmd.toString());
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 " + cmdName + " set 玩家名稱 " + "| " + cmdName + " show | " + cmdName + " [數量]。"));
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
