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

import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class LsimulatorLoc implements LsimulatorCommandExecutor {
	private static Logger _log = Logger.getLogger(LsimulatorLoc.class.getName());

	private LsimulatorLoc() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorLoc();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			int locx = pc.getX();
			int locy = pc.getY();
			short mapid = pc.getMapId();
			int gab = LsimulatorWorldMap.getInstance().getMap(mapid).getOriginalTile(
					locx, locy);
			String msg = String.format("座標 (%d, %d, %d) %d", locx, locy, mapid,
					gab);
			pc.sendPackets(new S_SystemMessage(msg));
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
