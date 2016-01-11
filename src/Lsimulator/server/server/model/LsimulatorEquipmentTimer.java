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
package Lsimulator.server.server.model;

import java.util.TimerTask;

import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;

public class LsimulatorEquipmentTimer extends TimerTask {
	public LsimulatorEquipmentTimer(PcInstance pc, ItemInstance item) {
		_pc = pc;
		_item = item;
	}

	@Override
	public void run() {
		if ((_item.getRemainingTime() - 1) > 0) {
			_item.setRemainingTime(_item.getRemainingTime() - 1);
			_pc.getInventory().updateItem(_item, LsimulatorPcInventory.COL_REMAINING_TIME);
		}
		else {
			_pc.getInventory().removeItem(_item, 1);
			cancel();
		}
	}

	private final PcInstance _pc;

	private final ItemInstance _item;
}
