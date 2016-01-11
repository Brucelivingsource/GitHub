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
package Lsimulator.server.server.serverpackets;

import java.io.IOException;
import java.util.List;

import Lsimulator.server.Config;
import Lsimulator.server.server.Opcodes;
import Lsimulator.server.server.datatables.ShopTable;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorTaxCalculator;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.shop.LsimulatorShop;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.templates.LsimulatorItem;
import Lsimulator.server.server.templates.LsimulatorShopItem;
import java.util.StringTokenizer;

public class S_ShopSellList extends ServerBasePacket {

	/**
	 * 商店販賣的物品清單
	 * 店の品物リストを表示する。キャラクターがBUYボタンを押した時に送る。
	 */
	public S_ShopSellList(int objId, LsimulatorPcInstance pc) {
		writeC(Opcodes.S_OPCODE_SHOWSHOPBUYLIST);
		writeD(objId);

		LsimulatorObject npcObj = LsimulatorWorld.getInstance().findObject(objId);
		if (!(npcObj instanceof LsimulatorNpcInstance)) {
			writeH(0);
			return;
		}
		int npcId = ((LsimulatorNpcInstance) npcObj).getNpcTemplate().get_npcId();

		LsimulatorTaxCalculator calc = new LsimulatorTaxCalculator(npcId);
		LsimulatorShop shop = ShopTable.getInstance().get(npcId);
		List<LsimulatorShopItem> shopItems = shop.getSellingItems();

		writeH(shopItems.size());

		// LsimulatorItemInstanceのgetStatusBytesを利用するため
		LsimulatorItemInstance dummy = new LsimulatorItemInstance();

		for (int i = 0; i < shopItems.size(); i++) {
			LsimulatorShopItem shopItem = shopItems.get(i);
			LsimulatorItem item = shopItem.getItem();
			int price = calc.layTax((int) (shopItem.getPrice() * Config.RATE_SHOP_SELLING_PRICE));
			writeD(i);
			writeH(shopItem.getItem().getGfxId());
			writeD(price);

			if (shopItem.getPackCount() > 1) {
				writeS(item.getName() + " (" + shopItem.getPackCount() + ")");
			} else {
				if (item.getItemId() == 40309) {// 食人妖精RaceTicket
                                                                                            
					String[] temp = item.getName().split(" ");
					String buf = temp[temp.length - 1]; 
					temp = buf.split("-");
					writeS(buf + " $"
							+ (1212 + Integer.parseInt(temp[temp.length - 1])));
				} else {
					writeS(item.getName());
				}
			}

			LsimulatorItem template = ItemTable.getInstance().getTemplate(item.getItemId());
			if (template == null) {
				writeC(0);
			} else {
				dummy.setItem(template);
				byte[] status = dummy.getStatusBytes();
				writeC(status.length);
				for (byte b : status) {
					writeC(b);
				}
			}
		}
		writeH(0x07); // 0x00:kaimo 0x01:pearl 0x07:adena
	}

	@Override
	public byte[] getContent() throws IOException {
		return _bao.toByteArray();
	}
}
