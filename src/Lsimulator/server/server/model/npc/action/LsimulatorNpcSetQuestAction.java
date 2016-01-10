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
package Lsimulator.server.server.model.npc.action;

import org.w3c.dom.Element;

import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;

public class LsimulatorNpcSetQuestAction extends LsimulatorNpcXmlAction {
	private final int _id;
	private final int _step;

	public LsimulatorNpcSetQuestAction(Element element) {
		super(element);

		_id = LsimulatorNpcXmlParser.parseQuestId(element.getAttribute("Id"));
		_step = LsimulatorNpcXmlParser.parseQuestStep(element.getAttribute("Step"));

		if (_id == -1 || _step == -1) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public LsimulatorNpcHtml execute(String actionName, LsimulatorPcInstance pc, LsimulatorObject obj,
			byte[] args) {
		pc.getQuest().set_step(_id, _step);
		return null;
	}

}
