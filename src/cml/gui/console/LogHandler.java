/*
 * Copyright (C) 2020 benne
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cml.gui.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author benne
 */
public class LogHandler extends Handler {

    private static final Logger LOGGER = Logger.getLogger(LogHandler.class.getName());

    public static final Set<Level> LEVELS = new HashSet();

    static {
        LEVELS.add(Level.CONFIG);
        LEVELS.add(Level.FINE);
        LEVELS.add(Level.FINER);
        LEVELS.add(Level.FINEST);
        LEVELS.add(Level.INFO);
        LEVELS.add(Level.WARNING);
        LEVELS.add(Level.SEVERE);
    }

    private List<LogRecord> cache = new ArrayList();

    private ConsoleController console;
    private final Set<Level> doLogs = new HashSet();

    public LogHandler(Level... logLevels) {
        if (logLevels.length == 0) {
            logLevels = new Level[]{Level.INFO};
        }
        if (logLevels.length == 1 && !logLevels[0].equals(Level.OFF)) {
            for (Level level : LEVELS) {
                if (level.intValue() <= logLevels[0].intValue() || logLevels[0].equals(Level.ALL)) {
                    doLogs.add(level);
                }
            }
        } else {
            doLogs.addAll(Arrays.asList(logLevels));
        }
        doLogs.remove(Level.OFF);
        doLogs.add(Level.ALL);
        super.setLevel(Level.ALL);
        super.setFormatter(new LogFormatter());
    }

    public LogHandler(ConsoleController console, Level... logLevels) {
        this(logLevels);
        initController(console);
    }

    public final void initController(ConsoleController console) {
        this.console = console;
        this.console.initHandler(doLogs, this);
        this.console.getItems().addAll(cache.stream().map((record) -> new LogData(record)).collect(Collectors.toList()));
    }

    @Override
    public synchronized void setLevel(Level newLevel) throws SecurityException {
        LEVELS.forEach((level) -> {
            if (level.intValue() <= newLevel.intValue() || newLevel.equals(Level.ALL)) {
                doLogs.add(level);
            } else {
                doLogs.remove(level);
            }
        });
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return doLogs.contains(record.getLevel());
    }
    
    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record) && record != null) {
            record.setMessage(getFormatter().format(record));
            add(record);
        }
    }

    private void add(LogRecord record) {
        if (console != null) {
            console.getItems().add(new LogData(record));
        }
        cache.add(record);
    }

    @Override
    public void flush() {
    }

    List<LogRecord> getCache() {
        return cache;
    }

    @Override
    public void close() throws SecurityException {
        this.console.allOff();
        this.console = null;
    }

    public static class LogOutputStream extends ByteArrayOutputStream {

        private final Handler handler;
        private final Level level;
        private final String source;
        private final boolean autoFlush;

        public LogOutputStream(Handler handler, Level level, String source) {
            this.handler = handler;
            this.level = level;
            this.source = source;
            this.autoFlush = false;
        }
        
        public LogOutputStream(Handler handler, Level level, String source, boolean autoFlush) {
            this.handler = handler;
            this.level = level;
            this.source = source;
            this.autoFlush = autoFlush;
        }

        @Override
        public synchronized void write(int b) {
            super.write(b);
            if (autoFlush && b == (int) '\n') {
                try {
                    flush();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to autoflush", ex);
                }
            }
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            LogRecord record = new LogRecord(level, new String(toByteArray()));
            record.setLoggerName(source);
            handler.publish(record);
            this.reset();
        }
    }

    private static class LogFormatter extends Formatter {

        private final String format = "%s%s%n";

        @Override
        public synchronized String format(LogRecord record) {
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    pw.println();
                    record.getThrown().printStackTrace(pw);
                }
                throwable = sw.toString();
            }
            return String.format(format, message, throwable).trim();
        }

    }

}
