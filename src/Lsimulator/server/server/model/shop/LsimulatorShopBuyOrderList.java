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

import Lsimulator.server.Config;
import Lsimulator.server.server.model.LsimulatorTaxCalculator;
import Lsimulator.server.server.templates.LsimulatorShopItem;
import Lsimulator.server.server.utils.collections.Lists;

class LsimulatorShopBuyOrder {
	private final LsimulatorShopItem _item;

	private final int _count;

	public LsimulatorShopBuyOrder(LsimulatorShopItem item, int count) {
		_item = item;
		_count = count;
	}

	public LsimulatorShopItem getItem() {
		return _item;
	}

	public int getCount() {
		return _count;
	}
}

public class LsimulatorShopBuyOrderList {
	private final LsimulatorShop _shop;

	private final List<LsimulatorShopBuyOrder> _list = Lists.newList();

	private final LsimulatorTaxCalculator _taxCalc;

	private int _totalWeight = 0;

	private int _totalPrice = 0;

	private int _totalPriceTaxIncluded = 0;

	LsimulatorShopBuyOrderList(LsimulatorShop shop) {
		_shop = shop;
		_taxCalc = new LsimulatorTaxCalculator(shop.getNpcId());
	}

	public void add(int orderNumber, int count) {
		if (_shop.getSellingItems().size() < orderNumber) {
			return;
		}
		LsimulatorShopItem shopItem = _shop.getSellingItems().get(orderNumber);

		int price = (int) (shopItem.getPrice() * Config.RATE_SHOP_SELLING_PRICE);
		// オーバーフローチェック
		for (int j = 0; j < count; j++) {
			if (price * j < 0) {
				return;
			}
		}
		if (_totalPrice < 0) {
			return;
		}
		_totalPrice += price * count;
		_totalPriceTaxIncluded += _taxCalc.layTax(price) * count;
		_totalWeight += shopItem.getItem().getWeight() * count * shopItem.getPackCount();

		if (shopItem.getItem().isStackable()) {
			_list.add(new LsimulatorShopBuyOrder(shopItem, count * shopItem.getPackCount()));
			return;
		}

		for (int i = 0; i < (count * shopItem.getPackCount()); i++) {
			_list.add(new LsimulatorShopBuyOrder(shopItem, 1));
		}
	}

	List<LsimulatorShopBuyOrder> getList() {
		return _list;
	}

	public int getTotalWeight() {
		return _totalWeight;
	}

	public int getTotalPrice() {
		return _totalPrice;
	}

	public int getTotalPriceTaxIncluded() {
		return _totalPriceTaxIncluded;
	}

	LsimulatorTaxCalculator getTaxCalculator() {
		return _taxCalc;
	}
}
