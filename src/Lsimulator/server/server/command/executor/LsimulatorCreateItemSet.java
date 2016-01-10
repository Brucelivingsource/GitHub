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

import java.util.List;
import java.util.StringTokenizer;

import Lsimulator.server.server.GMCommandsConfig;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorItem;
import Lsimulator.server.server.templates.LsimulatorItemSetItem;

/**
 * GM指令：創立套裝
 */
public class LsimulatorCreateItemSet implements LsimulatorCommandExecutor {
	private LsimulatorCreateItemSet() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorCreateItemSet();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		try {
			String name = new StringTokenizer(arg).nextToken();
			List<LsimulatorItemSetItem> list = GMCommandsConfig.ITEM_SETS.get(name);
			if (list == null) {
				pc.sendPackets(new S_SystemMessage(name + " 是未定義的套裝。"));
				return;
			}
			for (LsimulatorItemSetItem item : list) {
				LsimulatorItem temp = ItemTable.getInstance().getTemplate(item.getId());
				if (!temp.isStackable() && (0 != item.getEnchant())) {
					for (int i = 0; i < item.getAmount(); i++) {
						LsimulatorItemInstance inst = ItemTable.getInstance().createItem(item.getId());
						inst.setEnchantLevel(item.getEnchant());
						pc.getInventory().storeItem(inst);
					}
				}
				else {
					pc.getInventory().storeItem(item.getId(), item.getAmount());
				}
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 .itemset 套裝名稱。"));
		}
	}
}
