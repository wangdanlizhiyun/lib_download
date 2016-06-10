package download.http.listener;

import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import download.http.exception.AppException;
import download.http.request.Request;
import download.utils.Util;

/**
 * Created by lizhiyun on 16/6/3.
 */
public abstract class AbsCallback<T> implements ICallback<T> {

    public void onProgressUpdate(int curLength, int totalLength){}
    public void onProgressDownload(int curLength, int totalLength){}

    public void onCancel(){}
    public T onPost(T t){return t;}

    public void onFailure(AppException exception){}

    private String path;

    @Override
    public T parse(Request request,HttpURLConnection connection,OnProgressDownloadListener listener) throws AppException {
        try {
            request.checkIfCancelled();
            int status = connection.getResponseCode();
            InputStream is = null;
            if (status == HttpStatus.SC_OK){
                String encode = connection.getContentEncoding();
                if (encode != null && "gzip".equalsIgnoreCase(encode)){
                    is = new GZIPInputStream(connection.getInputStream());
                }else if (encode != null && "deflate".equalsIgnoreCase(encode)){
                    is = new InflaterInputStream(connection.getInputStream());
                }else {
                    is = connection.getInputStream();
                }
                if (path == null){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int totalLength = connection.getContentLength();
                    int curLength = 0;
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = is.read(buffer)) != -1){
                        request.checkIfCancelled();
                        out.write(buffer, 0, len);
                        curLength += len;
                        if (listener != null){
                            listener.onProgressUpdate(curLength, totalLength);
                        }
                    }
                    Util.close(connection, out, is);
                    String result = new String(out.toByteArray()).trim();
                    return onPost(parseData(result));
                }else {
                    FileOutputStream out = new FileOutputStream(path);
                    int totalLength = connection.getContentLength();
                    int curLength = 0;
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = is.read(buffer)) != -1){
                        request.checkIfCancelled();
                        out.write(buffer,0,len);
                        curLength += len;
                        if (listener != null){
                            listener.onProgressUpdate(curLength, totalLength);
                        }
                    }
                    Util.close(connection, out, is);
                    return onPost(parseData(path));
                }

            }else {
                throw new AppException(status,connection.getResponseMessage());
            }
        }catch (Exception e){
            throw new AppException(AppException.ErrorType.IO,e.getMessage());
        }
    }


    protected abstract T parseData(String result) throws AppException;

    public ICallback setCachePath(String path){
        this.path = path;
        return this;
    };

}
