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
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorSystemMessageId;
import Lsimulator.server.server.serverpackets.S_Message_YN;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來建立組隊的封包
 */
public class C_CreateParty extends ClientBasePacket {

	private static final String C_CREATE_PARTY = "[C] C_CreateParty";

	public C_CreateParty(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		
		LsimulatorPcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}

		int type = readC();
		if ((type == 0) || (type == 1)) { // 自動接受組隊 on 與 off 的同
			int targetId = readD();
			LsimulatorObject temp = LsimulatorWorld.getInstance().findObject(targetId);
			if (temp instanceof LsimulatorPcInstance) {
				LsimulatorPcInstance targetPc = (LsimulatorPcInstance) temp;
				if (pc.getId() == targetPc.getId()) {
					return;
				}
				if ((!pc.getLocation().isInScreen(targetPc.getLocation()) || (pc.getLocation().getTileLineDistance(targetPc.getLocation()) > 7))) {
					// 邀請組隊時，對象不再螢幕內或是7步內
					pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$952));
					return;
				}
				if (targetPc.isInParty()) {
					// 您無法邀請已經參加其他隊伍的人。
					pc.sendPackets(new S_ServerMessage(415));
					return;
				}

				if (pc.isInParty()) {
					if (pc.getParty().isLeader(pc)) {
						targetPc.setPartyType(type);
						targetPc.setPartyID(pc.getId());
						switch (type) {
						case 0:
							// 玩家 %0%s 邀請您加入隊伍？(Y/N)
							targetPc.sendPackets(new S_Message_YN(953, pc.getName()));
							break;
						case 1:
							// 玩家 %0%s 邀請您加入自動分配隊伍？(Y/N)
							targetPc.sendPackets(new S_Message_YN(954, pc.getName()));
							break;
						}
					} else {
						// 只有領導者才能邀請其他的成員。
						pc.sendPackets(new S_ServerMessage(416));
					}
				} else {
					pc.setPartyType(type);
					targetPc.setPartyID(pc.getId());
					switch (type) {
					case 0:
						// 玩家 %0%s 邀請您加入隊伍？(Y/N)
						targetPc.sendPackets(new S_Message_YN(953, pc.getName()));
						break;
					case 1:
						targetPc.sendPackets(new S_Message_YN(954, pc.getName()));
						break;
					}
				}
			}
		} else if (type == 2) { // 聊天組隊
			String name = readS();
			LsimulatorPcInstance targetPc = LsimulatorWorld.getInstance().getPlayer(name);
			if (targetPc == null) {
				// 沒有叫%0的人。
				pc.sendPackets(new S_ServerMessage(109));
				return;
			}
			if (pc.getId() == targetPc.getId()) {
				return;
			}
			if ((!pc.getLocation().isInScreen(targetPc.getLocation()) || (pc.getLocation().getTileLineDistance(targetPc.getLocation()) > 7))) {
				// 邀請組隊時，對象不再螢幕內或是7步內
				pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$952));
				return;
			}
			if (targetPc.isInChatParty()) {
				// 您無法邀請已經參加其他隊伍的人。
				pc.sendPackets(new S_ServerMessage(415));
				return;
			}

			if (pc.isInChatParty()) {
				if (pc.getChatParty().isLeader(pc)) {
					targetPc.setPartyID(pc.getId());
					// 您要接受玩家 %0%s 提出的隊伍對話邀請嗎？(Y/N)
					targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
				} else {
					// 只有領導者才能邀請其他的成員。
					pc.sendPackets(new S_ServerMessage(416));
				}
			} else {
				targetPc.setPartyID(pc.getId());
				// 您要接受玩家 %0%s 提出的隊伍對話邀請嗎？(Y/N)
				targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
			}
		}
		// 隊長委任
		else if (type == 3) {
			// 不是隊長時, 不可使用
			if ((pc.getParty() == null) || !pc.getParty().isLeader(pc)) {
				pc.sendPackets(new S_ServerMessage(1697));
				return;
			}

			// 取得目標物件編號
			int targetId = readD();

			// 嘗試取得目標
			LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(targetId);

			// 判斷目標是否合理
			if ((obj == null) || (pc.getId() == obj.getId()) || !(obj instanceof LsimulatorPcInstance)) {
				return;
			}
			if ((!pc.getLocation().isInScreen(obj.getLocation()) || (pc.getLocation().getTileLineDistance(obj.getLocation()) > 7))) {
				// 邀請組隊時，對象不再螢幕內或是7步內
				pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$1695));
				return;
			}

			// 轉型為玩家物件
			LsimulatorPcInstance targetPc = (LsimulatorPcInstance) obj;

			// 判斷目標是否屬於相同隊伍
			if (!targetPc.isInParty()) {
				pc.sendPackets(new S_ServerMessage(1696));
				return;
			}
			// 委任給其他玩家?
			pc.sendPackets(new S_Message_YN(LsimulatorSystemMessageId.$1703, ""));

			// 指定隊長給新的目標
			pc.getParty().passLeader(targetPc);
		}
	}

	@Override
	public String getType() {
		return C_CREATE_PARTY;
	}

}
