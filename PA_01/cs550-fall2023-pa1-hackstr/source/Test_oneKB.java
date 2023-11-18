import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class Test_oneKB
{

    public static void main(String args[]) throws IOException
    {
        BufferedReader input = null;
        input = new BufferedReader(new InputStreamReader(System.in));
        String hostAddress, fileName;
        System.out.println("\nEnter server address and name of the file you want to search:");
		System.out.println("enter host address");
                    hostAddress = input.readLine();
					System.out.println("enter file name");
                    fileName = input.readLine();
					(new LookupTest(hostAddress, fileName)).start();
    }

}

 class LookupTest extends Thread {
		private String serverAddress;
		private String fileName;
		
		public LookupTest(String host, String file) {
			this.serverAddress = host;
			this.fileName = file;
		}
		
		public void run() {
            int TEST_COUNT=10000;
			Socket socket = null;
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			
			long startTime, endTime, totalTime = 0;
			double avgTime;
			
			try {
				socket = new Socket(serverAddress, 7777);
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		        out = new ObjectOutputStream(socket.getOutputStream());
		        out.flush();
		        in = new ObjectInputStream(socket.getInputStream());
		        
				for (int i = 0; i < TEST_COUNT; i++) {
					startTime = System.currentTimeMillis();
					//System.out.println("filename"+fileName);
					out.writeObject("search "+fileName);
					in.readObject();
					endTime = System.currentTimeMillis();
					totalTime += (endTime - startTime);
					if(i%100==0)System.out.print(" "+i);
				}
				avgTime = (double) Math.round(totalTime / (double) TEST_COUNT) / 1000;

				System.out.println("Average search time for " + TEST_COUNT + " lookup requests is " + avgTime + " seconds.");
				input.readLine();
				this.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// Closing all streams. Close the stream only if it is initialized 
					if (out != null)
						out.close();
					
					if (in != null)
						in.close();
					
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}