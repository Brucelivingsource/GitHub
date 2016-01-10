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
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorMagicDoll;

public class ItemMakeByDoll extends TimerTask {
	private static Logger _log = Logger.getLogger(ItemMakeByDoll.class
			.getName());

	private final LsimulatorPcInstance _pc;

	public ItemMakeByDoll(LsimulatorPcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}
			itemMake();			
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void itemMake() {
		LsimulatorItemInstance temp = ItemTable.getInstance().createItem(LsimulatorMagicDoll.getMakeItemId(_pc));
		if (temp!= null) {
			if (_pc.getInventory().checkAddItem(temp, 1) == LsimulatorInventory.OK) {
				LsimulatorItemInstance item = _pc.getInventory().storeItem(temp.getItemId(), 1);
				_pc.sendPackets(new S_ServerMessage(403, item.getItem().getName())); // 獲得%0%o 。
			}
		}
	}
}