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
package Lsimulator.server.server.model;

import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_Paralysis;
import Lsimulator.server.server.templates.LsimulatorEtcItem;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorItemDelay

public class LsimulatorItemDelay {

	private LsimulatorItemDelay() {
	}

	static class ItemDelayTimer implements Runnable {
		private int _delayId;

		private LsimulatorCharacter _cha;

		public ItemDelayTimer(LsimulatorCharacter cha, int id) {
			_cha = cha;
			_delayId = id;
		}

		@Override
		public void run() {
			stopDelayTimer(_delayId);
		}

		public void stopDelayTimer(int delayId) {
			_cha.removeItemDelay(delayId);
		}
	}

	static class TeleportUnlockTimer implements Runnable {
		private LsimulatorPcInstance _pc;

		public TeleportUnlockTimer(LsimulatorPcInstance pc) {
			_pc = pc;
		}

		@Override
		public void run() {
			_pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT_UNLOCK,
					true));
		}
	}

	public static void onItemUse(ClientThread client, LsimulatorItemInstance item) {
		int delayId = 0;
		int delayTime = 0;

		LsimulatorPcInstance pc = client.getActiveChar();

		if (item.getItem().getType2() == 0) {
			// 種別：一般道具
			delayId = ((LsimulatorEtcItem) item.getItem()).get_delayid();
			delayTime = ((LsimulatorEtcItem) item.getItem()).get_delaytime();
		} else if (item.getItem().getType2() == 1) {
			// 種別：武器
			return;
		} else if (item.getItem().getType2() == 2) {
			// 種別：防具

			if ((item.getItem().getItemId() == 20077)
					|| (item.getItem().getItemId() == 20062)
					|| (item.getItem().getItemId() == 120077)) {
				// 隱身防具
				if (item.isEquipped() && !pc.isInvisble()) {
					pc.beginInvisTimer();
				}
			} else {
				return;
			}
		}

		ItemDelayTimer timer = new ItemDelayTimer(pc, delayId);
		pc.addItemDelay(delayId, timer);
		GeneralThreadPool.getInstance().schedule(timer, delayTime);
		
	}

	public static void teleportUnlock(LsimulatorPcInstance pc, LsimulatorItemInstance item) {
		int delayTime = ((LsimulatorEtcItem) item.getItem()).get_delaytime();
		TeleportUnlockTimer timer = new TeleportUnlockTimer(pc);
		GeneralThreadPool.getInstance().schedule(timer, delayTime);
	}

}
