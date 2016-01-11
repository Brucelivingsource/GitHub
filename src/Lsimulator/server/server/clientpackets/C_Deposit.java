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
import Lsimulator.server.server.datatables.CastleTable;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.templates.LsimulatorCastle;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * TODO: 處理收到由客戶端傳來納稅的封包(?)
 */
public class C_Deposit extends ClientBasePacket {

	private static final String C_DEPOSIT = "[C] C_Deposit";

	public C_Deposit(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		
		PcInstance player = clientthread.getActiveChar();
		if (player == null) {
			return;
		}
		
		int i = readD();
		int j = readD();
	
		if (i == player.getId()) {
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(player.getClanname());
			if (clan != null) {
				int castle_id = clan.getCastleId();
				if (castle_id != 0) { // 有城堡的盟主
					LsimulatorCastle l1castle = CastleTable.getInstance().getCastleTable(castle_id);
					synchronized (l1castle) {
						int money = l1castle.getPublicMoney();
						if (player.getInventory().consumeItem(LsimulatorItemId.ADENA, j)) {
							money += j;
							l1castle.setPublicMoney(money);
							CastleTable.getInstance().updateCastle(l1castle);
						}
					}
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_DEPOSIT;
	}

}
