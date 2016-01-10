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
package Lsimulator.server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.LsimulatorMessage;
import Lsimulator.server.console.ConsoleProcess;
import Lsimulator.server.server.datatables.CastleTable;
import Lsimulator.server.server.datatables.CharacterTable;
import Lsimulator.server.server.datatables.ChatLogTable;
import Lsimulator.server.server.datatables.ClanTable;
import Lsimulator.server.server.datatables.DoorTable;
import Lsimulator.server.server.datatables.DropTable;
import Lsimulator.server.server.datatables.DropItemTable;
import Lsimulator.server.server.datatables.FurnitureItemTable;
import Lsimulator.server.server.datatables.FurnitureSpawnTable;
import Lsimulator.server.server.datatables.GetBackRestartTable;
import Lsimulator.server.server.datatables.InnTable;
import Lsimulator.server.server.datatables.IpTable;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.datatables.MagicDollTable;
import Lsimulator.server.server.datatables.MailTable;
import Lsimulator.server.server.datatables.MapsTable;
import Lsimulator.server.server.datatables.MobGroupTable;
import Lsimulator.server.server.datatables.NpcActionTable;
import Lsimulator.server.server.datatables.NpcChatTable;
import Lsimulator.server.server.datatables.NpcSpawnTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.datatables.PetTable;
import Lsimulator.server.server.datatables.PetTypeTable;
import Lsimulator.server.server.datatables.PolyTable;
import Lsimulator.server.server.datatables.RaceTicketTable;
import Lsimulator.server.server.datatables.ResolventTable;
import Lsimulator.server.server.datatables.ShopTable;
import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.datatables.SpawnTable;
import Lsimulator.server.server.datatables.SprTable;
import Lsimulator.server.server.datatables.UBSpawnTable;
import Lsimulator.server.server.datatables.WeaponSkillTable;
import Lsimulator.server.server.model.Dungeon;
import Lsimulator.server.server.model.ElementalStoneGenerator;
import Lsimulator.server.server.model.Getback;
import Lsimulator.server.server.model.LsimulatorBossCycle;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorDeleteItemOnGround;
import Lsimulator.server.server.model.LsimulatorNpcRegenerationTimer;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.game.LsimulatorBugBearRace;
import Lsimulator.server.server.model.gametime.LsimulatorGameTimeClock;
import Lsimulator.server.server.model.item.LsimulatorTreasureBox;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;
import Lsimulator.server.server.model.npc.action.LsimulatorNpcDefaultAction;
import Lsimulator.server.server.model.trap.LsimulatorWorldTraps;
import Lsimulator.server.server.storage.mysql.MysqlAutoBackup;
import Lsimulator.server.server.utils.MysqlAutoBackupTimer;
import Lsimulator.server.server.utils.SystemUtil;

// Referenced classes of package Lsimulator.server.server:
// ClientThread, Logins, RateTable, IdFactory,
// LoginController, GameTimeController, Announcements,
// MobTable, SpawnTable, SkillsTable, PolyTable,
// TeleportLocations, ShopTable, NPCTalkDataTable, NpcSpawnTable,
// IpTable, Shutdown, NpcTable, MobGroupTable, NpcShoutTable

public class GameServer extends Thread {
	private static Logger _log = Logger.getLogger(GameServer.class.getName());
	private static int YesNoCount = 0;
	public final int startTime = (int) (System.currentTimeMillis() / 1000);
	private ServerSocket _serverSocket;
	private int _port;
	private LoginController _loginController;
	private int chatlvl;

	@Override
	public void run() {
		System.out.println(LsimulatorMessage.memoryUse + SystemUtil.getUsedMemoryMB() + LsimulatorMessage.memory);
		System.out.println(LsimulatorMessage.waitingforuser);
		while (true) {
			try {
				Socket socket = _serverSocket.accept();
				System.out.println(LsimulatorMessage.from + socket.getInetAddress()+ LsimulatorMessage.attempt);
				String host = socket.getInetAddress().getHostAddress();
				if (IpTable.getInstance().isBannedIp(host)) {
					_log.info("banned IP(" + host + ")");
				} else {
					ClientThread client = new ClientThread(socket);
					GeneralThreadPool.getInstance().execute(client);
				}
			} catch (IOException ioexception) {
			}
		}
	}

	private static GameServer _instance;

	private GameServer() {
		super("GameServer");
	}

	public static GameServer getInstance() {
		if (_instance == null) {
			_instance = new GameServer();
		}
		return _instance;
	}

