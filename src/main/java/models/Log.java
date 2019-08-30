package models;

import org.apache.log4j.LogManager;

import java.util.logging.Logger;

public class Log {
    static private org.apache.log4j.Logger logger = LogManager.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static{
        //PropertyConfigurator.configure("src/main/resources/log4j.properties");
    }
    public static void debug(String msg){
        logger.info(msg);
    }
    public static void error(String msg){
        logger.error( msg);
    }
    public static void warning(String msg){
        logger.warn( msg);
    }
}
