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

import java.util.logging.Logger;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.templates.LsimulatorBoardTopic;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 收到由客戶端傳送寫入公告欄的封包
 */
public class C_BoardWrite extends ClientBasePacket {

	private static final String C_BOARD_WRITE = "[C] C_BoardWrite";
	private static Logger _log = Logger.getLogger(C_BoardWrite.class.getName());

	public C_BoardWrite(byte decrypt[], ClientThread client) {
		super(decrypt);
		int id = readD();
		String title = readS();
		String content = readS();

		LsimulatorObject tg = LsimulatorWorld.getInstance().findObject(id);

		if (tg == null) {
			_log.warning("不正確的 NPCID : " + id);
			return;
		}

		LsimulatorPcInstance pc = client.getActiveChar();
		LsimulatorBoardTopic.create(pc.getName(), title, content);
		pc.getInventory().consumeItem(LsimulatorItemId.ADENA, 300);
	}

	@Override
	public String getType() {
		return C_BOARD_WRITE;
	}
}