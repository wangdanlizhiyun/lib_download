package download.otherFileLoader.core;

/**
 * Created by lizhiyun on 16/6/12.
 */
public class Constants {

    public static final int WHAT_DOWNLOADING = 10;
    public static final int WHAT_FINISH = 12;
    public static final int WHAT_ERROR = 13;


    public static final int CONNECT_TIME = 10000;
    public static final int READ_TIME = 10000;
    public static final int BLOB_COUNT = 5;

    public static final int DOWNLOAD_STATE_IDLE = 0;
    public static final int DOWNLOAD_STATE_DOWNLOADING = 2;
    public static final int DOWNLOAD_STATE_FINISH = 1;
    public static final int DOWNLOAD_STATE_ERROR = 3;
    public static final int DOWNLOAD_STATE_PAUSE = 4;
    public static final int DOWNLOAD_STATE_CANCEL = 5;
}
