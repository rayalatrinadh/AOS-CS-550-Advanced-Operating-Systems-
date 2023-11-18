import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;

public class generatetextfiles {

    void generate(String Path) throws IOException
    {
        System.out.println(" *****Generate your random files here*****");
        int numfiles=10;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter filename here");
        String fname= br.readLine();

        System.out.println("Enter of files to be generated");
        numfiles=Integer.valueOf( br.readLine());

        System.out.println("Enter file size \n Enter 2 for 1KB files \n Enter 1024 of 1MB files");
        int filesize=Integer.valueOf( br.readLine());
        
        for(int j=1;j<=numfiles;j++)
        {
            //FileOutputStream file = new FileOutputStream(Path+"\\"+fname+"_"+j+".txt");
            System.out.println("************"+Path+"\\"+fname+"_"+j+".txt");
            File file = new File(Path+"\\"+fname+"_"+j+".txt");
        
            PrintWriter writer = new PrintWriter(file);
    
            Random rand = new Random();
            String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
            for(int i=0; i<1024*filesize; i++) {
                int index = rand.nextInt(characters.length());
                char randomChar = characters.charAt(index);
                writer.print(randomChar);
                }
            writer.close();
    }
    System.out.println("Generated 1kB random text file: " );
    }
  public static void main(String[] args) throws Exception {
    
    System.out.println("Welcome to test file generator");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int option =0;
    String userpath = System.getProperty("user.dir")+"\\PeerFiles";
    String path=userpath+"\\Peer1";
    generatetextfiles gt=new generatetextfiles();

    while (option != 3){
        switch (option)
    {
        case 0:
            System.out.println("current Path : "+path);
            System.out.println(" 1. Generate min 10k 1kb files");
            System.out.println(" 2. change path");
            System.out.println(" 3. exit");

            break;
        case 2:
            System.out.println("change path");
            path= userpath+"\\"+br.readLine();
            break;
        case 1:
            gt.generate(path);
            break;
        default:
            System.out.println("Invalid in put try again");


    }
    System.out.print("Enter option here > ");
    option=Integer.valueOf(br.readLine());
    }
    
  }

}

    

