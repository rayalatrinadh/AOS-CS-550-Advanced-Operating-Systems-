import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Logger;

public class MainPeer {
    public static LoggerInfo infoLogger = new LoggerInfo(System.getProperty("user.dir"));
    public static Logger getLogInfo = infoLogger.getLoggerInfo();
    String mIP = "Undefined";
    int mPort = -1;
    int mPeerID = -1;
    boolean mserStatus = false;
    String mPath = "dir doesn't exist";
    String mtopology = "undefined";
    boolean serverDefault = true;

    // boolean topologyDefault = true;
    private void status() {

        getLogInfo.info("---------------------> status() in  MainPeer.java class");
        getLogInfo.info("+++++++++++++++++++++++++++++++");
        getLogInfo.info("Peer Status");
        getLogInfo.info("Peer Topology: " + mtopology);
        getLogInfo.info("Peer ID: " + mPeerID);
        getLogInfo.info("Peer IP: " + mIP);
        getLogInfo.info("Peer Port: " + mPort);
        getLogInfo.info("Peer Path: " + mPath);
        getLogInfo.info("Server State: " + mserStatus);
        getLogInfo.info("+++++++++++++++++++++++++++++++");

        // System.out.println("\n+++++++++++++++++++++++++++++++");
        // System.out.println("\tPeer Status");
        // System.out.println("\tPeer Topology: " + mtopology);
        // System.out.println("\tPeer ID: " + mPeerID);
        // System.out.println("\tPeer IP: " + mIP);
        // System.out.println("\tPeer Port: " + mPort);
        // System.out.println("\tPeer Path: " + mPath);
        // System.out.println("\tServer State: " + mserStatus);
        // System.out.println("\n+++++++++++++++++++++++++++++++");

    }

    private static boolean isConfigExist() {

        getLogInfo.info("---------------------> isConfigExist() in  MainPeer.java class");
        if (!new File("config.txt").exists()) {

            getLogInfo.info("****Unable to locate config file*****");
            getLogInfo.info("You can generate <config.txt> file by running configFileGen");


            // System.out.println("\n*****Unable to locate config file*****");
            // System.out.println("You can generate <config.txt> file by running configFileGen\n\n");
            return false;
        } else {

            getLogInfo.info("file exists");
            //System.out.println("file exists");
            return true;
        }
    }

    public synchronized String[] getPeerPath(int peerID) {


        getLogInfo.info("---------------------> getPeerPath() in  MainPeer.java class");
        File folder = (new File(System.getProperty("user.dir")));
        //String path = String.valueOf(folder + "\\PeerFiles\\Peer" + peerID);
        //String path = new File(folder, "PeerFiles" + File.separator + "Peer" + peerID).toString();
        String path = new File(folder, "PeerFiles").toString();
        getLogInfo.info("path: "+ path);
        Boolean check = new File(path).exists();
        if (check) {
            String ret[] = new String[1];
            ret[0] = path;
            // mPath=path;
            return ret;

        }
        String ret[] = new String[2];
        ret[0] = "dir doesn't exist";
        ret[1] = path;
        return (ret);
    }

    private String setTopology(InputStream is, Properties p) throws IOException {

        getLogInfo.info("---------------------> setTopology() in  MainPeer.java class");

        p.load(is);
        boolean star = Boolean.valueOf(p.getProperty("peer.topology.star"));
        boolean mesh = Boolean.valueOf(p.getProperty("peer.topology.mesh"));
        boolean index = Boolean.valueOf(p.getProperty("peer.topology.index"));

        if (star && mesh && index)
            return "All the topologies are true";
        else if (!star && !mesh && !index)
            return "At least one should be true";
        else if (star)
            return "star";
        else if (mesh)
            return "mesh";
        else if (index)
            return "index";
        else
            return mtopology;

    }

