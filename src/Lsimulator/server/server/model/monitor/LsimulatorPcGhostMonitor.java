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
package Lsimulator.server.server.model.monitor;

import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.model.Instance.PcInstance;

public class LsimulatorPcGhostMonitor extends LsimulatorPcMonitor {

	public LsimulatorPcGhostMonitor(int oId) {
		super(oId);
	}

	@Override
	public void execTask(PcInstance pc) {
		// endGhostの実行時間が影響ないように
		Runnable r = new LsimulatorPcMonitor(pc.getId()) {
			@Override
			public void execTask(PcInstance pc) {
				pc.endGhost();
			}
		};
		GeneralThreadPool.getInstance().execute(r);
	}
}
