package com.didi.community.socket;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class Log {


	  public static final int LEVEL_NONE = 6;
	  public static final int LEVEL_ERROR = 5;
	  public static final int LEVEL_WARN = 4;
	  public static final int LEVEL_INFO = 3;
	  public static final int LEVEL_DEBUG = 2;
	  public static final int LEVEL_TRACE = 1;
	  private static int level = 3;
	  public static boolean ERROR = level <= 5;
	  public static boolean WARN = level <= 4;
	  public static boolean INFO = level <= 3;
	  public static boolean DEBUG = level <= 2;
	  public static boolean TRACE = level <= 1;
	  
	  public static void set(int paramInt)
	  {
	    level = paramInt;
	    ERROR = paramInt <= 5;
	    WARN = paramInt <= 4;
	    INFO = paramInt <= 3;
	    DEBUG = paramInt <= 2;
	    TRACE = paramInt <= 1;
	  }
	  
	  public static void NONE()
	  {
	    set(6);
	  }
	  
	  public static void ERROR()
	  {
	    set(5);
	  }
	  
	  public static void WARN()
	  {
	    set(4);
	  }
	  
	  public static void INFO()
	  {
	    set(3);
	  }
	  
	  public static void DEBUG()
	  {
	    set(2);
	  }
	  
	  public static void TRACE()
	  {
	    set(1);
	  }
	  
	  public static void setLogger(Logger paramLogger)
	  {
	    logger = paramLogger;
	  }
	  
	  private static Logger logger = new Logger();
	  
	  public static void error(String paramString, Throwable paramThrowable)
	  {
	    if (ERROR) {
	      logger.log(5, null, paramString, paramThrowable);
	    }
	  }
	  
	  public static void error(String paramString1, String paramString2, Throwable paramThrowable)
	  {
	    if (ERROR) {
	      logger.log(5, paramString1, paramString2, paramThrowable);
	    }
	  }
	  
	  public static void error(String paramString)
	  {
	    if (ERROR) {
	      logger.log(5, null, paramString, null);
	    }
	  }
	  
	  public static void error(String paramString1, String paramString2)
	  {
	    if (ERROR) {
	      logger.log(5, paramString1, paramString2, null);
	    }
	  }
	  
	  public static void warn(String paramString, Throwable paramThrowable)
	  {
	    if (WARN) {
	      logger.log(4, null, paramString, paramThrowable);
	    }
	  }
	  
	  public static void warn(String paramString1, String paramString2, Throwable paramThrowable)
	  {
	    if (WARN) {
	      logger.log(4, paramString1, paramString2, paramThrowable);
	    }
	  }
	  
	  public static void warn(String paramString)
	  {
	    if (WARN) {
	      logger.log(4, null, paramString, null);
	    }
	  }
	  
	  public static void warn(String paramString1, String paramString2)
	  {
	    if (WARN) {
	      logger.log(4, paramString1, paramString2, null);
	    }
	  }
	  
	  public static void info(String paramString, Throwable paramThrowable)
	  {
	    if (INFO) {
	      logger.log(3, null, paramString, paramThrowable);
	    }
	  }
	  
	  public static void info(String paramString1, String paramString2, Throwable paramThrowable)
	  {
	    if (INFO) {
	      logger.log(3, paramString1, paramString2, paramThrowable);
	    }
	  }
	  
	  public static void info(String paramString)
	  {
	    if (INFO) {
	      logger.log(3, null, paramString, null);
	    }
	  }
	  
	  public static void info(String paramString1, String paramString2)
	  {
	    if (INFO) {
	      logger.log(3, paramString1, paramString2, null);
	    }
	  }
	  
	  public static void debug(String paramString, Throwable paramThrowable)
	  {
	    if (DEBUG) {
	      logger.log(2, null, paramString, paramThrowable);
	    }
	  }
	  
	  public static void debug(String paramString1, String paramString2, Throwable paramThrowable)
	  {
	    if (DEBUG) {
	      logger.log(2, paramString1, paramString2, paramThrowable);
	    }
	  }
	  
	  public static void debug(String paramString)
	  {
	    if (DEBUG) {
	      logger.log(2, null, paramString, null);
	    }
	  }
	  
	  public static void debug(String paramString1, String paramString2)
	  {
	    if (DEBUG) {
	      logger.log(2, paramString1, paramString2, null);
	    }
	  }
	  
	  public static void trace(String paramString, Throwable paramThrowable)
	  {
	    if (TRACE) {
	      logger.log(1, null, paramString, paramThrowable);
	    }
	  }
	  
	  public static void trace(String paramString1, String paramString2, Throwable paramThrowable)
	  {
	    if (TRACE) {
	      logger.log(1, paramString1, paramString2, paramThrowable);
	    }
	  }
	  
	  public static void trace(String paramString)
	  {
	    if (TRACE) {
	      logger.log(1, null, paramString, null);
	    }
	  }
	  
	  public static void trace(String paramString1, String paramString2)
	  {
	    if (TRACE) {
	      logger.log(1, paramString1, paramString2, null);
	    }
	  }
	  
	  public static class Logger
	  {
	    private long firstLogTime = new Date().getTime();
	    
	    public void log(int paramInt, String paramString1, String paramString2, Throwable paramThrowable)
	    {
	      StringBuilder localStringBuilder = new StringBuilder(256);
	      
	      long l1 = new Date().getTime() - this.firstLogTime;
	      long l2 = l1 / 60000L;
	      long l3 = l1 / 1000L % 60L;
	      if (l2 <= 9L) {
	        localStringBuilder.append('0');
	      }
	      localStringBuilder.append(l2);
	      localStringBuilder.append(':');
	      if (l3 <= 9L) {
	        localStringBuilder.append('0');
	      }
	      localStringBuilder.append(l3);
	      switch (paramInt)
	      {
	      case 5: 
	        localStringBuilder.append(" ERROR: ");
	        break;
	      case 4: 
	        localStringBuilder.append("  WARN: ");
	        break;
	      case 3: 
	        localStringBuilder.append("  INFO: ");
	        break;
	      case 2: 
	        localStringBuilder.append(" DEBUG: ");
	        break;
	      case 1: 
	        localStringBuilder.append(" TRACE: ");
	      }
	      if (paramString1 != null)
	      {
	        localStringBuilder.append('[');
	        localStringBuilder.append(paramString1);
	        localStringBuilder.append("] ");
	      }
	      localStringBuilder.append(paramString2);
	      if (paramThrowable != null)
	      {
	        StringWriter localStringWriter = new StringWriter(256);
	        paramThrowable.printStackTrace(new PrintWriter(localStringWriter));
	        localStringBuilder.append('\n');
	        localStringBuilder.append(localStringWriter.toString().trim());
	      }
	      print(localStringBuilder.toString());
	    }
	    
	    protected void print(String paramString)
	    {
	      System.out.println(paramString);
	    }
	  }
}
