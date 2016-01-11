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
package Lsimulator.server.server.model.shop;

import java.util.List;

import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.utils.collections.Lists;

class LsimulatorShopSellOrder {
	private final LsimulatorAssessedItem _item;

	private final int _count;

	public LsimulatorShopSellOrder(LsimulatorAssessedItem item, int count) {
		_item = item;
		_count = count;
	}

	public LsimulatorAssessedItem getItem() {
		return _item;
	}

	public int getCount() {
		return _count;
	}

}

public class LsimulatorShopSellOrderList {
	private final LsimulatorShop _shop;

	private final PcInstance _pc;

	private final List<LsimulatorShopSellOrder> _list = Lists.newList();

	LsimulatorShopSellOrderList(LsimulatorShop shop, PcInstance pc) {
		_shop = shop;
		_pc = pc;
	}

	public void add(int itemObjectId, int count) {
		LsimulatorAssessedItem assessedItem = _shop.assessItem(_pc.getInventory().getItem(itemObjectId));
		if (assessedItem == null) {
			/*
			 * 買取リストに無いアイテムが指定された。 不正パケの可能性。
			 */
			throw new IllegalArgumentException();
		}

		_list.add(new LsimulatorShopSellOrder(assessedItem, count));
	}

	PcInstance getPc() {
		return _pc;
	}

	List<LsimulatorShopSellOrder> getList() {
		return _list;
	}
}
