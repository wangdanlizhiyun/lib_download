package download.imageLoader.core;

import java.util.LinkedHashMap;

import download.imageLoader.request.BitmapRequest;

/**
 * 该类用于解决较近的2个相同uri下载造成的可能的bug。
 * Created by lizhiyun on 16/5/23.
 */
public class RunningTasksManager {

    private LinkedHashMap<String, String> doingMap = null;
    private RunningTasksManager(){}
    public RunningTasksManager(int threadCount){
        doingMap = new LinkedHashMap<String, String>(
                threadCount);
    }
    public Boolean hasDoingTask(BitmapRequest request) {
        synchronized (doingMap) {
            return doingMap.containsKey(request.path);
        }
    }

    public void addDoingTask(BitmapRequest request) {
        synchronized (doingMap) {
            doingMap.put(request.path, request.path);
        }
    }

    public void removeDoingTask(BitmapRequest request) {
        synchronized (doingMap) {
            doingMap.remove(request.path);
        }
    }
}