    private boolean serverDefault() {

        getLogInfo.info("---------------------> serverDefault() in  MainPeer.java class");

        if (mPeerID != -1 && mPort != -1 && !mPath.equals("dir doesn't exist")) {
            serverDefault = false;
            return false;
        }
        return serverDefault;
    }

    public static void main(String args[]) throws IOException {

        getLogInfo.info("---------------------> main() in  MainPeer java class");

        // variables
        MainPeer mp = new MainPeer();
        int peerID = -1; // Either by command line or from directly from the config file by comparing
                         // with host IP.
        int peerPort = -1; // should get from config file
        String currentIP = InetAddress.getLocalHost().getHostAddress(); // Always server starts on the current host
        String peerDir = mp.getPeerPath(1)[0]; // depends on peerID
        String config = "config.txt";
        boolean serStatus = false;
        boolean checkConfig = isConfigExist(); // no config no start
        InputStream is;

        is = new FileInputStream(config);
        Properties p = new Properties();
        p.load(is);
        String topology = mp.setTopology(is, p);
        mp.mtopology = topology;
        if (checkConfig && args.length != 0) // command line arguments detected
        {
            peerID = Integer.valueOf(args[0]); // get peerID from cline
            peerPort = Integer.valueOf(p.getProperty("peer." + peerID + ".port"));
            // dir
            String path[] = mp.getPeerPath(peerID);
            if (path.length != 1) {
                getLogInfo.info("path : "+path[0]);
                //System.out.println(path[0]);
                peerDir = path[0];
                getLogInfo.info("check path " + path[1]);
                //System.out.println("check path " + path[1]);

            } else {
                peerDir = path[0];
            }
            mp.mPeerID = peerID;
            mp.mIP = currentIP;
            mp.mPort = peerPort;
            mp.mPath = peerDir;
            mp.mserStatus = serStatus;
            mp.status();

        }

        if (checkConfig && peerID == -1) // get the details from config file
        {

            int tempPeerID = 1;
            getLogInfo.info("looping config file");
            //System.out.println("looping config file");
            while (tempPeerID <= 16) // loop to find the peerID from config file
            {
                //System.out.println("Comparing: "+p.getProperty("peer." + tempPeerID + ".IP")+"with currentIp: "+currentIP);
                if (p.getProperty("peer." + tempPeerID + ".IP").equals(currentIP)) {
                    peerID = tempPeerID;

                    // we got peerID and IP from config file dir path to be checked
                    break;
                }
                tempPeerID++;
            }
            if (peerID == -1){
             
                 getLogInfo.info("PeerID unidentified");
                //System.out.print("PeerID unidentified");
            }
            else {
                String path[] = mp.getPeerPath(peerID);
                if (path.length != 1) {
                    System.out.println(path[0]);
                    peerDir = path[0];
                    getLogInfo.info("check path " + path[1]);
                    //System.out.println("check path " + path[1]);

                } else {
                    peerDir = path[0];
                }

                getLogInfo.info("Peer with ID " + peerID + " can be Started  on");
                //System.out.println("Peer with ID " + peerID + " can be Started  on");
                peerPort = Integer.valueOf(p.getProperty("peer." + peerID + ".port")); // always get the peer port from
                                                                                       // the config file
                mp.mPeerID = peerID;
                mp.mIP = currentIP;
                mp.mPort = peerPort;
                mp.mPath = peerDir;
                mp.mserStatus = serStatus;
            }
            mp.status();

        }

        if (!mp.serverDefault()) {
            Mclient mc = new Mclient(peerID, mp.mtopology);
            Mserver ms = new Mserver(peerID, peerPort, peerDir, mp.mIP, mc);

            ms.start();
            // start the server thread
            // new Mserver(peerID,PeerPort,dir).start();
            // start client thread
            mc.start();

        }
        getLogInfo.info("Server Default: " + mp.serverDefault);
        //System.out.println("Server Default: " + mp.serverDefault);

    }
}
