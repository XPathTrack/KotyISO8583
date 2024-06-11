package utils;

import java.util.LinkedList;

/**
 * @author PathTrack
 */
public class TaskThread {

    private static final String TAG = "TaskThread";
    private volatile boolean isRunning = false;
    private Thread thread;
    private final LinkedList<Runnable> taskQueue = new LinkedList<>();

    private final Runnable mainRunnable = () -> {
        Runnable currentTask;
        while (isRunning) {
            synchronized (taskQueue) {
                while (taskQueue.isEmpty()) {
                    try {
                        taskQueue.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                currentTask = taskQueue.pollFirst();
            }
            try {
                currentTask.run();
            } catch (Exception e) {
                System.out.println(TAG + "/Falló la ejecución de la tarea. -> " + e.getMessage());
            }
        }
    };

    public void release() {
        synchronized (taskQueue) {
            if (!isRunning)
                return;
            isRunning = false;
            taskQueue.clear();
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                thread.interrupt();
            }
            thread = null;
        }
    }

    public void addTask(Runnable runnable) {
        synchronized (taskQueue) {
            if (!isRunning) {
                isRunning = true;
                thread = new Thread(mainRunnable);
                thread.start();
            }
            taskQueue.add(runnable);
            taskQueue.notify();
        }
    }
}