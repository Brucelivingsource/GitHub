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
package Lsimulator.server.server.clientpackets;

import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorTrade;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorDollInstance;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來增加交易物品的封包
 */
public class C_TradeAddItem extends ClientBasePacket {
	private static final String C_TRADE_ADD_ITEM = "[C] C_TradeAddItem";

	public C_TradeAddItem(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);

		LsimulatorPcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}
		
		int itemid = readD();
		int itemcount = readD();
		
		LsimulatorTrade trade = new LsimulatorTrade();
		LsimulatorItemInstance item = pc.getInventory().getItem(itemid);
		if (!item.getItem().isTradable()) {
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); // \f1%0は捨てたりまたは他人に讓ることができません。
			return;
		}
		if (item.getBless() >= 128) { // 封印的裝備
			// \f1%0は捨てたりまたは他人に讓ることができません。
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
			return;
		}
		// 使用中的寵物項鍊 - 無法交易
		for (LsimulatorNpcInstance petNpc : pc.getPetList().values()) {
			if (petNpc instanceof LsimulatorPetInstance) {
				LsimulatorPetInstance pet = (LsimulatorPetInstance) petNpc;
				if (item.getId() == pet.getItemObjId()) {
					pc.sendPackets(new S_ServerMessage(1187)); // 寵物項鍊正在使用中。
					return;
				}
			}
		}
		// 使用中的魔法娃娃 - 無法交易
		for (LsimulatorDollInstance doll : pc.getDollList().values()) {
			if (doll.getItemObjId() == item.getId()) {
				pc.sendPackets(new S_ServerMessage(1181)); // 這個魔法娃娃目前正在使用中。
				return;
			}
		}

		LsimulatorPcInstance tradingPartner = (LsimulatorPcInstance) LsimulatorWorld.getInstance().findObject(pc.getTradeID());
		if (tradingPartner == null) {
			return;
		}
		if (pc.getTradeOk()) {
			return;
		}
		if (tradingPartner.getInventory().checkAddItem(item, itemcount) != LsimulatorInventory.OK) { // 檢查容量與重量
			tradingPartner.sendPackets(new S_ServerMessage(270)); // \f1持っているものが重くて取引できません。
			pc.sendPackets(new S_ServerMessage(271)); // \f1相手が物を持ちすぎていて取引できません。
			return;
		}

		trade.TradeAddItem(pc, itemid, itemcount);
	}

	@Override
	public String getType() {
		return C_TRADE_ADD_ITEM;
	}
}
