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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import Lsimulator.server.server.utils.collections.Lists;
import Lsimulator.server.server.utils.collections.Maps;

public class LsimulatorUbPattern {
	private boolean _isFrozen = false;

	private Map<Integer, List<LsimulatorUbSpawn>> _groups = Maps.newMap();

	public void addSpawn(int groupNumber, LsimulatorUbSpawn spawn) {
		if (_isFrozen) {
			return;
		}

		List<LsimulatorUbSpawn> spawnList = _groups.get(groupNumber);
		if (spawnList == null) {
			spawnList = Lists.newList();
			_groups.put(groupNumber, spawnList);
		}

		spawnList.add(spawn);
	}

	public void freeze() {
		if (_isFrozen) {
			return;
		}

		// 格納されているグループのスポーンリストをID順にソート
		for (List<LsimulatorUbSpawn> spawnList : _groups.values()) {
			Collections.sort(spawnList);
		}

		_isFrozen = true;
	}

	public boolean isFrozen() {
		return _isFrozen;
	}

	public List<LsimulatorUbSpawn> getSpawnList(int groupNumber) {
		if (!_isFrozen) {
			return null;
		}

		return _groups.get(groupNumber);
	}
}
