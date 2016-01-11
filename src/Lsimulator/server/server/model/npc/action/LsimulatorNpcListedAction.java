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

import java.util.List;

import org.w3c.dom.Element;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;

public class LsimulatorNpcListedAction extends LsimulatorNpcXmlAction {
	private List<LsimulatorNpcAction> _actions;

	public LsimulatorNpcListedAction(Element element) {
		super(element);
		_actions = LsimulatorNpcXmlParser.listActions(element);
	}

	@Override
	public LsimulatorNpcHtml execute(String actionName, PcInstance pc, LsimulatorObject obj,
			byte[] args) {
		LsimulatorNpcHtml result = null;
		for (LsimulatorNpcAction action : _actions) {
			if (!action.acceptsRequest(actionName, pc, obj)) {
				continue;
			}
			LsimulatorNpcHtml r = action.execute(actionName, pc, obj, args);
			if (r != null) {
				result = r;
			}
		}
		return result;
	}

	@Override
	public LsimulatorNpcHtml executeWithAmount(String actionName, PcInstance pc,
			LsimulatorObject obj, int amount) {
		LsimulatorNpcHtml result = null;
		for (LsimulatorNpcAction action : _actions) {
			if (!action.acceptsRequest(actionName, pc, obj)) {
				continue;
			}
			LsimulatorNpcHtml r = action.executeWithAmount(actionName, pc, obj, amount);
			if (r != null) {
				result = r;
			}
		}
		return result;
	}
}
