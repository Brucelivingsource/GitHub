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
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorHouseLocation;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorBookMark;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到客戶端傳來新增書籤的封包
 */
public class C_AddBookmark extends ClientBasePacket {

	private static final String C_ADD_BOOKMARK = "[C] C_AddBookmark";

	public C_AddBookmark(byte[] decrypt, ClientThread client) {
		super(decrypt);
		
		PcInstance pc = client.getActiveChar();
		if ((pc == null) || pc.isGhost()) {
			return;
		}

		String s = readS();

		if (pc.getMap().isMarkable() || pc.isGm()) {
			if ((LsimulatorCastleLocation.checkInAllWarArea(pc.getX(), pc.getY(),
					pc.getMapId()) || LsimulatorHouseLocation.isInHouse(pc.getX(),
					pc.getY(), pc.getMapId()))
					&& !pc.isGm()) {
				// \f1ここを記憶することができません。
				pc.sendPackets(new S_ServerMessage(214));
			} else {
				LsimulatorBookMark.addBookmark(pc, s);
			}
		} else {
			// \f1ここを記憶することができません。
			pc.sendPackets(new S_ServerMessage(214));
		}
	}

	@Override
	public String getType() {
		return C_ADD_BOOKMARK;
	}
}
