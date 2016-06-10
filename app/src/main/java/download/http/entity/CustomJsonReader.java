package download.http.entity;

import com.google.gson.stream.JsonReader;

import download.http.exception.AppException;

/**
 * Created by lizhiyun on 16/6/10.
 */
public interface CustomJsonReader {
    public abstract void readJsonReader(JsonReader jsonReader) throws AppException;
}
