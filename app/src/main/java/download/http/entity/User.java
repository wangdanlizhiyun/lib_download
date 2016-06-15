package download.http.entity;


/**
 * Created by lizhiyun on 16/6/3.
 */
public class User extends SimpleJsonReader {
    public String id;
    public String account;
    public String email;
    public String username;
    public String token;

    @Override
    public String toString() {
        return username+"  "+email;
    }
}
