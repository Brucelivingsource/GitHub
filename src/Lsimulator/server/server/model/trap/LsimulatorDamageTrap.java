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
import Lsimulator.server.server.storage.TrapStorage;
import Lsimulator.server.server.utils.Dice;

public class LsimulatorDamageTrap extends LsimulatorTrap {
	private final Dice _dice;
	private final int _base;
	private final int _diceCount;

	public LsimulatorDamageTrap(TrapStorage storage) {
		super(storage);

		_dice = new Dice(storage.getInt("dice"));
		_base = storage.getInt("base");
		_diceCount = storage.getInt("diceCount");
	}

	@Override
	public void onTrod(PcInstance trodFrom, LsimulatorObject trapObj) {
		sendEffect(trapObj);

		int dmg = _dice.roll(_diceCount) + _base;

		trodFrom.receiveDamage(trodFrom, dmg, false);
	}
}
