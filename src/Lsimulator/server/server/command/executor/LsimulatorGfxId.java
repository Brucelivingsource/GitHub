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

import java.lang.reflect.Constructor;
import java.util.StringTokenizer;

import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorGfxId implements LsimulatorCommandExecutor {
	private LsimulatorGfxId() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorGfxId();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			int gfxid = Integer.parseInt(st.nextToken(), 10);
			int count = Integer.parseInt(st.nextToken(), 10);
			for (int i = 0; i < count; i++) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(45001);
				if (l1npc != null) {
					String s = l1npc.getImpl();
					Constructor<?> constructor = Class.forName("Lsimulator.server.server.model.Instance." + s + "Instance").getConstructors()[0];
					Object aobj[] =
					{ l1npc };
					NpcInstance npc = (NpcInstance) constructor.newInstance(aobj);
					npc.setId(IdFactory.getInstance().nextId());
					npc.setGfxId(gfxid + i);
					npc.setTempCharGfx(0);
					npc.setNameId("");
					npc.setMap(pc.getMapId());
					npc.setX(pc.getX() + i << 1);
					npc.setY(pc.getY() + i << 1 );
					npc.setHomeX(npc.getX());
					npc.setHomeY(npc.getY());
					npc.setHeading(4);

					LsimulatorWorld.getInstance().storeObject(npc);
					LsimulatorWorld.getInstance().addVisibleObject(npc);
				}
			}
		}
		catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(cmdName + " 請輸入  動畫編號  動畫數量  人物ID。"));
		}
	}
}
