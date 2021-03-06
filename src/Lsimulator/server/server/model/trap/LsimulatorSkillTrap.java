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
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.storage.TrapStorage;

public class LsimulatorSkillTrap extends LsimulatorTrap {
	private final int _skillId;
	private final int _skillTimeSeconds;

	public LsimulatorSkillTrap(TrapStorage storage) {
		super(storage);

		_skillId = storage.getInt("skillId");
		_skillTimeSeconds = storage.getInt("skillTimeSeconds");
	}

	@Override
	public void onTrod(PcInstance trodFrom, LsimulatorObject trapObj) {
		sendEffect(trapObj);

		new LsimulatorSkillUse().handleCommands(trodFrom, _skillId, trodFrom.getId(),
				trodFrom.getX(), trodFrom.getY(), null, _skillTimeSeconds,
				LsimulatorSkillUse.TYPE_GMBUFF);
	}

}
