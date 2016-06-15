package download.otherFileLoader.core;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lizhiyun on 16/6/15.
 */
public class ConnectRunnable implements Runnable {
    private String url;
    public ConnectRunnable(String url, ConnectListener listener){
        this.url = url;
        this.listener = listener;
    }
    private Boolean isRunning = false;
    ConnectListener listener;
    public Boolean isRunning (){
        return isRunning;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            connection.setConnectTimeout(5 * 1000);
            connection.setReadTimeout(5*1000);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupport = false;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL){
                isSupport = true;
            }
            isRunning = false;
            listener.onConnected(contentLength,isSupport);
        }catch (Exception e){
            isRunning = false;
            listener.onError(e.getMessage());
            e.printStackTrace();
        }finally {
            if (connection != null){
                connection.disconnect();
            }
        }

    }
    interface ConnectListener{
        void onConnected(int totalLength, Boolean isSupport);
        void onError(String message);
    }
    public void cancel(){
        Thread.currentThread().interrupt();
    }
}
