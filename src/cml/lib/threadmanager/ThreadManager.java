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
package cml.lib.threadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author benne
 */
public class ThreadManager {
    private static final List<Thread> THREADS = new ArrayList();
    private static final List<Runnable> ON_STOP = new ArrayList();
    private static boolean stopped = false;
    
    public static void addThread(Thread thread) {
        THREADS.add(thread);
    }
    
    public static void removeThread(Thread thread) {
        THREADS.remove(thread);
    }
    
    public static void stop() {
        stopped = true;
        THREADS.forEach(Thread::interrupt);
        ON_STOP.forEach(Runnable::run);
    }
    
    public static void onStop(Runnable run) {
        ON_STOP.add(run);
    }
    
    public static boolean isStopped() {
        return stopped;
    }
    
    public static final ThreadManager MANAGER = new ThreadManager(15);
    
    public final ExecutorService executor;
    
    public ThreadManager(int threads) {
        executor = Executors.newFixedThreadPool(threads);
    }
    
}