	public void initialize() throws Exception {
		String s = Config.WORLD_SERVER_HOST_NAME;
		double rateXp = Config.RATE_XP;
		double LA = Config.RATE_LA;
		double rateKarma = Config.RATE_KARMA;
		double rateDropItems = Config.RATE_DROP_ITEMS;
		double rateDropAdena = Config.RATE_DROP_ADENA;

		// Locale 多國語系
		LsimulatorMessage.getInstance();

		chatlvl = Config.GLOBAL_CHAT_LEVEL;
		_port = Config.WORLD_SERVER_PORT;
		if (!"*".equals(s)) {
			InetAddress inetaddress = InetAddress.getByName(s);
			inetaddress.getHostAddress();
			_serverSocket = new ServerSocket(_port, 50, inetaddress);
			System.out.println(LsimulatorMessage.setporton + _port);
		} else {
			_serverSocket = new ServerSocket(_port);
			System.out.println(LsimulatorMessage.setporton + _port);
		}

		System.out.println("┌───────────────────────────────┐");
		System.out.println("│     " + LsimulatorMessage.ver + "\t" + "\t" + "│");
		System.out.println("└───────────────────────────────┘" + "\n");

		System.out.println(LsimulatorMessage.settingslist + "\n");
		System.out.println("┌" + LsimulatorMessage.exp + ": " + (rateXp) + LsimulatorMessage.x
				+ "\n\r├" + LsimulatorMessage.justice + ": " + (LA) + LsimulatorMessage.x
				+ "\n\r├" + LsimulatorMessage.karma + ": " + (rateKarma) + LsimulatorMessage.x
				+ "\n\r├" + LsimulatorMessage.dropitems + ": " + (rateDropItems)+ LsimulatorMessage.x 
				+ "\n\r├" + LsimulatorMessage.dropadena + ": "+ (rateDropAdena) + LsimulatorMessage.x 
				+ "\n\r├"+ LsimulatorMessage.enchantweapon + ": "+ (Config.ENCHANT_CHANCE_WEAPON) + "%" 
				+ "\n\r├"+ LsimulatorMessage.enchantarmor + ": " + (Config.ENCHANT_CHANCE_ARMOR)+ "%");
		System.out.println("├" + LsimulatorMessage.chatlevel + ": " + (chatlvl)+ LsimulatorMessage.level);

		if (Config.ALT_NONPVP) { // Non-PvP設定
			System.out.println("└" + LsimulatorMessage.nonpvpNo + "\n");
		} else {
			System.out.println("└" + LsimulatorMessage.nonpvpYes + "\n");
		}

		int maxOnlineUsers = Config.MAX_ONLINE_USERS;
		System.out.println(LsimulatorMessage.maxplayer + (maxOnlineUsers)
				+ LsimulatorMessage.player);

		System.out.println("┌───────────────────────────────┐");
		System.out.println("│     " + LsimulatorMessage.ver + "\t" + "\t" + "│");
		System.out.println("└───────────────────────────────┘" + "\n");

		IdFactory.getInstance();
		LsimulatorWorldMap.getInstance();
		_loginController = LoginController.getInstance();
		_loginController.setMaxAllowedOnlinePlayers(maxOnlineUsers);

		// 讀取所有角色名稱
		CharacterTable.getInstance().loadAllCharName();

		// 初始化角色的上線狀態
		CharacterTable.clearOnlineStatus();

		// 初始化遊戲時間
		LsimulatorGameTimeClock.init();

		// 初始化無限大戰
		UbTimeController ubTimeContoroller = UbTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(ubTimeContoroller);
		
		// 初始化攻城
		WarTimeController warTimeController = WarTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(warTimeController);
		
		// 設定精靈石的產生
		if (Config.ELEMENTAL_STONE_AMOUNT > 0) {
			ElementalStoneGenerator elementalStoneGenerator = ElementalStoneGenerator.getInstance();
			GeneralThreadPool.getInstance().execute(elementalStoneGenerator);
		}

		// 初始化 HomeTown 時間
		HomeTownTimeController.getInstance();

		// 初始化盟屋拍賣
		AuctionTimeController auctionTimeController = AuctionTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(auctionTimeController);

		// 初始化盟屋的稅金
		HouseTaxTimeController houseTaxTimeController = HouseTaxTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(houseTaxTimeController);

		// 初始化釣魚
		FishingTimeController fishingTimeController = FishingTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(fishingTimeController);

		// 初始化 NPC 聊天
		NpcChatTimeController npcChatTimeController = NpcChatTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(npcChatTimeController);

		// 初始化 Light
		LightTimeController lightTimeController = LightTimeController.getInstance();
		GeneralThreadPool.getInstance().execute(lightTimeController);

		// 初始化遊戲公告
		Announcements.getInstance();
		
		// 初始化遊戲循環公告
	    AnnouncementsCycle.getInstance();

		// 初始化MySQL自動備份程序
		MysqlAutoBackup.getInstance();

		// 開始 MySQL自動備份程序 計時器
		MysqlAutoBackupTimer.TimerStart();
		
		// 初始化帳號使用狀態
		Account.InitialOnlineStatus();

		NpcTable.getInstance();
		LsimulatorDeleteItemOnGround deleteitem = new LsimulatorDeleteItemOnGround();
		deleteitem.initialize();

		if (!NpcTable.getInstance().isInitialized()) {
			throw new Exception("Could not initialize the npc table");
		}
		LsimulatorNpcDefaultAction.getInstance();
		DoorTable.initialize();
		SpawnTable.getInstance();
		MobGroupTable.getInstance();
		SkillsTable.getInstance();
		PolyTable.getInstance();
		ItemTable.getInstance();
		DropTable.getInstance();
		DropItemTable.getInstance();
		ShopTable.getInstance();
		NPCTalkDataTable.getInstance();
		LsimulatorWorld.getInstance();
		LsimulatorWorldTraps.getInstance();
		Dungeon.getInstance();
		NpcSpawnTable.getInstance();
		IpTable.getInstance();
		MapsTable.getInstance();
		UBSpawnTable.getInstance();
		PetTable.getInstance();
		ClanTable.getInstance();
		CastleTable.getInstance();
		LsimulatorCastleLocation.setCastleTaxRate(); // 必須在 CastleTable 初始化之後
		GetBackRestartTable.getInstance();
		GeneralThreadPool.getInstance();
		LsimulatorNpcRegenerationTimer.getInstance();
		ChatLogTable.getInstance();
		WeaponSkillTable.getInstance();
		NpcActionTable.load();
		GMCommandsConfig.load();
		Getback.loadGetBack();
		PetTypeTable.load();
		LsimulatorBossCycle.load();
		LsimulatorTreasureBox.load();
		SprTable.getInstance();
		ResolventTable.getInstance();
		FurnitureSpawnTable.getInstance();
		NpcChatTable.getInstance();
		MailTable.getInstance();
		RaceTicketTable.getInstance();
		LsimulatorBugBearRace.getInstance();
		InnTable.getInstance();
		MagicDollTable.getInstance();
		FurnitureItemTable.getInstance();

		System.out.println(LsimulatorMessage.initialfinished);
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		// cmd互動指令
		Thread cp = new ConsoleProcess();
		cp.start();
		
		this.start();
	}

