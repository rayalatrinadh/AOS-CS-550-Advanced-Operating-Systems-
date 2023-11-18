import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Mclient extends Thread {

    public static LoggerInfo infoLogger = new LoggerInfo(System.getProperty("user.dir"));
    public static Logger getLogInfo = infoLogger.getLoggerInfo();


    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(20);
    ArrayList<Neighbors> neighborslist = new ArrayList<Neighbors>();
    ConcurrentHashMap<String, String> seen = new ConcurrentHashMap<>();
    ConcurrentLinkedQueue<String> process = new ConcurrentLinkedQueue<String>();
    ConcurrentHashMap<String, String> hitseen = new ConcurrentHashMap<>();
    ConcurrentLinkedQueue<String> hitdownload = new ConcurrentLinkedQueue<String>();
    int peerID = -1;
    String topology;
    final int MAX_SIZE = 700;

    public Mclient(int peerID, String topology) throws IOException {
        getLogInfo.info("---------------------> Mclient() in  Mclientclass");
        this.peerID = peerID;
        this.topology = topology;
        loadNeighborsdata();
         displayNeighbors();
        Thread t3 = new Thread(new Runnable() {
            public void run() {
                ClientInterface();
            }
        });
        Thread t4 = new Thread(new Runnable() {
            public void run() {
                try {
                    ContinueProcess();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        Thread t6 = new Thread(new Runnable() {
            public void run() {
                ProcessHit();
            }
        });

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

        THREAD_POOL.execute(t3);
        THREAD_POOL.execute(t4);
        THREAD_POOL.execute(t6);
        THREAD_POOL.execute(t7);
         
    }

    void AddToProcessQueue(String msg) {
        // Query("Query"0,msg1,TTL2,QID3,From4,Origin5)

        getLogInfo.info("Adding to Process queue : " + msg);
        //System.out.println("Adding to Process queue : " + msg);
        process.add("Query" + "," + msg + "," +
                3 + "," +
                System.currentTimeMillis() + "," +
                peerID + "," + peerID);
        // System.out.println(process.remove());
        // QFarword(process.remove());
    }

    void QFarword(String Query) {
        if (!seen.containsKey(Query.split(",")[3])) {
            for (Neighbors n : neighborslist) {
                THREAD_POOL.submit(new QFarword(Query, n));
            }
        }
        //seen.put(Query.split(",")[3],Query);
    }
    void QFarword(String Query, Boolean ser)
    {
        for(int i=0;i<3;i++)
        {
            for (Neighbors n : neighborslist) {
                THREAD_POOL.submit(new QFarword(Query, n));
            }
        }seen.put(Query.split(",")[3],Query);
    }

    void sendHitQuery(String HitQuery) {
        if (!hitseen.containsKey(HitQuery.split(",")[2])) // get Qid
        {
            hitseen.put(HitQuery.split(",")[2], HitQuery);
            QFarword(HitQuery);
        }
    }

    public void cleanMap(ConcurrentHashMap<String, String> map) {
        

        if (map.size() > MAX_SIZE) {
            // Convert the keys to a list and sort it
            List<String> sortedKeys = new ArrayList<>(map.keySet());
            Collections.sort(sortedKeys);
            
            // Remove the oldest entries
            int excess = map.size() - MAX_SIZE;
            for (int i = 0; i < excess; i++) {
                map.remove(sortedKeys.get(i));
            }
        }
    }

    // String receive1(String fileName, String IP, int Port) throws IOException {
    //     System.out.println("Line 103 Started Receiving file: "+fileName+"IP:"+IP+"Port:"+Port);
    //     try {
    //         System.out.println("Line 105 client: connection suceeded");
    //         try (Socket peerAsClientSocket = new Socket(IP, Port)) {
    //             System.out.println("Line 107 client: connection suceeded");
    //             System.out.print("line 108, this is peerASClient socket: "+peerAsClientSocket);
    //             ObjectOutputStream oos = new ObjectOutputStream(peerAsClientSocket.getOutputStream());
    //             System.out.println("line 110, this is peerASClient socket: "+oos);
    //             oos.flush(); 
                
    //             ObjectInputStream ois = new ObjectInputStream(peerAsClientSocket.getInputStream());
    //             // Writing file name to server peer
    //             System.out.println("Line 109 client: connection suceeded");
    //             oos.writeObject("fileName," + fileName);
    //             //oos.writeObject("fileName," + fileName);
    //             System.out.println("Line 103 Started Receiving file"+fileName);
    //             // Reading bytes of the file
    //             int readBytes = (int) ois.readObject();
    //             byte b[] = new byte[readBytes];
    //             ois.readFully(b);

    //             // Writing file on the client peer directory
    //             File folder = (new File(System.getProperty("user.dir")));
    //             String path = String.valueOf(folder + "\\PeerFiles\\downloads");
    //             // Boolean check= new File(path).exists();
    //             FileOutputStream os = new FileOutputStream(path + "//" + fileName);
    //             try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
    //                 bos.write(b, 0, (int) readBytes);

    //                 System.out.println("Requested file: " + fileName + ", has been downloaded to directory: " + path);
    //                 System.out.println("Display file " + fileName);

    //                 bos.flush();
    //             }
    //         }
    //         //return "success";
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return "success";

    // }
    String receive(String fileName, String IP, int Port) throws IOException {
        String result = "failure";  // Initialize with a failure state
        try (Socket peerAsClientSocket = new Socket(IP, Port)) {
            
            // Initialize the ObjectOutputStream first
            ObjectOutputStream oos = new ObjectOutputStream(peerAsClientSocket.getOutputStream());
                       
            // Then, initialize the ObjectInputStream
           
            getLogInfo.info("Line 155");
            //System.out.println("Line 155");
            // Writing file name to server peer
            oos.writeObject("fileName," + fileName);
            oos.flush();
             ObjectInputStream ois = new ObjectInputStream(peerAsClientSocket.getInputStream());
            // Reading bytes of the file
            int readBytes = (int) ois.readObject();
            byte[] b = new byte[readBytes];
            ois.readFully(b);
            
            // Defining the path for the download directory
            //String path = System.getProperty("user.dir") + "\\PeerFiles\\downloads";
        //String path = System.getProperty("user.dir") + "\\PeerFiles\\downloads";
        String path = new File(System.getProperty("user.dir"), "PeerFiles" + File.separator + "downloads").toString();
          
            // Writing file on the client peer directory
            File file = new File(path, fileName);
            try (FileOutputStream os = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(b, 0, readBytes);
                bos.flush();
            }
            getLogInfo.info("File downloaded");
            getLogInfo.info("Requested file: " + fileName + ", has been downloaded to directory: " + path);
            //System.out.println("Requested file: " + fileName + ", has been downloaded to directory: " + path);
            result = "success";  // If all goes well, update result to success
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    void putHit(String HitQuery)
    {
        hitdownload.offer(HitQuery);
    }

    void obtain(String HitQuery) {
        getLogInfo.info("---------------------> obtain() in  Mclient class");
        getLogInfo.info("132: Obtain fun activated Hit Query: "+HitQuery);
        //System.out.println("132: Obtain fun activated Hit Query: "+HitQuery);
        String Hitsplit[] = HitQuery.split(",");
        String fileName = Hitsplit[1];
        String IP = Hitsplit[3];
        int port = Integer.valueOf(Hitsplit[4]);
        Thread t5 = new Thread(new Runnable() {
            public void run() {
                try {
                    getLogInfo.info("140 Calling receive");
                    //System.out.println("140 Calling receive");
                    receive(fileName, IP, port);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        THREAD_POOL.execute(t5);

    }

    void loadNeighborsdata() throws IOException {
        InputStream is;

        is = new FileInputStream("config.txt");
        Properties p = new Properties();
        p.load(is);
        // System.out.println(("peer." + peerID +"." +topology+".next"));
        String neighborsdata[] = p.getProperty("peer." + peerID + "." + topology + ".next").split(",");
        for (int i = 0; i < neighborsdata.length; i++) {
            int tport = Integer.valueOf(p.getProperty("peer." + neighborsdata[i] + ".port"));
            String tIP = p.getProperty("peer." + neighborsdata[i] + ".IP");

            neighborslist.add(new Neighbors(tport, tIP));
        }
    }

    void displayNeighbors() {
        for (Neighbors n : neighborslist) {

            getLogInfo.info(n.getIpAddress());
            getLogInfo.info(n.getPort()+"");
            //System.out.println(n.getIpAddress());
            //System.out.println(n.getPort());
        }
    }
    public void ProcessHit() 
    {
        while(true)
        {
            String hit = hitdownload.poll();
            if (hit != null)
            {
                obtain( hit);
            }
        }
    }
    public void ContinueProcess() throws InterruptedException {
        getLogInfo.info("Continue process******");
        //System.out.println("Continue process******");
        while (true) {
            String Q = process.poll();
            // System.out.println(Q);
            if (Q != null) { // Queue is not empty farword the query and reduce the ttl
                // System.out.println(Q);
                QFarword(Q);
                // again enqueue
                String Qsplit[] = Q.split(",");
                int ttl = Integer.valueOf(Qsplit[2]);
                ttl--;
                if (ttl > 0) {
                    Qsplit[2] = String.valueOf(ttl);
                    process.offer(String.join(",", Qsplit));

                    getLogInfo.info(String.join(",", Qsplit));

                    //System.out.println(String.join(",", Qsplit));
                } else {

                    getLogInfo.info(Qsplit[3]+" , "+ Q);

                    //seen.put(Qsplit[3], Q); // seen list using qid
                }
            }
            Thread.sleep(5000);

        }
    }

    public synchronized void ClientInterface() {
        getLogInfo.info("---------------------> ClientInterface() in  Mclient class");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {

            getLogInfo.info("client > ");
            System.out.print("client > ");
            String smsg;
            try {

                smsg = br.readLine();
                if (smsg.equals("exit")) {
                    THREAD_POOL.shutdownNow();
                    break;
                }
                if (smsg.contains("search")) {
                    if (smsg.length() <= 7 || !smsg.contains(" ")) {
                        getLogInfo.info("Invalid Search \n Ex: search <text>");

                        getLogInfo.info("Invalid Search \n Ex: search <text>");

                        System.out.println("Invalid Search \n Ex: search <text>");
                        // continue;
                    } else {
                        AddToProcessQueue(smsg.substring(7));
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    // public static void main(String args[]) throws IOException {

    //     String topology = "star";
    //     Mclient mc = new Mclient(1, topology);

    // }

}

class QFarword implements Runnable// just connect with neighbour server send msg and wait for 3 sec and repeat
// until ttl is zero close
{

    public static LoggerInfo infoLogger = new LoggerInfo(System.getProperty("user.dir"));
    public static Logger getLogInfo = infoLogger.getLoggerInfo();

    // private static final ExecutorService THREAD_POOL =
    // Executors.newFixedThreadPool(5);
    String Query;
    Neighbors n;

    public QFarword(String Query, Neighbors n) {

        this.Query = Query;
        this.n = n;
    }

    public void run() {
        try {
        
        getLogInfo.info("---------------------> run() in  QFarword class");
        Socket socket = new Socket(n.getIpAddress(), n.getPort());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(Query);
      // System.out.println("258 In Q farword query: " + Query + " IP: " + n.getIpAddress() + " port: " + n.getPort());
        } catch (IOException e) {
        //TODO Auto-generated catch block
        getLogInfo.info("Error in QFarword line 261, Server Unavailable");
        System.out.println("Error in QFarword line 261, Server Unavailable");
        }
    }

}

class Neighbors {
    private int port;
    private String IPaddress;

    public Neighbors(int port, String IPaddress) {
        this.IPaddress = IPaddress;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return IPaddress;
    }

}
