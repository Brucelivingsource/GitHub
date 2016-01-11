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

import java.util.Map;
import java.util.StringTokenizer;

import Lsimulator.server.server.datatables.NpcSpawnTable;
import Lsimulator.server.server.datatables.SpawnTable;
import Lsimulator.server.server.model.LsimulatorSpawn;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.utils.collections.Maps;

public class LsimulatorToSpawn implements LsimulatorCommandExecutor {
	private static final Map<Integer, Integer> _spawnId = Maps.newMap();

	private LsimulatorToSpawn() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorToSpawn();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			if (!_spawnId.containsKey(pc.getId())) {
				_spawnId.put(pc.getId(), 0);
			}
			int id = _spawnId.get(pc.getId());
			if (arg.isEmpty() || arg.equals("+")) {
				id++;
			}
			else if (arg.equals("-")) {
				id--;
			}
			else {
				StringTokenizer st = new StringTokenizer(arg);
				id = Integer.parseInt(st.nextToken());
			}
			LsimulatorSpawn spawn = NpcSpawnTable.getInstance().getTemplate(id);
			if (spawn == null) {
				spawn = SpawnTable.getInstance().getTemplate(id);
			}
			if (spawn != null) {
				LsimulatorTeleport.teleport(pc, spawn.getLocX(), spawn.getLocY(), spawn.getMapId(), 5, false);
				pc.sendPackets(new S_SystemMessage("spawnid(" + id + ")已傳送到"));
			}
			else {
				pc.sendPackets(new S_SystemMessage("spawnid(" + id + ")找不到"));
			}
			_spawnId.put(pc.getId(), id);
		}
		catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(cmdName + " spawnid|+|-"));
		}
	}
}
