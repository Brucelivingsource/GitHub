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
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 收到由客戶端傳來離開組隊的封包
 */
public class C_BanParty extends ClientBasePacket {

	private static final String C_BAN_PARTY = "[C] C_BanParty";

	public C_BanParty(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		
		PcInstance player = client.getActiveChar();
		if (player == null) {
			return;
		}
		
		String s = readS();
		
		if (!player.getParty().isLeader(player)) {
			// 非組隊隊長
			player.sendPackets(new S_ServerMessage(427)); // 只有領導者才有驅逐隊伍成員的權力。
			return;
		}

		for (PcInstance member : player.getParty().getMembers()) {
			if (member.getName().toLowerCase().equals(s.toLowerCase())) {
				player.getParty().kickMember(member);
				return;
			}
		}

		player.sendPackets(new S_ServerMessage(426, s)); // %0%d 不屬於任何隊伍。
	}

	@Override
	public String getType() {
		return C_BAN_PARTY;
	}

}
