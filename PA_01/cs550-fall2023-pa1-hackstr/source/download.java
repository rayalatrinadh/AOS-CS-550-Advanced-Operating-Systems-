import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class download extends Thread{
    
    private String peerAddress;
		private String fileName;
        private int port;
		private static int counter = 1;
        int TEST_COUNT=10000;
		
		public static void main(String args[]) {
		    new download().start();;
		}
        public void run() {
			long startTime, endTime, totalTime = 0, totalFileSize = 0;
			double time, avgSpeed;
			System.out.println("Test Started...");
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			
			try {
				for (int i = 0; i < TEST_COUNT; i++) {
					startTime = System.currentTimeMillis();
					
                    receive("onekbfiles_3.txt","localhost" , 7778, 1);
					endTime = System.currentTimeMillis();
					totalTime += (endTime - startTime);
					//File file = new File("C:/Users/ronil/Desktop/sem1/cs550/submissions/PA1/code/peer_files/downloads/" + fileName);
					//totalFileSize += file.length();
					//file.delete();
                    System.out.print(i);
				}
				time = (double) Math.round(totalTime / 1000.0);
				avgSpeed  = (totalFileSize / (1024.0 * 1024.0)) / totalTime * 1000.0;;
				
				System.out.println("Average speed for downloading " + TEST_COUNT + " files is " + avgSpeed + " MBps. \nPress ENETER.");
				input.readLine();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
        static String  receive(String fileName, String IP, int Port, int clientID)throws IOException
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

