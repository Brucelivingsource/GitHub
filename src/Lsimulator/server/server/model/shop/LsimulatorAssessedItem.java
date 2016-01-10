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
package Lsimulator.server.server.model.shop;

import Lsimulator.server.server.datatables.RaceTicketTable;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.templates.LsimulatorRaceTicket;

public class LsimulatorAssessedItem {
	private final int _targetId;
	private int _assessedPrice;

	LsimulatorAssessedItem(int targetId, int assessedPrice) {
		_targetId = targetId;
		LsimulatorItemInstance item = (LsimulatorItemInstance) LsimulatorWorld.getInstance()
				.findObject(getTargetId());
		if (item.getItemId() == 40309) {// Race Tickets
			LsimulatorRaceTicket ticket = RaceTicketTable.getInstance().getTemplate(
					_targetId);
			int price = 0;
			if (ticket != null) {
				price = (int) (assessedPrice
						* ticket.get_allotment_percentage() * ticket
						.get_victory());
			}
			_assessedPrice = price;
		} else {
			_assessedPrice = assessedPrice;
		}
	}

	public int getTargetId() {
		return _targetId;
	}

	public int getAssessedPrice() {
		return _assessedPrice;
	}
}