	/**
	 * 踢掉世界地圖中所有的玩家與儲存資料。
	 */
	public void disconnectAllCharacters() {
		Collection<LsimulatorPcInstance> players = LsimulatorWorld.getInstance()
				.getAllPlayers();
		for (LsimulatorPcInstance pc : players) {
			pc.getNetConnection().setActiveChar(null);
			pc.getNetConnection().kick();
		}
		// 踢除所有在線上的玩家
		for (LsimulatorPcInstance pc : players) {
			ClientThread.quitGame(pc);
			LsimulatorWorld.getInstance().removeObject(pc);
			Account account = Account.load(pc.getAccountName());
			Account.online(account, false);
		}
	}

	private class ServerShutdownThread extends Thread {
		private final int _secondsCount;

		public ServerShutdownThread(int secondsCount) {
			_secondsCount = secondsCount;
		}

		@Override
		public void run() {
			LsimulatorWorld world = LsimulatorWorld.getInstance();
			try {
				int secondsCount = _secondsCount;
				world.broadcastServerMessage("伺服器即將關閉。");
				world.broadcastServerMessage("請玩家移動到安全區域先行登出");
				while (0 < secondsCount) {
					if (secondsCount <= 30) {
						world.broadcastServerMessage("伺服器將在" + secondsCount
								+ "秒後關閉，請玩家移動到安全區域先行登出。");
					} else {
						if (secondsCount % 60 == 0) {
							world.broadcastServerMessage("伺服器將在" + secondsCount
									/ 60 + "分鐘後關閉。");
						}
					}
					Thread.sleep(1000);
					secondsCount--;
				}
				shutdown();
			} catch (InterruptedException e) {
				world.broadcastServerMessage("已取消伺服器關機。伺服器將會正常運作。");
				return;
			}
		}
	}

	private ServerShutdownThread _shutdownThread = null;

	public synchronized void shutdownWithCountdown(int secondsCount) {
		if (_shutdownThread != null) {
			// 如果正在關閉
			// TODO 可能要有錯誤通知之類的
			return;
		}
		_shutdownThread = new ServerShutdownThread(secondsCount);
		GeneralThreadPool.getInstance().execute(_shutdownThread);
	}

	public void shutdown() {
		disconnectAllCharacters();
		System.exit(0);
	}

	public synchronized void abortShutdown() {
		if (_shutdownThread == null) {
			// 如果正在關閉
			// TODO 可能要有錯誤通知之類的
			return;
		}

		_shutdownThread.interrupt();
		_shutdownThread = null;
	}

	/**
	 * 取得世界中發送YesNo總次數
	 * @return YesNo總次數
	 */
	public static int getYesNoCount() {
		YesNoCount += 1;
		return YesNoCount;
	}
}
