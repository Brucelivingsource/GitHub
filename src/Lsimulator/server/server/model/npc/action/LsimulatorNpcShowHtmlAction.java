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

import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;
import Lsimulator.server.server.utils.IterableElementList;
import Lsimulator.server.server.utils.collections.Lists;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LsimulatorNpcShowHtmlAction extends LsimulatorNpcXmlAction {
	private final String _htmlId;

	private final String[] _args;

	public LsimulatorNpcShowHtmlAction(Element element) {
		super(element);

		_htmlId = element.getAttribute("HtmlId");
		NodeList list = element.getChildNodes();
		List<String> dataList = Lists.newList();
		for (Element elem : new IterableElementList(list)) {
			if (elem.getNodeName().equalsIgnoreCase("Data")) {
				dataList.add(elem.getAttribute("Value"));
			}
		}
		_args = dataList.toArray(new String[dataList.size()]);
	}

	@Override
	public LsimulatorNpcHtml execute(String actionName, PcInstance pc, LsimulatorObject obj, byte[] args) {
		return new LsimulatorNpcHtml(_htmlId, _args);
	}

}
