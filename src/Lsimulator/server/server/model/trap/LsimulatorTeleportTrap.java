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
package Lsimulator.server.server.model.trap;

import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.storage.TrapStorage;

public class LsimulatorTeleportTrap extends LsimulatorTrap {
	private final LsimulatorLocation _loc;

	public LsimulatorTeleportTrap(TrapStorage storage) {
		super(storage);

		int x = storage.getInt("teleportX");
		int y = storage.getInt("teleportY");
		int mapId = storage.getInt("teleportMapId");
		_loc = new LsimulatorLocation(x, y, mapId);
	}

	@Override
	public void onTrod(PcInstance trodFrom, LsimulatorObject trapObj) {
		sendEffect(trapObj);

		LsimulatorTeleport.teleport(trodFrom, _loc.getX(), _loc.getY(), (short) _loc
				.getMapId(), 5, true);
	}
}
