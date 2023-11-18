import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerInfo {

    private static Logger logger = null;
    String filepath ;
    String filename="MyLogFile.log";
    public LoggerInfo(String filepath)
    {
        this.filepath=filepath;
    }
    public Logger getLoggerInfo() {
        if (logger == null) {
            synchronized (LoggerInfo.class) {
                if (logger == null) {  // Double-check
                    logger = Logger.getLogger("MyLog");
                    FileHandler fh;
                    try {
                        // Use "true" as the second parameter to append logs to the existing file
                        fh = new FileHandler(filepath+File.separator+filename, true);
                        logger.addHandler(fh);
                        SimpleFormatter formatter = new SimpleFormatter();
                        fh.setFormatter(formatter);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return logger;
    }

}