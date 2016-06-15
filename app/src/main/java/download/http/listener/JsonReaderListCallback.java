package download.http.listener;


import android.annotation.TargetApi;
import android.os.Build;

import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

import download.http.core.Http;
import download.http.entity.SimpleJsonReader;
import download.http.exception.AppException;

/**
 * Created by lizhiyun on 16/6/3.
 */
public abstract class JsonReaderListCallback<T extends SimpleJsonReader> extends AbsCallback<ArrayList<T>> {
    String key="data";
    public JsonReaderListCallback(String key){
        this.key = key;
        setCachePath(Http.tempFileRootPath + File.separator + UUID.randomUUID());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected ArrayList<T> parseData(String result) throws AppException{
        ArrayList<T> ts = new ArrayList<T>();
        T t;
        try{
            ParameterizedType p = (ParameterizedType) this.getClass().getGenericSuperclass();
            Type type = p.getActualTypeArguments()[0];
            Class<T> clazz = (Class<T>) type;
            FileReader in = new FileReader(result);
            JsonReader reader = new JsonReader(in);
            String node = "";
            reader.beginObject();
            while (reader.hasNext()){
                node = reader.nextName();
                if (node.equals(key)){
                    reader.beginArray();
                    while (reader.hasNext()){
                        t = clazz.newInstance();
                        t.readFromJsonReader(reader);
                        ts.add(t);
                    }
                    reader.endArray();
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return ts;
        }catch (Exception e){
            throw new AppException(AppException.ErrorType.JSON,e.getMessage());
        }finally {
            try {
                File file = new File(result);
                if (file != null && file.exists()){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
