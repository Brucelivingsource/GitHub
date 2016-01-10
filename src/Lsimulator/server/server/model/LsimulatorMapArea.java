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

import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;
import Lsimulator.server.server.types.Rectangle;

public class LsimulatorMapArea extends Rectangle {
	private LsimulatorMap _map = LsimulatorMap.newNull();

	public LsimulatorMap getMap() {
		return _map;
	}

	public void setMap(LsimulatorMap map) {
		_map = map;
	}

	public int getMapId() {
		return _map.getId();
	}

	public LsimulatorMapArea(int left, int top, int right, int bottom, int mapId) {
		super(left, top, right, bottom);

		_map = LsimulatorWorldMap.getInstance().getMap((short) mapId);
	}

	public boolean contains(LsimulatorLocation loc) {
		return (_map.getId() == loc.getMap().getId()) && super.contains(loc);
	}
}
