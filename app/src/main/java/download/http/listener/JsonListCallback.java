package download.http.listener;


import android.annotation.TargetApi;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONObject;

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
public abstract class JsonListCallback<T> extends AbsCallback<ArrayList<T>> {
    String key="";
    public JsonListCallback(String key){
        this.key = key;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected ArrayList<T> parseData(String result) throws AppException{
        ParameterizedType p = (ParameterizedType) this.getClass().getGenericSuperclass();
        Class c = (Class) p.getActualTypeArguments()[0];
        ArrayList<T> ts = new ArrayList<T>();

        T t;
        try{
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            if (jsonArray.length() > 0){
                for (int i = 0;i < jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    t = (T) new Gson().fromJson(object.toString(), c);
                    ts.add(t);
                }
            }
            return ts;
        }catch (Exception e){
            throw new AppException(AppException.ErrorType.JSON,e.getMessage());
        }
    }
}
