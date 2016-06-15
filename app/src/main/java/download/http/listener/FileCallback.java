package download.http.listener;



/**
 * Created by lizhiyun on 16/6/3.
 */
public abstract class FileCallback extends AbsCallback<String> {

    @Override
    protected String parseData(String result) {
        return result;
    }
}
