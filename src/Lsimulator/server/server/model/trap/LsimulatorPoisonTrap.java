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
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.poison.LsimulatorDamagePoison;
import Lsimulator.server.server.model.poison.LsimulatorParalysisPoison;
import Lsimulator.server.server.model.poison.LsimulatorSilencePoison;
import Lsimulator.server.server.storage.TrapStorage;

public class LsimulatorPoisonTrap extends LsimulatorTrap {
	private final String _type;
	private final int _delay;
	private final int _time;
	private final int _damage;

	public LsimulatorPoisonTrap(TrapStorage storage) {
		super(storage);

		_type = storage.getString("poisonType");
		_delay = storage.getInt("poisonDelay");
		_time = storage.getInt("poisonTime");
		_damage = storage.getInt("poisonDamage");
	}

	@Override
	public void onTrod(PcInstance trodFrom, LsimulatorObject trapObj) {
		sendEffect(trapObj);

		if (_type.equals("d")) {
			LsimulatorDamagePoison.doInfection(trodFrom, trodFrom, _time, _damage);
		} else if (_type.equals("s")) {
			LsimulatorSilencePoison.doInfection(trodFrom);
		} else if (_type.equals("p")) {
			LsimulatorParalysisPoison.doInfection(trodFrom, _delay, _time);
		}
	}
}
