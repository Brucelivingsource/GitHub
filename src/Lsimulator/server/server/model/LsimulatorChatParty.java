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
package Lsimulator.server.server.model;

import java.util.List;

import Lsimulator.server.Config;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.utils.collections.Lists;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorChatParty

public class LsimulatorChatParty {
	private final List<PcInstance> _membersList = Lists.newList();

	private PcInstance _leader = null;

	public void addMember(PcInstance pc) {
		if (pc == null) {
			throw new NullPointerException();
		}
		if (((_membersList.size() == Config.MAX_CHAT_PT) && !_leader.isGm()) || _membersList.contains(pc)) {
			return;
		}

		if (_membersList.isEmpty()) {
			// 最初のPTメンバーであればリーダーにする
			setLeader(pc);
		}

		_membersList.add(pc);
		pc.setChatParty(this);
	}

	private void removeMember(PcInstance pc) {
		if (!_membersList.contains(pc)) {
			return;
		}

		_membersList.remove(pc);
		pc.setChatParty(null);
	}

	public boolean isVacancy() {
		return _membersList.size() < Config.MAX_CHAT_PT;
	}

	public int getVacancy() {
		return Config.MAX_CHAT_PT - _membersList.size();
	}

	public boolean isMember(PcInstance pc) {
		return _membersList.contains(pc);
	}

	private void setLeader(PcInstance pc) {
		_leader = pc;
	}

	public PcInstance getLeader() {
		return _leader;
	}

	public boolean isLeader(PcInstance pc) {
		return pc.getId() == _leader.getId();
	}

	public String getMembersNameList() {
		String _result = new String("");
		for (PcInstance pc : _membersList) {
			_result = _result + pc.getName() + " ";
		}
		return _result;
	}

	private void breakup() {
		PcInstance[] members = getMembers();

		for (PcInstance member : members) {
			removeMember(member);
			member.sendPackets(new S_ServerMessage(418)); // パーティーを解散しました。
		}
	}

	public void leaveMember(PcInstance pc) {
		PcInstance[] members = getMembers();
		if (isLeader(pc)) {
			// パーティーリーダーの場合
			breakup();
		}
		else {
			// パーティーリーダーでない場合
			if (getNumOfMembers() == 2) {
				// パーティーメンバーが自分とリーダーのみ
				removeMember(pc);
				PcInstance leader = getLeader();
				removeMember(leader);

				sendLeftMessage(pc, pc);
				sendLeftMessage(leader, pc);
			}
			else {
				// 残りのパーティーメンバーが２人以上いる
				removeMember(pc);
				for (PcInstance member : members) {
					sendLeftMessage(member, pc);
				}
				sendLeftMessage(pc, pc);
			}
		}
	}

	public void kickMember(PcInstance pc) {
		if (getNumOfMembers() == 2) {
			// パーティーメンバーが自分とリーダーのみ
			removeMember(pc);
			PcInstance leader = getLeader();
			removeMember(leader);
		}
		else {
			// 残りのパーティーメンバーが２人以上いる
			removeMember(pc);
		}
		pc.sendPackets(new S_ServerMessage(419)); // パーティーから追放されました。
	}

	public PcInstance[] getMembers() {
		return _membersList.toArray(new PcInstance[_membersList.size()]);
	}

	public int getNumOfMembers() {
		return _membersList.size();
	}

	private void sendLeftMessage(PcInstance sendTo, PcInstance left) {
		// %0がパーティーから去りました。
		sendTo.sendPackets(new S_ServerMessage(420, left.getName()));
	}

}
