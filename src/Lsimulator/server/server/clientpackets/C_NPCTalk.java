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
import Lsimulator.server.server.datatables.NpcActionTable;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;
import Lsimulator.server.server.model.npc.action.LsimulatorNpcAction;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket, C_NPCTalk

/**
 * 處理收到由客戶端傳來NPC講話的封包
 */
public class C_NPCTalk extends ClientBasePacket {

	private static final String C_NPC_TALK = "[C] C_NPCTalk";
	private static Logger _log = Logger.getLogger(C_NPCTalk.class.getName());

	public C_NPCTalk(byte abyte0[], ClientThread client)
			throws Exception {
		super(abyte0);
		
		PcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}
		
		int objid = readD();
		LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(objid);
		
		if (obj != null) {
			LsimulatorNpcAction action = NpcActionTable.getInstance().get(pc, obj);
			if (action != null) {
				LsimulatorNpcHtml html = action.execute("", pc, obj, new byte[0]);
				if (html != null) {
					pc.sendPackets(new S_NPCTalkReturn(obj.getId(), html));
				}
				return;
			}
			obj.onTalkAction(pc);
		} else {
			_log.severe("不正確的NPC objid=" + objid);
		}
	}

	@Override
	public String getType() {
		return C_NPC_TALK;
	}
}
