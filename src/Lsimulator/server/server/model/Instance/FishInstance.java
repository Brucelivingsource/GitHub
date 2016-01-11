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
package Lsimulator.server.server.model.Instance;

import java.util.Timer;
import java.util.TimerTask;

import Lsimulator.server.server.serverpackets.S_ChangeHeading;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_NPCPack;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.Random;

public class FishInstance extends NpcInstance {

	private static final long serialVersionUID = 1L;
	private fishTimer _fishTimer;

	public FishInstance(LsimulatorNpc template) {
		super(template);
		_fishTimer = new fishTimer(this);
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(_fishTimer, 1000, (Random.nextInt(30, 30) * 1000));
	}

	@Override
	public void onPerceive(PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_NPCPack(this));
	}

	private class fishTimer extends TimerTask {

		private FishInstance _fish;

		public fishTimer(FishInstance fish) {
			_fish = fish;
		}

		@Override
		public void run() {
			if (_fish != null) {
				_fish.setHeading(Random.nextInt(8)); // 隨機面向
				_fish.broadcastPacket(new S_ChangeHeading(_fish)); // 更新面向
				_fish.broadcastPacket(new S_DoActionGFX(_fish.getId(), 0)); // 動作
			} else {
				cancel();
			}
		}
	}

}
