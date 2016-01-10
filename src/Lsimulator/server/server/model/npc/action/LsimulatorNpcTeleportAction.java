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

import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

public class LsimulatorNpcTeleportAction extends LsimulatorNpcXmlAction {
	private final LsimulatorLocation _loc;
	private final int _heading;
	private final int _price;
	private final boolean _effect;

	public LsimulatorNpcTeleportAction(Element element) {
		super(element);

		int x = LsimulatorNpcXmlParser.getIntAttribute(element, "X", -1);
		int y = LsimulatorNpcXmlParser.getIntAttribute(element, "Y", -1);
		int mapId = LsimulatorNpcXmlParser.getIntAttribute(element, "Map", -1);
		_loc = new LsimulatorLocation(x, y, mapId);

		_heading = LsimulatorNpcXmlParser.getIntAttribute(element, "Heading", 5);

		_price = LsimulatorNpcXmlParser.getIntAttribute(element, "Price", 0);
		_effect = LsimulatorNpcXmlParser.getBoolAttribute(element, "Effect", true);
	}

	@Override
	public LsimulatorNpcHtml execute(String actionName, LsimulatorPcInstance pc, LsimulatorObject obj,
			byte[] args) {
		if (!pc.getInventory().checkItem(LsimulatorItemId.ADENA, _price)) {
			pc.sendPackets(new S_ServerMessage(337, "$4")); // アデナが不足しています。
			return LsimulatorNpcHtml.HTML_CLOSE;
		}
		pc.getInventory().consumeItem(LsimulatorItemId.ADENA, _price);
		LsimulatorTeleport.teleport(pc, _loc.getX(), _loc.getY(), (short) _loc
				.getMapId(), _heading, _effect);
		return null;
	}

}
