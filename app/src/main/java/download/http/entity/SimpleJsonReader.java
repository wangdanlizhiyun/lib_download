package download.http.entity;


import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.lang.reflect.Field;

import download.http.exception.AppException;

/**
 * Created by lizhiyun on 16/6/10.
 */
public class SimpleJsonReader {
    Field[] fields = null;
    public void readFromJsonReader(JsonReader reader) throws AppException{
        try {
            reader.beginObject();
            String node;
            while (reader.hasNext()){
                node = reader.nextName();
                Boolean isReaded = false;
                if (fields == null){
                    fields = this.getClass().getDeclaredFields();
                }
                for (Field field: fields
                     ) {
                    if (field.getName().equals(node)){
                        field.setAccessible(true);
                        Class field_class = field.getType();
                        if (field_class == Integer.TYPE){
                            field.set(this, reader.nextInt());
                            isReaded = true;
                        }else if (field_class == Long.TYPE){
                            field.set(this, reader.nextLong());
                            isReaded = true;
                        }else if (field_class == Boolean.TYPE){
                            field.set(this, reader.nextBoolean());
                            isReaded = true;
                        }else if (field_class == String.class){
                            field.set(this, reader.nextString());
                            isReaded = true;
                        }else if (field_class == Double.TYPE){
                            field.set(this, reader.nextDouble());
                            isReaded = true;
                        }
                        else if (field_class.getSuperclass() == SimpleJsonReader.class){
                            SimpleJsonReader baseJsonReader = (SimpleJsonReader) field_class.newInstance();
                            baseJsonReader.readFromJsonReader(reader);
                            field.set(this, baseJsonReader);
                            isReaded = true;
                        }
                    }
                }
                if (!isReaded){
                    reader.skipValue();
                }
            }
            reader.endObject();
        }catch (IOException e){
            throw new AppException(AppException.ErrorType.JSON,e.getMessage());
        } catch (IllegalAccessException e) {
            throw new AppException(AppException.ErrorType.JSON,e.getMessage());
        }
        catch (InstantiationException e) {
            throw new AppException(AppException.ErrorType.JSON,e.getMessage());
        }
    };
}
