import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.Serializable;

class Server_1 
{
    final int ServerPort = 7777;
    // An Array to hold the metadata
    static ArrayList<FileInfo> GlobalIndex =new ArrayList<FileInfo>();
    // Create a thread pool to hold the client Threads
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);
    // Track number of clients connected
    int ClientCount =0;
    public int getClientCount()
    {
        return ClientCount;
    }
    public void reduceClientCount()
    {
         --ClientCount;
    }

    public void ServerStatus()
    {
        System.out.println("no of clients connected = "+ClientCount+" \n Number of files Registered = "+GlobalIndex.size());
    }

    public ArrayList<FileInfo> getGlobalIndex()
    {
        return GlobalIndex;
    }

    public int updateGlobalIndex(ArrayList<FileInfo> temp)
    {
        for(int i=0; i<temp.size(); i++)
			GlobalIndex.add(temp.get(i));
        return GlobalIndex.size();
    }

    public void acceptClients() throws IOException
    {
        try (// Host the server on a port
        ServerSocket ss = new ServerSocket(ServerPort)) {
            while (true ) {
                // Accept the connection
                Socket clientSocket = ss.accept();
                ClientCount++;
                ServerStatus();
                System.out.println();
                // send the connected client to threadpool
                THREAD_POOL.submit(new ClientHandler(clientSocket,ClientCount));
                
            }
        }
    }

    public static void main (String args[]) throws IOException
    {
        // start receiving clients via accept clients methods
        Server_1 s1 = new Server_1();
        System.out.println("Server started on port : "+s1.ServerPort);
        s1.acceptClients();
        System.out.println("Server Closed");
    }


}

// New class for clients handeling

 class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final int clientID;

    public ClientHandler(Socket clientSocket,int clientID) {

        this.clientSocket = clientSocket;
        this.clientID=clientID;
    }
    public void run() {

        try {
        ObjectOutputStream oos =new ObjectOutputStream(clientSocket.getOutputStream());
	    
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("Client connected");
            while(true)
            {
                String rec =(String)ois.readObject();
                if(rec.equalsIgnoreCase("CLIENTID"))
                {                
                oos.writeObject(String.valueOf(clientID));
                }
                else if(rec.equalsIgnoreCase("END"))
                {
                    clientSocket.close();
                    oos.close();
                    ois.close();
                }
                else if((rec.contains("search")))
                {
                    List<FileInfo> result ;
                       result= search(rec);
                       oos.writeObject(result);
                }
                else if((rec.contains("register")))
                {
                    
                    oos.writeObject("OK");
                    @SuppressWarnings(value = { "unchecked" })
                    ArrayList<FileInfo> recive = (ArrayList<FileInfo>) ois.readObject();
                    Server_1 temp = new Server_1();
                    int size=temp.updateGlobalIndex(recive);
                    oos.writeObject("DONE");
                    System.out.println("Recieved total files > "+recive.size());
                    System.out.println("Total Index Files > "+size);
                }
                else
                {
                    oos.writeObject("Unknown Command \n > Register \n > Search");
                }
            
             }
            
            //clientSocket.close();

        
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Client Disconnected");
            
            try {
                clientSocket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    ArrayList<FileInfo> search(String text)
    {
        ArrayList<FileInfo> result =new ArrayList<>();
        String decode = text.substring(7);
        
        Server_1 temp = new Server_1();
        ArrayList<FileInfo> GlobalIndex = temp.getGlobalIndex();

        for(int i=0; i<GlobalIndex.size();i++)
        {
            FileInfo file =GlobalIndex.get(i);
            if(file.fileName.contains(decode))
            {
                result.add(file);
            }
        }
        return result;
    }



}

class FileInfo implements Serializable {
	public int peerID;
	public String peerHost;
	public int portNumber;
	public String fileName;
	public String filepath;
}
