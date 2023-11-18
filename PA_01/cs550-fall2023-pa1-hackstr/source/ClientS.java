import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public  class ClientS{

    int ServerPort =7778;
    static String ClientasServerIP;
    
    public boolean portChecker(int port){
        try{
        ServerSocket ss =new ServerSocket(port);
        ss.setReuseAddress(true);
        ss.close();
        return true;
        }catch (IOException e){
            return false;
        }
       
        
    }

    

    ArrayList<FileInfo> peerFiles = new ArrayList<FileInfo>();
    public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException
    {
        // Start the server on port 7778
        // >> 
        // Get the IP address of the client server automatically
        // >>store in ClientasServerIP
                 ClientasServerIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Your IP: "+ClientasServerIP);
            BufferedReader kb =new BufferedReader(new InputStreamReader(System.in));
            String IndexIP = "localhost";
            System.out.println("Enter Index server IP to connect : ");
            IndexIP=kb.readLine();

            if(IndexIP.isEmpty())
            {
                System.out.println("Connecting with local host");
                IndexIP = "localhost";
            }
            else{
                System.out.println("Connecting with "+IndexIP);
            }

        try (Socket ctoServ = new Socket(IndexIP,7777)) {
            System.out.println("Connected to IndexServer");

            ObjectOutputStream oos =new ObjectOutputStream(ctoServ.getOutputStream());
            
            ObjectInputStream ois = new ObjectInputStream(ctoServ.getInputStream());
             int clientID=-1;
            // Ask for Client ID
            
            String send ="clientID";
            oos.writeObject(send);
            // Receive the id from Server and store it in a variable called clientID
            clientID=Integer.valueOf( (String) ois.readObject() );
            //String rec =(String)ois.readObject();
            
            //Since Connected to server we have the port where this client is hosted on port and IP address 
            //  and client ID

            // now build the path were the peer files were located and save them.
            ClientS h= new ClientS();
            File folder =(new File(System.getProperty("user.dir")));
            String path = String.valueOf(folder+"\\PeerFiles\\Peer"+clientID);
            Boolean check= new File(path).exists();
            for( int i=0;i<10 && check;i++)
            {
                if(h.portChecker(h.ServerPort))
                {
                    new StartAsServer(h.ServerPort ,path).start();
                    System.out.println("Client as server started on : "+h.ServerPort);
                    break;
                }
                else
                h.ServerPort++;
            }
            
            ClientS c =new ClientS();
            int FRstatus = c.fileMetadata( clientID);
            if(FRstatus==-2) System.out.println("No peerfiles in the path");
            if(FRstatus==1) System.out.println("peer files store successfull");

            
            // Build to and fro interface for the client-server
            while(true){
                System.out.print("~> "); // print > 
                String smsg  =kb.readLine(); // read cmd from user
                
                if(!smsg.contains("search") && !smsg.contains("register") && !smsg.contains("end")){
                    oos.writeObject(smsg); // send message to server
                    String recmsg = (String)ois.readObject();
                    System.out.println(recmsg);
            
                }
                else {
                    //ctoServ.close();
                    if(smsg.contains("search"))
                    {
                        if(smsg.length()<=7 || !smsg.contains(" ")) 
                        {
                            System.out.println("Invalid Search \n Ex: search <text>");
                            continue;
                        }
                        oos.writeObject(smsg);
                        @SuppressWarnings(value = { "unchecked" })
                        List<FileInfo> result = (List<FileInfo>) ois.readObject();
                            if(result.size() == 0) System.out.println("No results found");
                            else{
                            for(int i=0;i<result.size();i++)
                            {
                            System.out.println(i+") "+result.get(i).fileName+" "+result.get(i).peerID+ " "+result.get(i).peerHost+ " "+result.get(i).portNumber+ " ");
                            }

                            // Download the specific files
                            System.out.println("You want to download any file > Enter Y and then index");
                            String n=kb.readLine();
                            if(n.equalsIgnoreCase("y"))
                            {
                                int i=Integer.valueOf(kb.readLine());
                                c.receive(result.get(i).fileName, result.get(i).peerHost, result.get(i).portNumber,clientID );
                            }
                        }
                    }
                    else if(smsg.contains("register"))
                    {
                        oos.writeObject(smsg); // send message that it is going to receive a object
                        String confirm = (String)ois.readObject(); // server will send ok to client
                        if(confirm.equals("OK"))
                        {
                            oos.writeObject(c.peerFiles);// send the object
                            confirm = (String)ois.readObject(); //Wait for confirmation
                            if(confirm.equals("DONE")) System.out.println("Registered successfully");
                            //c.retrieve();
                        }
                    }
                    else if(smsg.contains("end"))
                    {
                        System.out.println("Connection Terminated with server");
                       ctoServ.close();
                        break;
                        
                    }
                   //
                }
            }
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        //System.out.print("\nClientID: "+clientID+"\n");
       // ctoServ.close();
    }

    public int fileMetadata(int PeerID) throws IOException
    {
        if (PeerID == -1) 
        {
            BufferedReader kb = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter Path");
            String Path1=kb.readLine();
            Boolean check= new File(Path1).exists();
            if(check==false) return -2;
            storeMetadata( Path1, PeerID);
        } // returns -2 if the path doesn't exist
        else{
            File folder =(new File(System.getProperty("user.dir")));
        String path = String.valueOf(folder+"\\PeerFiles\\Peer"+PeerID);
        Boolean check= new File(path).exists();

        if(check==false) return -2;
        storeMetadata( path, PeerID);
        }
        
        return 1;
        
    }

    public void storeMetadata(String path, int PeerID)
    {
        File[] fileList =new File(path).listFiles();
                FileInfo file;
                    File current;
        
                    for(int i=0;i< fileList.length;i++)
                    {
                        file =new FileInfo();
                        current = fileList[i];
                        file.peerID = PeerID;
                        file.peerHost=ClientasServerIP;
                        file.portNumber = ServerPort;
                        file.fileName = current.getName();
                        file.filepath = current.getPath(); 
                        //System.out.println(file.peerID + file.fileName); 
                        peerFiles.add(file);
                    }
    }

    //function to download the specific file

    String receive1(String fileName, String IP, int Port) throws IOException
    {
        try (Socket socket = new Socket(IP,Port)) {
            // DataOutputStream dataOutputStream = null;
              ObjectOutputStream oos =new ObjectOutputStream(socket.getOutputStream());
              DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
              //System.out.println("Line 204 + "+fileName);
            oos.writeObject(fileName);
            int bytes=0;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            long size = dataInputStream.readLong(); // read file size 
            //System.out.println("line 209... "+size);
            byte[] buffer = new byte[4 * 1024];
            while (size > 0
                    && (bytes = dataInputStream.read(
                    buffer, 0,
                    (int)Math.min(buffer.length, size)))
                    != -1) {
                // Here we write the file using write method
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes; // read upto file size
            }
            System.out.println("Recieved File: "+fileName);
            fileOutputStream.close();
        }
         return "Success";
    }
    String receive(String fileName, String IP, int Port, int clientID)throws IOException
    {
        try{
        try (Socket peerAsClientSocket = new Socket(IP,Port)) {
            ObjectOutputStream oos = new ObjectOutputStream(peerAsClientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(peerAsClientSocket.getInputStream());
            // Writing file name to server peer
            	oos.writeObject(fileName);

            	// Reading bytes of the file
            	int readBytes = (int)ois.readObject();
            	byte b[] = new byte[readBytes];
            	ois.readFully(b);
            	
            	// Writing file on the client peer directory
                File folder =(new File(System.getProperty("user.dir")));
            String path = String.valueOf(folder+"\\PeerFiles\\downloads");
            //Boolean check= new File(path).exists();
            	FileOutputStream  os = new FileOutputStream(path + "//" + fileName);
            	try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                    bos.write(b, 0, (int)readBytes);
                    
                    System.out.println("Requested file: "+ fileName + ", has been downloaded to directory: "+ path);
                    System.out.println("Display file " + fileName);
                    
                    bos.flush();
                }
        }
        return"success";
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
        return"success";

    }


}

class StartAsServer extends Thread
{
    int Port;
    String Path;
    String FileName;
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);
    ServerSocket Socket;
    Socket cs;
    StartAsServer(int Port ,String Path) throws IOException
    {
        this.Port=Port;
        this.Path=Path;
        //this.FileName=FileName;
        Socket= new ServerSocket(Port);
        

    }

    public void run()
    {
        while(true)
        {
             try {
                cs= Socket.accept();
            } catch (IOException e) {
                // TODO Auto-generated catch block
               // e.printStackTrace();
            }
            THREAD_POOL.submit(new SClientHandler(cs,Path));
        }
    }

    

}

 class SClientHandler implements Runnable{

    final Socket clientSocket;
    String Path;
    public SClientHandler(Socket clientSocket, String Path) {

        this.clientSocket = clientSocket;
        this.Path=Path;
        
    }

  
    public void run1()
    {
        try{
            
       //ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
       
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        
        
          //  System.out.println("Line 283"+"....");
                String Filename=(String)ois.readObject();  
        int bytes = 0;
		// Open the File where he located in your pc
        File file = new File(Path+"\\"+Filename);//System.out.println("Line 288"+"...."+Path+"\\"+Filename);
		FileInputStream fileInputStream = new FileInputStream(file);
            // Here we send the File to Server
            DataOutputStream dataOutputStream = new DataOutputStream(
                    clientSocket.getOutputStream());;
            try {
                dataOutputStream.writeLong(file.length());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Here we break file into chunks
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer))
            	!= -1) {
            // Send the file to Server Socket
            dataOutputStream.write(buffer, 0, bytes);
            	dataOutputStream.flush();
            }
            // close the file here
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    // send the file content
    public void run()
    {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            // Retrieving file name to be downloaded
            String fileName = (String)ois.readObject();
            while(true) {
                // Reading file info
                File newFile = new File(Path + "//"+ fileName);
                byte b[] = new byte[(int)newFile.length()];
                
                // Writing file info
                oos.writeObject((int)newFile.length());
                oos.flush();
                
                // Reading file content
                FileInputStream fis = new FileInputStream(newFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(b, 0, (int)newFile.length());

                // Writing file content
                oos.write(b, 0, b.length);
                oos.flush();
                bis.close();                
            }
            }
        catch(Exception e) {
            
        }


    }


}



class FileInfo implements Serializable {
	public int peerID;
	public String peerHost;
	public int portNumber;
	public String fileName;
	public String filepath;
}
