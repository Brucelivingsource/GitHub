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

public class LsimulatorPresent implements LsimulatorCommandExecutor {
	private LsimulatorPresent() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorPresent();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String account = st.nextToken();
			int itemid = Integer.parseInt(st.nextToken(), 10);
			int enchant = Integer.parseInt(st.nextToken(), 10);
			int count = Integer.parseInt(st.nextToken(), 10);

			LsimulatorItem temp = ItemTable.getInstance().getTemplate(itemid);
			if (temp == null) {
				pc.sendPackets(new S_SystemMessage("不存在的道具編號。"));
				return;
			}

			LsimulatorDwarfInventory.present(account, itemid, enchant, count);
			pc.sendPackets(new S_SystemMessage(temp.getIdentifiedNameId() + "數量" + count + "個發送出去了。", true));
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 : " + ".present 帳號 道具編號 數量 強化等級。（* 等於所有帳號）"));
		}
	}
}
