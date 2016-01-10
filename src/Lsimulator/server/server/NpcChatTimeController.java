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
package Lsimulator.server.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.datatables.NpcChatTable;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.templates.LsimulatorNpcChat;

public class NpcChatTimeController implements Runnable {
	private static Logger _log = Logger.getLogger(NpcChatTimeController.class.getName());

	private static NpcChatTimeController _instance;

	public static NpcChatTimeController getInstance() {
		if (_instance == null) {
			_instance = new NpcChatTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkNpcChatTime(); // 檢查開始聊天時間
				Thread.sleep(60000);
			}
		} catch (Exception e1) {
			_log.warning(e1.getMessage());
		}
	}

	private void checkNpcChatTime() {
		for (LsimulatorNpcChat npcChat : NpcChatTable.getInstance().getAllGameTime()) {
			if (isChatTime(npcChat.getGameTime())) {
				int npcId = npcChat.getNpcId();
				for (LsimulatorObject obj : LsimulatorWorld.getInstance().getObject()) {
					if (!(obj instanceof LsimulatorNpcInstance)) {
						continue;
					}
					LsimulatorNpcInstance npc = (LsimulatorNpcInstance) obj;
					if (npc.getNpcTemplate().get_npcId() == npcId) {
						npc.startChat(LsimulatorNpcInstance.CHAT_TIMING_GAME_TIME);
					}
				}
			}
		}
	}

	private boolean isChatTime(int chatTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		Calendar realTime = getRealTime();
		int nowTime = Integer.valueOf(sdf.format(realTime.getTime()));
		return (nowTime == chatTime);
	}

	private static Calendar getRealTime() {
		TimeZone _tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(_tz);
		return cal;
	}

}
