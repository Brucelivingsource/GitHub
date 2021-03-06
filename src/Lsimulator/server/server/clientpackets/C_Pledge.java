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
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_PacketBox;
import Lsimulator.server.server.serverpackets.S_Pledge;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來血盟的封包
 */
public class C_Pledge extends ClientBasePacket {

	private static final String C_PLEDGE = "[C] C_Pledge";

	public C_Pledge(byte abyte0[], ClientThread clientthread) throws Exception{
		super(abyte0);
		
		PcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}

		if (pc.getClanid() > 0) {
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
			// 血盟公告
			pc.sendPackets(new S_Pledge(clan.getClanId()));
			
			// 血盟成員
			pc.sendPackets(new S_Pledge(pc));
			
			// 線上血盟成員
			pc.sendPackets(new S_PacketBox(S_PacketBox.HTML_PLEDGE_ONLINE_MEMBERS, clan.getOnlineClanMember()));
		} else {
			// 不屬於血盟。
			pc.sendPackets(new S_ServerMessage(1064));
		}
	}

	@Override
	public String getType() {
		return C_PLEDGE;
	}

}
