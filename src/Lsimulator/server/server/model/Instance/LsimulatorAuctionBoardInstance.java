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
package Lsimulator.server.server.model.Instance;

import Lsimulator.server.server.serverpackets.S_AuctionBoard;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class LsimulatorAuctionBoardInstance extends LsimulatorNpcInstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LsimulatorAuctionBoardInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(LsimulatorPcInstance pc) {
		pc.sendPackets(new S_AuctionBoard(this));
	}

}
