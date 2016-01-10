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

import java.util.StringTokenizer;

import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.LsimulatorDwarfInventory;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorItem;

public class LsimulatorLevelPresent implements LsimulatorCommandExecutor {
	private LsimulatorLevelPresent() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorLevelPresent();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {

		try {
			StringTokenizer st = new StringTokenizer(arg);
			int minlvl = Integer.parseInt(st.nextToken(), 10);
			int maxlvl = Integer.parseInt(st.nextToken(), 10);
			int itemid = Integer.parseInt(st.nextToken(), 10);
			int enchant = Integer.parseInt(st.nextToken(), 10);
			int count = Integer.parseInt(st.nextToken(), 10);

			LsimulatorItem temp = ItemTable.getInstance().getTemplate(itemid);
			if (temp == null) {
				pc.sendPackets(new S_SystemMessage("不存在的道具編號。"));
				return;
			}

			LsimulatorDwarfInventory.present(minlvl, maxlvl, itemid, enchant, count);
			pc.sendPackets(new S_SystemMessage(temp.getName() + "數量" + count + "個發送出去了。(Lv" + minlvl + "～" + maxlvl + ")"));
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 .lvpresent minlvl maxlvl 道具編號  強化等級 數量。"));
		}
	}
}
