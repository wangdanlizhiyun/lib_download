package download.http.listener;


import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;


/**
 * Created by lizhiyun on 16/6/3.
 */
public abstract class JsonCallback<T> extends AbsCallback<T> {

    @Override
    protected T parseData(String result) {
        ParameterizedType p = (ParameterizedType) this.getClass().getGenericSuperclass();
        Class c = (Class) p.getActualTypeArguments()[0];
        return (T) new Gson().fromJson(result, c);
    }
}
