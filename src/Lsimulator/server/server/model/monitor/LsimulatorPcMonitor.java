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
package Lsimulator.server.server.model.monitor;

import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;

/**
 * LsimulatorPcInstanceの定期処理、監視処理等を行う為の共通的な処理を実装した抽象クラス
 * 
 * 各タスク処理は{@link #run()}ではなく{@link #execTask(LsimulatorPcInstance)}にて実装する。
 * PCがログアウトするなどしてサーバ上に存在しなくなった場合、run()メソッドでは即座にリターンする。
 * その場合、タスクが定期実行スケジューリングされていたら、ログアウト処理等でスケジューリングを停止する必要がある。
 * 停止しなければタスクは止まらず、永遠に定期実行されることになる。
 * 定期実行でなく単発アクションの場合はそのような制御は不要。
 * 
 * LsimulatorPcInstanceの参照を直接持つことは望ましくない。
 * 
 * @author frefre
 *
 */
public abstract class LsimulatorPcMonitor implements Runnable {

	/** モニター対象LsimulatorPcInstanceのオブジェクトID */
	protected int _id;

	/**
	 * 指定されたパラメータでLsimulatorPcInstanceに対するモニターを作成する。
	 * @param oId {@link LsimulatorPcInstance#getId()}で取得できるオブジェクトID
	 */
	public LsimulatorPcMonitor(int oId) {
		_id = oId;
	}

	@Override
	public final void run() {
		LsimulatorPcInstance pc = (LsimulatorPcInstance) LsimulatorWorld.getInstance().findObject(_id);
		if (pc == null || pc.getNetConnection() == null) {
			return;
		}
		execTask(pc);
	}

	/**
	 * タスク実行時の処理
	 * @param pc モニター対象のPC
	 */
	public abstract void execTask(LsimulatorPcInstance pc);
}
