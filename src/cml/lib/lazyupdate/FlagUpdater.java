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
package cml.lib.lazyupdate;

import cml.lib.lazyupdate.Flags.FlagParent;
import cml.lib.threadmanager.ThreadManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class FlagUpdater {
    
    private static final Logger LOGGER = Logger.getLogger(FlagUpdater.class.getName());
    
    private static final Map<FlagParent, Consumer<Entry<String, Object>>> LISTENERS = new HashMap();
    
    /**
     * Creates a handler for a flag set
     * @param parent Flag parent
     * @param handler Change handler
     */
    public static void setListener(FlagParent parent, Consumer<Entry<String, Object>> handler) {
        LISTENERS.put(parent, handler);
    }
    
    /**
     * Removes a handler for a flag set
     * @param parent Flag parent
     */
    public static void removeHandler(FlagParent parent) {
        LISTENERS.remove(parent);
    }
    
    /**
     * Returns a handler for a flag set
     * @param parent Flag parent
     * @return The current handler for the given flag set
     */
    public static Consumer<Entry<String, Object>> getListener(FlagParent parent) {
        return LISTENERS.get(parent);
    }
    
    /**
     * Run a check of the various flags, and runs their consumers if any flags are set.
     */
    public static void check() {
        LISTENERS.forEach((parent, handler) -> {
            if (Flags.FLAGS.containsKey(parent)) {
                Flags.FLAGS.get(parent).entrySet().forEach((flag) -> handler.accept(flag));
            }
        });
    }

    /**
     * Recursively checks the various flags, and runs their consumers if any flags are set.
     * @param period Recursion period
     * @param unit Time unit for the given period
     */
    public static void recursivelyCheck(int period, TimeUnit unit) {
        @SuppressWarnings("SleepWhileInLoop")
        Thread checkerThread = new Thread(() -> {
            for (;;) {
                check();
                try {
                    unit.sleep(period);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Flag Updater stopped", ex);
                    break;
                }
                if (Thread.interrupted()) {
                    break;
                }
            }
        });
        ThreadManager.addThread(checkerThread);
        checkerThread.start();
    }
}
