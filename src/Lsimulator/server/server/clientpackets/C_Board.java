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
import Lsimulator.server.server.model.Instance.AuctionBoardInstance;
import Lsimulator.server.server.model.Instance.BoardInstance;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket, C_Board

/**
 * 收到由客戶端傳送打開公告欄的封包
 */
public class C_Board extends ClientBasePacket {

	private static final String C_BOARD = "[C] C_Board";

	private boolean isBoardInstance(LsimulatorObject obj) {
		return ((obj instanceof BoardInstance) || (obj instanceof AuctionBoardInstance));
	}

	public C_Board(byte abyte0[], ClientThread client) {
		super(abyte0);
		int objectId = readD();
		LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(objectId);
		if (!isBoardInstance(obj)) {
			return; // 對象不是佈告欄停止
		}
		obj.onAction(client.getActiveChar());
	}

	@Override
	public String getType() {
		return C_BOARD;
	}

}
