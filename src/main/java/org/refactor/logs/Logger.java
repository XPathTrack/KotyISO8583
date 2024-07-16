package org.refactor.logs;

import org.refactor.ui.MainFrame;
import org.refactor.utils.TaskThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Logger {

    private static final String TAG = "Logger";

    //LOG LEVELS
    private static final String INFO = "I";
    private static final String DEBUG = "D";
    private static final String WARNING = "W";
    private static final String ERROR = "E";

    private static final String EXTENSION = ".log";

    private static final TaskThread tasks = new TaskThread();

    private Logger() {
    }

    public static void i(String tag, String message) {
        log(INFO, tag, message);
    }

    public static void d(String tag, String message) {
        log(DEBUG, tag, message);
    }

    public static void w(String tag, String message) {
        log(WARNING, tag, message);
    }

    public static void e(String tag, String message) {
        log(ERROR, tag, message);
    }

    private static void log(final String level, final String tag, final String message) {
        final LogData logData = new LogData(level, tag, message);
        tasks.addTask(() -> {
            try {
                writeToFile(logData);
            } catch (Exception e) {
                System.out.println(TAG + "Falló la escritura del log: " + logData + ". -> " + e.getMessage());
            }
        });
    }

    private static void writeToFile(LogData log) throws Exception {

        File file = new File(MainFrame.PROGRAM_PATH + "/" + log.getTimeDate().substring(0,10) + EXTENSION);
        if (!file.exists()) {
            file.createNewFile();
        }

        try (BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true))) {
            buffer.write(log.toString());
            buffer.newLine();
            buffer.flush();
        }
    }

    private static class LogData {

        private final long timeStamp;
        private String timeDate;
        private final String threadName;
        private final long threadId;
        private final String level;
        private final String tag;
        private final String message;
        private static final SimpleDateFormat dateFormat;

        static {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getDefault());
        }

        public LogData(String level, String tag, String message) {
            this.timeStamp = System.currentTimeMillis();
            Thread thread = Thread.currentThread();
            this.threadName = thread.getName();
            this.threadId = thread.getId();
            this.level = level;
            this.tag = tag;
            this.message = message;
        }

        private String formatTimeStamp(long timestamp) {
            return dateFormat.format(new Date(timestamp));
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getTimeDate() {
            if (timeDate == null)
                timeDate = formatTimeStamp(timeStamp);
            return timeDate;
        }

        public String getThreadName() {
            return threadName;
        }

        public long getThreadId() {
            return threadId;
        }

        public String getLevel() {
            return level;
        }

        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("%s %s#%s %s/%s: %s",
                    getTimeDate(), threadName, threadId, level, tag, message);
        }
    }
}
