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
import Lsimulator.server.server.model.Instance.BoardInstance;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket, C_BoardPage

/**
 * 收到由客戶端傳送公告欄回到上一頁的封包
 */
public class C_BoardBack extends ClientBasePacket {

	private static final String C_BOARD_BACK = "[C] C_BoardBack";

	public C_BoardBack(byte abyte0[], ClientThread client) {
		super(abyte0);
		int objId = readD();
		int topicNumber = readD();
		LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(objId);
		BoardInstance board = (BoardInstance) obj;
		board.onAction(client.getActiveChar(), topicNumber);
	}

	@Override
	public String getType() {
		return C_BOARD_BACK;
	}

}
