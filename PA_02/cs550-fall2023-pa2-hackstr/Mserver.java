import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Mserver extends Thread {

    public static LoggerInfo infoLogger = new LoggerInfo(System.getProperty("user.dir"));
    public static Logger getLogInfo = infoLogger.getLoggerInfo();

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(50);
    // new Mserver(peerID,PeerPort,dir)
    int peerID;
    int PeerPort;
    String dir;
    ServerSocket Socket;
    Socket cs;
    Mclient mc;
    String IP;
    CharBuffer buf;
    ConcurrentHashMap<String, String> seen = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> hitseen = new ConcurrentHashMap<>();

    public Mserver(int peerID, int PeerPort, String dir, String IP, Mclient mc) throws IOException {
        getLogInfo.info("---------------------> Mserver() in  Mserver.java class");

        getLogInfo.info("******************************server started&&&&&&&&&&");
        //System.out.println("******************************server started&&&&&&&&&&");
        this.peerID = peerID;
        this.PeerPort = PeerPort;
        this.dir = dir;
        this.mc = mc;
        this.IP = IP;
        Socket = new ServerSocket(PeerPort);
        FileChannel channel = new RandomAccessFile("filename-index" + peerID + ".dat", "rw").getChannel();
        MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024 * 100);
        ;
        buf = mappedBuffer.asCharBuffer();
        CreateIndexFile(dir, buf);
        Thread t7 = new Thread(new Runnable() {
            public void run() {
                while(true)
                {
                    if(seen.size()>900)
                    {
                        cleanMap(seen);
                    }
                    if(hitseen.size()>900)
                    {
                        cleanMap(hitseen);
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block

                        getLogInfo.info("Error in t7 thread");

                        //System.out.println("Error in t7 thread");
                    }
                }
            }
        });
        THREAD_POOL.submit(t7);
    }
    public void cleanMap(ConcurrentHashMap<String, String> map) {
        getLogInfo.info("---------------------> cleanMap() in  Mserver.java class");

        if (map.size() > 800) {
            // Convert the keys to a list and sort it
            List<String> sortedKeys = new ArrayList<>(map.keySet());
            Collections.sort(sortedKeys);
            
            // Remove the oldest entries
            int excess = map.size() - 800;
            for (int i = 0; i < excess; i++) {
                map.remove(sortedKeys.get(i));
            }
        }
    }

    public void CreateIndexFile(String path, CharBuffer buf) { 
        Boolean check = new File(path).exists();
        // log started
        if (check) {

            File folder = new File(path);
            File[] files = folder.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    CreateIndexFile(file.getAbsolutePath(), buf);
                } else {
                     //System.out.println(file.getAbsolutePath());
                    buf.put((char) file.getAbsolutePath().length());
                    buf.put(file.getAbsolutePath());
                    // System.out.println("Storing file name: "+ file.getName());
                }
            }
        }
        // log ended
    }

    public void run() {
        while (true) {
            try {
                cs = Socket.accept();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
            THREAD_POOL.submit(new SClientHandler(cs, seen, hitseen, peerID, PeerPort, IP, dir, buf, mc));

        }
    }

}

class SClientHandler implements Runnable {

    public static LoggerInfo infoLogger = new LoggerInfo(System.getProperty("user.dir"));
    public static Logger getLogInfo = infoLogger.getLoggerInfo();

    final Socket clientSocket;
    String dir;
    int PeerPort;
    int PeerID;
    String IP;
    Map<String, String> seen;
    ConcurrentHashMap<String, String> hitseen;
    Mclient mc;
    CharBuffer buf;

    public SClientHandler(Socket clientSocket, ConcurrentHashMap<String, String> seen,
            ConcurrentHashMap<String, String> hitseen, int PeerID, int PeerPort, String IP, String dir, CharBuffer buf,
            Mclient mc) {

        this.clientSocket = clientSocket;
        this.dir = dir;
        this.seen = seen;
        this.hitseen = hitseen;
        this.PeerID = PeerID;
        this.PeerPort = PeerPort;
        this.IP = IP;
        this.mc = mc;
        this.buf = buf;

    }

    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            String rec = (String) ois.readObject();
           // System.out.println("Line 114 Server: Received message: "+rec);
            String[] splitRec = rec.split(",");
            if ("Query".equals(splitRec[0])) {
                if (!seen.containsKey(splitRec[3])) // seeing request for 1st time
                {
                    boolean searchResult = search(splitRec[1]);
                    if (searchResult) { // file is found so it need to send hit query
                        mc.sendHitQuery("HitQuery," + splitRec[1] + "," + splitRec[3] + "," + IP + "," + PeerPort + ","
                                + splitRec[5]);
                    } else { // file is not found so it need to farword the request
                        mc.QFarword(rec, true);
                    }
                    seen.put(splitRec[3], rec);
                }

            } else if ("HitQuery".equals(splitRec[0])) {
                if (!hitseen.contains(splitRec[2])) {
                    int origin = Integer.parseInt(splitRec[splitRec.length - 1]);
                    if (origin == PeerID) {
                        getLogInfo.info("Calling Obtain fun");
                        //System.out.println("Calling Obtain fun");
                        mc.putHit(rec);
                    } else {
                        mc.sendHitQuery(rec);
                    }
                    hitseen.put(splitRec[2], rec);
                }
            } else if ("fileName".equals(splitRec[0])) {
                String path = getpath(splitRec[1]);
                getLogInfo.info("200:server filename Received from SC() in Mserver.java) : "+splitRec[1]);
                //System.out.println("200:server filename Received from client : "+splitRec[1]);
                sendfile(path);
            } else {
                getLogInfo.info("145 server: Unknown Query"+rec);
                //System.out.println("145 server: Unknown Query"+rec);
            }

        } catch (Exception e) {

        }

    }

    public void sendfile(String path) {
        try {
            getLogInfo.info("---------------------> sendfile() in SClientHandler class of   Mclient.java class");
            getLogInfo.info("Line156 Server sending file ........");
            //System.out.println("Line156 Server sending file ........");
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            // ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            // Retrieving file name to be downloaded
            // String fileName = (String) ois.readObject();
            while (true) {
                // Reading file info
                File newFile = new File(path);
                byte b[] = new byte[(int) newFile.length()];

                // Writing file info
                oos.writeObject((int) newFile.length());
                oos.flush();

                // Reading file content
                FileInputStream fis = new FileInputStream(newFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(b, 0, (int) newFile.length());

                // Writing file content
                oos.write(b, 0, b.length);
                oos.flush();
                bis.close();
            }
        } catch (Exception e) {

        }

    }

    public synchronized boolean search(String searchFile) {
        buf.rewind();
        while (buf.hasRemaining()) {
            int len = buf.get();
            char chars[] = new char[len];
            buf.get(chars);
            String fileName = new String(chars);
            //System.out.println("Here > " + fileName);
            String tfilesplit[] = fileName.split(Pattern.quote(File.separator));;
            if (tfilesplit[tfilesplit.length - 1].equals(searchFile)) {
                  System.out.println("Found " + searchFile);
                return true;

            }

        }
        return false;
    }

    public synchronized String getpath(String searchFile) {
        buf.rewind();
        while (buf.hasRemaining()) {
            int len = buf.get();
            char chars[] = new char[len];
            buf.get(chars);
            String fileName = new String(chars);
            //System.out.println("Here > " + fileName);
            String tfilesplit[] = fileName.split(Pattern.quote(File.separator));
            if (tfilesplit[tfilesplit.length - 1].equals(searchFile)) {
                // System.out.println("Found " + searchFile);
                return fileName;

            }

        }
        return null;
    }

}
