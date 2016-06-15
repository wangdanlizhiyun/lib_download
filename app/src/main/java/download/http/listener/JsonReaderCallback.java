package download.http.listener;



import android.annotation.TargetApi;
import android.os.Build;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

import download.http.core.Http;
import download.http.entity.SimpleJsonReader;
import download.http.exception.AppException;

/**
 * Created by lizhiyun on 16/6/3.
 */
public abstract class JsonReaderCallback<T extends SimpleJsonReader> extends AbsCallback<T> {
    public JsonReaderCallback(){
        setCachePath(Http.tempFileRootPath + File.separator + UUID.randomUUID());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected T parseData(String result) throws AppException{
        try{
            ParameterizedType p = (ParameterizedType) this.getClass().getGenericSuperclass();
            Type type = p.getActualTypeArguments()[0];
            Class<T> clazz = (Class<T>) type;
            T t = clazz.newInstance();
            FileReader in = new FileReader(result);
            JsonReader reader = new JsonReader(in);
            t.readFromJsonReader(reader);
            return t;
        }catch (Exception e){
            throw new AppException(AppException.ErrorType.JSON,e.getMessage());
        }finally {
            File file = new File(result);
            if (file != null && file.exists()){
                file.delete();
            }
        }

    }
}
