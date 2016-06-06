package download.http.exception;

/**
 * Created by lizhiyun on 16/6/5.
 */
public class AppException extends  Exception {

    public int statusCode;
    public String responseMessage;
    public enum ErrorType{TIMEOUT,SERVER,JSON,IO,FILENOTFOUND,CANCEL}

    public ErrorType errorType;
    public AppException(int statusCode, String responseMessage){
        super();
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.errorType = ErrorType.SERVER;
    }
    public AppException(ErrorType errorType, String responseMessage){
        super(responseMessage);
        this.errorType = errorType;
    }
}
