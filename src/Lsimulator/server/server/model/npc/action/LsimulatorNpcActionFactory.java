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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.utils.collections.Maps;

import org.w3c.dom.Element;

public class LsimulatorNpcActionFactory {
	private static Logger _log = Logger.getLogger(LsimulatorNpcActionFactory.class.getName());

	private static Map<String, Constructor<? extends LsimulatorNpcXmlAction>> _actions = Maps.newMap();

	private static Constructor<? extends LsimulatorNpcXmlAction> loadConstructor(Class<? extends LsimulatorNpcXmlAction> c) throws NoSuchMethodException {
		return c.getConstructor(new Class[]
		{ Element.class });
	}

	static {
		try {
			_actions.put("Action", loadConstructor(LsimulatorNpcListedAction.class));
			_actions.put("MakeItem", loadConstructor(LsimulatorNpcMakeItemAction.class));
			_actions.put("ShowHtml", loadConstructor(LsimulatorNpcShowHtmlAction.class));
			_actions.put("SetQuest", loadConstructor(LsimulatorNpcSetQuestAction.class));
			_actions.put("Teleport", loadConstructor(LsimulatorNpcTeleportAction.class));
		}
		catch (NoSuchMethodException e) {
			_log.log(Level.SEVERE, "NpcActionのクラスロードに失敗", e);
		}
	}

	public static LsimulatorNpcAction newAction(Element element) {
		try {
			Constructor<? extends LsimulatorNpcXmlAction> con = _actions.get(element.getNodeName());
			return con.newInstance(element);
		}
		catch (NullPointerException e) {
			_log.warning(element.getNodeName() + " 未定義のNPCアクションです");
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, "NpcActionのクラスロードに失敗", e);
		}
		return null;
	}
}
