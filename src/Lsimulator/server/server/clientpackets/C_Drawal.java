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
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorCastle;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * TODO: 處理收到由客戶端傳來取得城堡稅收的封包(?)
 */
public class C_Drawal extends ClientBasePacket {

	private static final String C_DRAWAL = "[C] C_Drawal";

	public C_Drawal(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		
		LsimulatorPcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}
		
		readD();
		int j = Math.abs(readD());
		
		LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
		if (clan != null) {
			int castle_id = clan.getCastleId();
			if (castle_id != 0) {
				LsimulatorCastle l1castle = CastleTable.getInstance().getCastleTable(castle_id);
				int money = l1castle.getPublicMoney();
				money -= j;
				LsimulatorItemInstance item = ItemTable.getInstance().createItem(LsimulatorItemId.ADENA);
				if (item != null) {
					l1castle.setPublicMoney(money);
					CastleTable.getInstance().updateCastle(l1castle);
					if (pc.getInventory().checkAddItem(item, j) == LsimulatorInventory.OK) {
						pc.getInventory().storeItem(LsimulatorItemId.ADENA, j);
					}
					else {
						LsimulatorWorld.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(LsimulatorItemId.ADENA, j);
					}
					pc.sendPackets(new S_ServerMessage(143, "$457", "$4" + " (" + j + ")")); // \f1%0%s
																								// 給你
																								// %1%o
																								// 。
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_DRAWAL;
	}

}
