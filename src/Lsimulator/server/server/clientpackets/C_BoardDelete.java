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
import Lsimulator.server.server.templates.LsimulatorBoardTopic;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 收到由客戶端傳送刪除公告欄的封包
 */
public class C_BoardDelete extends ClientBasePacket {

	private static final String C_BOARD_DELETE = "[C] C_BoardDelete";
	private static Logger _log = Logger
			.getLogger(C_BoardDelete.class.getName());

	public C_BoardDelete(byte decrypt[], ClientThread client) {
		super(decrypt);
		int objId = readD();
		int topicId = readD();
		LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(objId);
		if (obj == null) {
			_log.warning("不正確的NPCID : " + objId);
			return;
		}
		LsimulatorBoardTopic topic = LsimulatorBoardTopic.findById(topicId);
		if (topic == null) {
			logNotExist(topicId);
			return;
		}
		String name = client.getActiveChar().getName();
		if (!name.equals(topic.getName())) {
			logIllegalDeletion(topic, name);
			return;
		}

		topic.delete();
	}

	private void logNotExist(int topicId) {
		_log.warning(String
				.format("Illegal board deletion request: Topic id <%d> does not exist.",
						topicId));
	}

	private void logIllegalDeletion(LsimulatorBoardTopic topic, String name) {
		_log.warning(String
				.format("Illegal board deletion request: Name <%s> expected but was <%s>.",
						topic.getName(), name));
	}

	@Override
	public String getType() {
		return C_BOARD_DELETE;
	}
}