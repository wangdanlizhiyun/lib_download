package download.http.listener;

import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

    public void onCancel(){}
    public T onPost(T t){return t;}

    public void onFailure(AppException exception){}

    private String path;

    @Override
    public T parse(Request request,HttpURLConnection connection, download.http.listener.OnProgressDownloadListener listener) throws AppException {
        try {
            request.checkIfCancelled();
            int status = connection.getResponseCode();
            InputStream is = null;
            BufferedReader reader = null;
            String encode = connection.getContentEncoding();
            if (encode != null && "gzip".equalsIgnoreCase(encode)){
                is = new GZIPInputStream(connection.getInputStream());
            }else if (encode != null && "deflate".equalsIgnoreCase(encode)){
                is = new InflaterInputStream(connection.getInputStream());
            }else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            OutputStream out;
            if (status == HttpStatus.SC_OK){
                if (path == null){
                    out = new ByteArrayOutputStream();
                }else {
                    out = new FileOutputStream(path);
                }
                int totalLength = connection.getContentLength();
                int curLength = 0;
                if (reader == null){
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = is.read(buffer)) != -1){
                        request.checkIfCancelled();
                        out.write(buffer, 0, len);
                        curLength += len;
                        if (listener != null){
                            listener.onProgressDownload(curLength, totalLength);
                        }
                    }
                }else {
                    String s;
                    while ((s = reader.readLine()) != null){
                        request.checkIfCancelled();
                        byte[] bytes = s.getBytes();
                        out.write(bytes,0,bytes.length);
                        curLength += bytes.length;
                        if (listener != null){
                            listener.onProgressDownload(curLength, totalLength);
                        }
                    }
                }
                if (is != null){
                    is.close();
                }
                if (out != null){
                    out.flush();
                    out.close();
                }
                if (reader != null){
                    reader.close();
                }
                if (path == null){
                    String result = new String(((ByteArrayOutputStream)out).toByteArray()).trim();
                    return onPost(parseData(result));
                }else {
                    return onPost(parseData(path));
                }

            }else {
                throw new AppException(AppException.ErrorType.SERVER,connection.getResponseMessage());
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
