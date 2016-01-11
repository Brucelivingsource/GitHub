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

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來NPC講話動作的封包
 */
public class C_NPCTalkAction extends ClientBasePacket {

	private static final String C_NPC_TALK_ACTION = "[C] C_NPCTalkAction";
	private static Logger _log = Logger.getLogger(C_NPCTalkAction.class
			.getName());

	public C_NPCTalkAction(byte decrypt[], ClientThread client)
			throws FileNotFoundException, Exception {
		super(decrypt);
		
		PcInstance activeChar = client.getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		int objectId = readD();
		String action = readS();

		LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(objectId);
		if (obj == null) {
			_log.warning("object not found, oid " + objectId);
			return;
		}

		try {
			NpcInstance npc = (NpcInstance) obj;
			npc.onFinalAction(activeChar, action);
		} catch (ClassCastException e) {
		}
	}

	@Override
	public String getType() {
		return C_NPC_TALK_ACTION;
	}

}
