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
import Lsimulator.server.server.datatables.ClanMembersTable;
import Lsimulator.server.server.datatables.ClanTable;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.serverpackets.S_ClanAttention;
import Lsimulator.server.server.serverpackets.S_ClanName;
import Lsimulator.server.server.serverpackets.S_PacketBox;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來建立血盟的封包
 */
public class C_CreateClan extends ClientBasePacket {

	private static final String C_CREATE_CLAN = "[C] C_CreateClan";

	public C_CreateClan(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		
		LsimulatorPcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}
		
		String s = readS();
		if (pc.isCrown()) { // 是王族
			if (pc.getClanid() == 0) {
				for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) { // 檢查是否有同名的血盟
					if (clan.getClanName().toLowerCase().equals(s.toLowerCase())) {
						pc.sendPackets(new S_ServerMessage(99)); // \f1那個血盟名稱已經存在。
						return;
					}
				}
				if (pc.getInventory().checkItem(LsimulatorItemId.ADENA, 30000)) { // 身上有金幣3萬
					LsimulatorClan clan = ClanTable.getInstance().createClan(pc, s); // 建立血盟
					ClanMembersTable.getInstance().newMember(pc);
					if (clan != null) {
						pc.getInventory().consumeItem(LsimulatorItemId.ADENA, 30000);
						pc.sendPackets(new S_ServerMessage(84, s)); // 創立\f1%0  血盟。
						pc.sendPackets(new S_ClanName(pc, true));
						pc.sendPackets(new S_PacketBox(S_PacketBox.PLEDGE_EMBLEM_STATUS, pc.getClan().getEmblemStatus()));
						pc.sendPackets(new S_ClanAttention());
					}
				} else {
					pc.sendPackets(new S_ServerMessage(189)); // \f1金幣不足。
				}
			} else {
				pc.sendPackets(new S_ServerMessage(86)); // \f1已經創立血盟。
			}
		} else {
			pc.sendPackets(new S_ServerMessage(85)); // \f1王子和公主才可創立血盟。
		}
	}

	@Override
	public String getType() {
		return C_CREATE_CLAN;
	}

}
