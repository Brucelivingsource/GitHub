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
package Lsimulator.server.server.model.npc;

public class LsimulatorNpcHtml {
	private final String _name;
	private final String _args[];

	public static final LsimulatorNpcHtml HTML_CLOSE = new LsimulatorNpcHtml("");

	public LsimulatorNpcHtml(String name) {
		this(name, new String[] {});
	}

	public LsimulatorNpcHtml(String name, String... args) {
		if (name == null || args == null) {
			throw new NullPointerException();
		}
		_name = name;
		_args = args;
	}

	public String getName() {
		return _name;
	}

	public String[] getArgs() {
		return _args;
	}
}
