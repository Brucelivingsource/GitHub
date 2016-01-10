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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.utils.collections.Lists;
import Lsimulator.server.server.utils.collections.Maps;

public class LsimulatorHateList {
	private final Map<LsimulatorCharacter, Integer> _hateMap;

	private LsimulatorHateList(Map<LsimulatorCharacter, Integer> hateMap) {
		_hateMap = hateMap;
	}

	public LsimulatorHateList() {
		/*
		 * ConcurrentHashMapを利用するより、 全てのメソッドを同期する方がメモリ使用量、速度共に優れていた。
		 * 但し、今後このクラスの利用方法が変わった場合、 例えば多くのスレッドから同時に読み出しがかかるようになった場合は、
		 * ConcurrentHashMapを利用した方が良いかもしれない。
		 */
		_hateMap = Maps.newMap();
	}

	public synchronized void add(LsimulatorCharacter cha, int hate) {
		if (cha == null) {
			return;
		}
		if (_hateMap.containsKey(cha)) {
			_hateMap.put(cha, _hateMap.get(cha) + hate);
		}
		else {
			_hateMap.put(cha, hate);
		}
	}

	public synchronized int get(LsimulatorCharacter cha) {
		return _hateMap.get(cha);
	}

	public synchronized boolean containsKey(LsimulatorCharacter cha) {
		return _hateMap.containsKey(cha);
	}

	public synchronized void remove(LsimulatorCharacter cha) {
		_hateMap.remove(cha);
	}

	public synchronized void clear() {
		_hateMap.clear();
	}

	public synchronized boolean isEmpty() {
		return _hateMap.isEmpty();
	}

	public synchronized LsimulatorCharacter getMaxHateCharacter() {
		LsimulatorCharacter cha = null;
		int hate = Integer.MIN_VALUE;

		for (Map.Entry<LsimulatorCharacter, Integer> e : _hateMap.entrySet()) {
			if (hate < e.getValue()) {
				cha = e.getKey();
				hate = e.getValue();
			}
		}
		return cha;
	}

	public synchronized void removeInvalidCharacter(LsimulatorNpcInstance npc) {
		List<LsimulatorCharacter> invalidChars = Lists.newList();
		for (LsimulatorCharacter cha : _hateMap.keySet()) {
			if ((cha == null) || cha.isDead() || !npc.knownsObject(cha)) {
				invalidChars.add(cha);
			}
		}

		for (LsimulatorCharacter cha : invalidChars) {
			_hateMap.remove(cha);
		}
	}

	public synchronized int getTotalHate() {
		int totalHate = 0;
		for (int hate : _hateMap.values()) {
			totalHate += hate;
		}
		return totalHate;
	}

	public synchronized int getTotalLawfulHate() {
		int totalHate = 0;
		for (Map.Entry<LsimulatorCharacter, Integer> e : _hateMap.entrySet()) {
			if (e.getKey() instanceof LsimulatorPcInstance) {
				totalHate += e.getValue();
			}
		}
		return totalHate;
	}

	public synchronized int getPartyHate(LsimulatorParty party) {
		int partyHate = 0;

		for (Map.Entry<LsimulatorCharacter, Integer> e : _hateMap.entrySet()) {
			LsimulatorPcInstance pc = null;
			if (e.getKey() instanceof LsimulatorPcInstance) {
				pc = (LsimulatorPcInstance) e.getKey();
			}
			if (e.getKey() instanceof LsimulatorNpcInstance) {
				LsimulatorCharacter cha = ((LsimulatorNpcInstance) e.getKey()).getMaster();
				if (cha instanceof LsimulatorPcInstance) {
					pc = (LsimulatorPcInstance) cha;
				}
			}

			if ((pc != null) && party.isMember(pc)) {
				partyHate += e.getValue();
			}
		}
		return partyHate;
	}

	public synchronized int getPartyLawfulHate(LsimulatorParty party) {
		int partyHate = 0;

		for (Map.Entry<LsimulatorCharacter, Integer> e : _hateMap.entrySet()) {
			LsimulatorPcInstance pc = null;
			if (e.getKey() instanceof LsimulatorPcInstance) {
				pc = (LsimulatorPcInstance) e.getKey();
			}

			if ((pc != null) && party.isMember(pc)) {
				partyHate += e.getValue();
			}
		}
		return partyHate;
	}

	public synchronized LsimulatorHateList copy() {
		return new LsimulatorHateList(new HashMap<LsimulatorCharacter, Integer>(_hateMap));
	}

	public synchronized Set<Entry<LsimulatorCharacter, Integer>> entrySet() {
		return _hateMap.entrySet();
	}

	public synchronized ArrayList<LsimulatorCharacter> toTargetArrayList() {
		return new ArrayList<LsimulatorCharacter>(_hateMap.keySet());
	}

	public synchronized ArrayList<Integer> toHateArrayList() {
		return new ArrayList<Integer>(_hateMap.values());
	}
}
