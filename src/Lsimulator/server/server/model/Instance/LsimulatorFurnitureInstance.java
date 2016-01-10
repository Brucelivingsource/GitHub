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
package Lsimulator.server.server.model.Instance;

import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorFurnitureInstance extends LsimulatorNpcInstance {

	private static final long serialVersionUID = 1L;

	private int _itemObjId;

	public LsimulatorFurnitureInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(LsimulatorPcInstance player) {
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		LsimulatorWorld.getInstance().removeVisibleObject(this);
		LsimulatorWorld.getInstance().removeObject(this);
		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}

	public int getItemObjId() {
		return _itemObjId;
	}

	public void setItemObjId(int i) {
		_itemObjId = i;
	}

}
