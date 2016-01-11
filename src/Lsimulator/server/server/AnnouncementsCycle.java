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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javolution.util.FastList;
import Lsimulator.server.Config;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.StringTokenizer;

public class AnnouncementsCycle {

    private int round = 0; 
    private boolean firstboot = true; 
    private static AnnouncementsCycle _instance;

    /*
    改為固定開機讀取  之後不讀取
    */ 
    /**
     * announcementsCycle文件的位置
     */
    private static File dir = new File("data/announceCycle.txt");
 

    /**
     * 在公告首顯示公告修改時間
     */
    private boolean AnnounceTimeDisplay = Config.Announcements_Cycle_Modify_Time;

    /**
     * 容器
     */
    List<String> list = new FastList<String>();

    private AnnouncementsCycle() {
        cycle();
    }

    public static AnnouncementsCycle getInstance() {
        if (_instance == null) {
            _instance = new AnnouncementsCycle();
        }
        return _instance;
    }

    /**
     * 從announcementsCycle.txt將字串讀入
     */
    private void scanfile() {
        try {
            // NIO 讀檔
            if ( firstboot) { // 修改過或第一次 要讀檔
                Charset charset = Charset.forName("UTF-8");
                CharsetDecoder decoder = charset.newDecoder();
                FileInputStream in = new FileInputStream(dir);
                FileChannel inChannel = null;
                String allAnnounceS = new String();
                inChannel = in.getChannel();
                ByteBuffer inBuffer = ByteBuffer.allocateDirect(1000);
                CharBuffer outBuffer = ByteBuffer.allocateDirect(2000).asCharBuffer();
                ByteBuffer temp = ByteBuffer.allocateDirect(1000);
                try {
                    while (inChannel.read(inBuffer) != -1) {
                        inBuffer.flip();
                        final CoderResult result = decoder.decode(inBuffer, outBuffer, false);

                        outBuffer.flip();
                        while (outBuffer.hasRemaining()) {
                            allAnnounceS += String.valueOf(outBuffer.get());
                        }

                        StringTokenizer oneAnnounceST = new StringTokenizer(allAnnounceS, "\n");
                        while (oneAnnounceST.hasMoreTokens()) {
                            String oneAnnounceS = oneAnnounceST.nextToken();

                            if (oneAnnounceS.charAt(0) == '#' || oneAnnounceS.isEmpty()) // 略過註解
                            {
                                continue;
                            }
                            list.add(oneAnnounceS);
                        }
                        outBuffer.clear();
                        if (inBuffer.hasRemaining()) {
                            temp.put(inBuffer);
                            inBuffer.clear();
                            temp.flip();
                            inBuffer.put(temp);
                            temp.clear();
                        } else {
                            inBuffer.clear();
                        }
                    }
                    inBuffer.flip();
                    decoder.decode(inBuffer, outBuffer, true);
                    decoder.flush(outBuffer);
                    outBuffer.flip();
                    while (outBuffer.hasRemaining()) {
                        allAnnounceS += String.valueOf(outBuffer.get());
                    }

                    StringTokenizer oneAnnounceST = new StringTokenizer(allAnnounceS, "\n");
                    while (oneAnnounceST.hasMoreTokens()) {
                        String oneAnnounceS = oneAnnounceST.nextToken();
                        if (oneAnnounceS.charAt(0) == '#' || oneAnnounceS.isEmpty()) // 略過註解
                        {
                            continue;
                        }
                        list.add(oneAnnounceS);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                firstboot = false ;
            } // 如果第一次 讀檔
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            firstboot = false;
        }
    }

    /**
     * 確保announcementsCycle.txt存在
     *
     * @throws IOException 產生檔案錯誤
     */
    private void fileEnsure() throws IOException {
        if (!dir.exists()) {
            dir.createNewFile();
        }
    }

    private void cycle() {
        AnnouncementsCycleTask task = new AnnouncementsCycleTask();
        GeneralThreadPool.getInstance().scheduleAtFixedRate(task, 100000, 60000 * Config.Announcements_Cycle_Time); // 10分鐘公告一次
    }

    /**
     * 處理廣播字串任務
     */
    class AnnouncementsCycleTask implements Runnable {

        @Override
        public void run() {
            scanfile();
            // 啟用修改時間顯示 - (yyyy.MM.dd)
           // if (AnnounceTimeDisplay) {
                //SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
                //ShowAnnouncementsCycle("(" + formatter.format(new Date(lastmodify)) + ")");
           // }
            Iterator<String> iterator = list.listIterator();
            if (iterator.hasNext()) {
                round %= list.size();
                ShowAnnouncementsCycle(list.get(round));
                round++;
            }
        }
    }

    /**
     * 把字串廣播到伺服器上
     */
    private void ShowAnnouncementsCycle(String announcement) {
        Collection<PcInstance> AllPlayer = LsimulatorWorld.getInstance().getAllPlayers();
        for (PcInstance pc : AllPlayer) {
            pc.sendPackets(new S_SystemMessage(announcement));
        }
    }
}
