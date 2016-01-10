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

import java.util.Timer;
import java.util.TimerTask;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;

public class LsimulatorNpcDeleteTimer extends TimerTask {
	public LsimulatorNpcDeleteTimer(LsimulatorNpcInstance npc, int timeMillis) {
		_npc = npc;
		_timeMillis = timeMillis;
	}

	@Override
	public void run() {
		// 龍之門扉存在時間到時
		if (_npc != null) {
			if (_npc.getNpcId() == 81273 || _npc.getNpcId() == 81274 || _npc.getNpcId() == 81275
					|| _npc.getNpcId() == 81276 || _npc.getNpcId() == 81277) {
				if (_npc.getNpcId() == 81277) { // 隱匿的巨龍谷入口關閉
					LsimulatorDragonSlayer.getInstance().setHiddenDragonValleyStstus(0);
				}
				// 結束屠龍副本
				LsimulatorDragonSlayer.getInstance().setPortalPack(_npc.getPortalNumber(), null);
				LsimulatorDragonSlayer.getInstance().endDragonPortal(_npc.getPortalNumber());
				// 門扉消失動作
				_npc.setStatus(ActionCodes.ACTION_Die);
				_npc.broadcastPacket(new S_DoActionGFX(_npc.getId(), ActionCodes.ACTION_Die));
			}
			_npc.deleteMe();
			cancel();
		}
	}

	public void begin() {
		Timer timer = new Timer();
		timer.schedule(this, _timeMillis);
	}

	private final LsimulatorNpcInstance _npc;

	private final int _timeMillis;
}
