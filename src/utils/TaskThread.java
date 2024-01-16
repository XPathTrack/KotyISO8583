package utils;

import java.util.LinkedList;

/**
 *
 * @author PathTrack
 */
public class TaskThread {

    private boolean isRunning = true;
    private final LinkedList<Runnable> taskQueue = new LinkedList<>();

    private final Runnable mainRunnable = () -> {
        Runnable currentTask;
        while (isRunning) {
            synchronized (taskQueue) {
                if (taskQueue.isEmpty()) {
                    try {
                        taskQueue.wait();
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
                currentTask = taskQueue.pollFirst();
            }
            currentTask.run();
        }
    };

    public void release() {
        if (thread != null) {
            isRunning = false;
            synchronized (taskQueue) {
                taskQueue.clear();
            }
            thread.interrupt();
            thread = null;
        }
    }

    private Thread thread;

    public void addTask(Runnable runnable) {
        if (thread == null) {
            isRunning = true;
            thread = new Thread(mainRunnable);
            thread.start();
        }
        synchronized (taskQueue) {
            taskQueue.add(runnable);
            taskQueue.notify();
        }
    }
}