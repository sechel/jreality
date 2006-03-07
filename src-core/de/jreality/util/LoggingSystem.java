/*
 * Created on Apr 3, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 * 
 */
package de.jreality.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 * LoggingSystem for jReality. This sets up a base logger
 * called de.jreality that has default Level=Level.SEVERE
 * and does not pass records to the parent logger.
 * 
 * For debugging special packages simply set the Level for
 * the "package"-Logger to what you want.
 *
 * @author weissman
 */
public class LoggingSystem {
 
    private static final Level GLOBAL_LEVEL = Level.SEVERE;
    
    //Singleton object for the logging sytem.
    private static LoggingSystem logSystem = new LoggingSystem();

    //the logger.
    private final Logger logger;
    
    private final Formatter formatter = new SimpleFormatter() {

        public synchronized String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
//            if (record.getSourceClassName() != null) {
//                sb.append(record.getSourceClassName());
//            } else {
//                sb.append(record.getLoggerName());
//            }
            String message = formatMessage(record);
            sb.append(record.getLevel().getLocalizedName());
            if (record.getSourceMethodName() != null) {
                sb.append(" [");
                sb.append(record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.')+1)).append(".");
                sb.append(record.getSourceMethodName());
            }
            sb.append("]: ");
            sb.append(message);
            sb.append("\n");
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }
            }
            return sb.toString();
        }
    };

    private LoggingSystem() {
        logger = Logger.getLogger("de.jreality");
        try {
            setDebugUse();
        } catch (SecurityException se) {
            logger.info("no permission to change log level");
        }
    }

    private void setDebugUse() {
        // avoid logging on parent:
        logger.setUseParentHandlers(false);
        Handler handler=new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.setLevel(GLOBAL_LEVEL);
        handler.setLevel(Level.ALL);
        // make debugging loggers noisy
//        Logger.getLogger("de.jreality.scene.proxy.tree").setLevel(Level.CONFIG);
//        Logger.getLogger("de.jreality.scene.tool").setLevel(Level.FINE);
//        Logger.getLogger("de.jreality.scene.pick").setLevel(Level.FINE);
//        Logger.getLogger("de.jreality.geometry").setLevel(Level.INFO);
        Logger.getLogger("de.jreality.io").setLevel(Level.INFO);
        Logger.getLogger("de.jreality.backends").setLevel(Level.FINE);
        Logger.getLogger("de.jreality.jogl").setLevel(Level.FINE);
    }

//    public void setLogToFile(String fileName) throws IOException {
//      logger.removeHandler(handler);
//      handler = new FileHandler(fileName);
//      handler.setFormatter(new SimpleFormatter());
//      logger.addHandler(handler);
//    }
    
    /**
     * factory method to get a logger. Usually this is the right
     * method to get a logger - LoggingSystem.getLogger(this).log(...)
     * @param
     * @return a logger named like the package of the given object
     */
    public static Logger getLogger(Object o) {
      try {
        return logSystem.getLog(o.getClass());
      } catch (Throwable t) {
        return Logger.getLogger("de.jreality");
      }
    }
    
    /**
     * factory method to get a logger. This is the right
     * method to get a logger in a static context - LoggingSystem.getLogger(MyCurrentClass.class).log(...)
     * @param
     * @return a logger named like the package of the given class
     */
    public static Logger getLogger(Class clazz) {
      try {
        return logSystem.getLog(clazz);
      } catch (Throwable t) {
        return Logger.getLogger("de.jreality");
      }
    }
    
    /**
     *
     * @return returns a logger for the package of the given class
     */
    private Logger getLog(Class clazz) {
        Package p = clazz.getPackage();
        if (p == null) return logger;
        String name = p.getName();
        return Logger.getLogger(name);
    }
}
