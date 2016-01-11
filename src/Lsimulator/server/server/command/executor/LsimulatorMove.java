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

import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorMove implements LsimulatorCommandExecutor {
	private LsimulatorMove() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorMove();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			int locx = Integer.parseInt(st.nextToken());
			int locy = Integer.parseInt(st.nextToken());
			short mapid;
			if (st.hasMoreTokens()) {
				mapid = Short.parseShort(st.nextToken());
			}
			else {
				mapid = pc.getMapId();
			}
			LsimulatorTeleport.teleport(pc, locx, locy, mapid, 5, false);
			pc.sendPackets(new S_SystemMessage("座標 " + locx + ", " + locy + ", " + mapid + "已經到達。"));
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + "請輸入 X座標 Y座標 [地圖編號]。"));
		}
	}
}
