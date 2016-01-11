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
import Lsimulator.server.server.model.LsimulatorTrade;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來交易OK的封包
 */
public class C_TradeOK extends ClientBasePacket {

	private static final String C_TRADE_CANCEL = "[C] C_TradeOK";

	public C_TradeOK(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);

		PcInstance player = clientthread.getActiveChar();
		if (player == null) {
			return;
		}
		
		PcInstance trading_partner = (PcInstance) LsimulatorWorld.getInstance().findObject(player.getTradeID());
		if (trading_partner != null) {
			player.setTradeOk(true);

			if (player.getTradeOk() && trading_partner.getTradeOk()) // 同時都壓OK
			{
				// 檢查身上的空間是否還有 (180 - 16)
				if ((player.getInventory().getSize() < (180 - 16)) && (trading_partner.getInventory().getSize() < (180 - 16))) // お互いのアイテムを相手に渡す
				{
					LsimulatorTrade trade = new LsimulatorTrade();
					trade.TradeOK(player);
				}
				else // お互いのアイテムを手元に戻す
				{
					player.sendPackets(new S_ServerMessage(263)); // \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
					trading_partner.sendPackets(new S_ServerMessage(263)); // \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
					LsimulatorTrade trade = new LsimulatorTrade();
					trade.TradeCancel(player);
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_TRADE_CANCEL;
	}

}
