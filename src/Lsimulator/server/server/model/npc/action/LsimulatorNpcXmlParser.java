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
import java.util.Map;

import Lsimulator.server.server.model.LsimulatorQuest;
import Lsimulator.server.server.utils.IterableElementList;
import Lsimulator.server.server.utils.collections.Lists;
import Lsimulator.server.server.utils.collections.Maps;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LsimulatorNpcXmlParser {
	public static List<LsimulatorNpcAction> listActions(Element element) {
		List<LsimulatorNpcAction> result = Lists.newList();
		NodeList list = element.getChildNodes();
		for (Element elem : new IterableElementList(list)) {
			LsimulatorNpcAction action = LsimulatorNpcActionFactory.newAction(elem);
			if (action != null) {
				result.add(action);
			}
		}
		return result;
	}

	public static Element getFirstChildElementByTagName(Element element, String tagName) {
		IterableElementList list = new IterableElementList(element.getElementsByTagName(tagName));
		for (Element elem : list) {
			return elem;
		}
		return null;
	}

	public static int getIntAttribute(Element element, String name, int defaultValue) {
		int result = defaultValue;
		try {
			result = Integer.valueOf(element.getAttribute(name));
		}
		catch (NumberFormatException e) {}
		return result;
	}

	public static boolean getBoolAttribute(Element element, String name, boolean defaultValue) {
		boolean result = defaultValue;
		String value = element.getAttribute(name);
		if (!value.equals("")) {
			result = Boolean.valueOf(value);
		}
		return result;
	}

	private final static Map<String, Integer> _questIds = Maps.newMap();
	static {
		_questIds.put("level15", LsimulatorQuest.QUEST_LEVELsimulator5);
		_questIds.put("level30", LsimulatorQuest.QUEST_LEVEL30);
		_questIds.put("level45", LsimulatorQuest.QUEST_LEVEL45);
		_questIds.put("level50", LsimulatorQuest.QUEST_LEVEL50);
		_questIds.put("lyra", LsimulatorQuest.QUEST_LYRA);
		_questIds.put("oilskinmant", LsimulatorQuest.QUEST_OILSKINMANT);
		_questIds.put("doromond", LsimulatorQuest.QUEST_DOROMOND);
		_questIds.put("ruba", LsimulatorQuest.QUEST_RUBA);
		_questIds.put("lukein", LsimulatorQuest.QUEST_LUKEIN1);
		_questIds.put("tbox1", LsimulatorQuest.QUEST_TBOX1);
		_questIds.put("tbox2", LsimulatorQuest.QUEST_TBOX2);
		_questIds.put("tbox3", LsimulatorQuest.QUEST_TBOX3);
		_questIds.put("cadmus", LsimulatorQuest.QUEST_CADMUS);
		_questIds.put("resta", LsimulatorQuest.QUEST_RESTA);
		_questIds.put("kamyla", LsimulatorQuest.QUEST_KAMYLA);
		_questIds.put("lizard", LsimulatorQuest.QUEST_LIZARD);
		_questIds.put("desire", LsimulatorQuest.QUEST_DESIRE);
		_questIds.put("shadows", LsimulatorQuest.QUEST_SHADOWS);
		_questIds.put("toscroll", LsimulatorQuest.QUEST_TOSCROLL);
		_questIds.put("moonoflongbow", LsimulatorQuest.QUEST_MOONOFLONGBOW);
		_questIds.put("Generalhamelofresentment", LsimulatorQuest.QUEST_GENERALHAMELOFRESENTMENT);
	}

	public static int parseQuestId(String questId) {
		if (questId.equals("")) {
			return -1;
		}
		Integer result = _questIds.get(questId.toLowerCase());
		if (result == null) {
			throw new IllegalArgumentException();
		}
		return result;
	}

	public static int parseQuestStep(String questStep) {
		if (questStep.equals("")) {
			return -1;
		}
		if (questStep.equalsIgnoreCase("End")) {
			return LsimulatorQuest.QUEST_END;
		}
		return Integer.parseInt(questStep);
	}
}
