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
package Lsimulator.server.server.datatables;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.npc.action.LsimulatorNpcAction;
import Lsimulator.server.server.model.npc.action.LsimulatorNpcXmlParser;
import Lsimulator.server.server.utils.FileUtil;
import Lsimulator.server.server.utils.PerformanceTimer;
import Lsimulator.server.server.utils.collections.Lists;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NpcActionTable {
	private static Logger _log = Logger.getLogger(NpcActionTable.class.getName());

	private static NpcActionTable _instance;

	private final List<LsimulatorNpcAction> _actions = Lists.newList();

	private final List<LsimulatorNpcAction> _talkActions = Lists.newList();

	private List<LsimulatorNpcAction> loadAction(File file, String nodeName)

	throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(file);

		if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase(nodeName)) {
			return Lists.newList();
		}
		return LsimulatorNpcXmlParser.listActions(doc.getDocumentElement());
	}

	private void loadAction(File file) throws Exception {
		_actions.addAll(loadAction(file, "NpcActionList"));
	}

	private void loadTalkAction(File file) throws Exception {
		_talkActions.addAll(loadAction(file, "NpcTalkActionList"));
	}

	private void loadDirectoryActions(File dir) throws Exception {
		for (String file : dir.list()) {
			File f = new File(dir, file);
			if (FileUtil.getExtension(f).equalsIgnoreCase("xml")) {
				loadAction(f);
				loadTalkAction(f);
			}
		}
	}

	private NpcActionTable() throws Exception {
		File usersDir = new File("./data/xml/NpcActions/users/");
		if (usersDir.exists()) {
			loadDirectoryActions(usersDir);
		}
		loadDirectoryActions(new File("./data/xml/NpcActions/"));
	}

	public static void load() {
		try {
			PerformanceTimer timer = new PerformanceTimer();
			System.out.print("loading npcaction...");
			_instance = new NpcActionTable();
			System.out.println("OK! " + timer.get() + "ms");
		}
		catch (Exception e) {
			_log.log(Level.SEVERE, "NpcActionを読み込めませんでした", e);
			System.exit(0);
		}
	}

	public static NpcActionTable getInstance() {
		return _instance;
	}

	public LsimulatorNpcAction get(String actionName, PcInstance pc, LsimulatorObject obj) {
		for (LsimulatorNpcAction action : _actions) {
			if (action.acceptsRequest(actionName, pc, obj)) {
				return action;
			}
		}
		return null;
	}

	public LsimulatorNpcAction get(PcInstance pc, LsimulatorObject obj) {
		for (LsimulatorNpcAction action : _talkActions) {
			if (action.acceptsRequest("", pc, obj)) {
				return action;
			}
		}
		return null;
	}
}
