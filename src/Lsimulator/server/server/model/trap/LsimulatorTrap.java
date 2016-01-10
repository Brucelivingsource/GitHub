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
package Lsimulator.server.server.model.trap;

import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_EffectLocation;
import Lsimulator.server.server.storage.TrapStorage;

public abstract class LsimulatorTrap {
	protected final int _id;
	protected final int _gfxId;
	protected final boolean _isDetectionable;

	public LsimulatorTrap(TrapStorage storage) {
		_id = storage.getInt("id");
		_gfxId = storage.getInt("gfxId");
		_isDetectionable = storage.getBoolean("isDetectionable");
	}

	public LsimulatorTrap(int id, int gfxId, boolean detectionable) {
		_id = id;
		_gfxId = gfxId;
		_isDetectionable = detectionable;
	}

	public int getId() {
		return _id;
	}

	public int getGfxId() {
		return _gfxId;
	}

	protected void sendEffect(LsimulatorObject trapObj) {
		if (getGfxId() == 0) {
			return;
		}
		S_EffectLocation effect = new S_EffectLocation(trapObj.getLocation(),
				getGfxId());

		for (LsimulatorPcInstance pc : LsimulatorWorld.getInstance()
				.getRecognizePlayer(trapObj)) {
			pc.sendPackets(effect);
		}
	}

	public abstract void onTrod(LsimulatorPcInstance trodFrom, LsimulatorObject trapObj);

	public void onDetection(LsimulatorPcInstance caster, LsimulatorObject trapObj) {
		if (_isDetectionable) {
			sendEffect(trapObj);
		}
	}

	public static LsimulatorTrap newNull() {
		return new LsimulatorNullTrap();
	}
}

class LsimulatorNullTrap extends LsimulatorTrap {
	public LsimulatorNullTrap() {
		super(0, 0, false);
	}

	@Override
	public void onTrod(LsimulatorPcInstance trodFrom, LsimulatorObject trapObj) {
	}
}