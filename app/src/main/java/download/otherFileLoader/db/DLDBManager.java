package download.otherFileLoader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;

import download.otherFileLoader.request.DownFile;

public final class DLDBManager {
    private static DLDBManager sManager;
    private DLDBHelper helper;
    private Context context;

    private DLDBManager(Context context) {
        helper = new DLDBHelper(context);
    }

    static DLDBManager getInstance(Context context) {
        if (null == sManager) {
            sManager = new DLDBManager(context);
        }
        return sManager;
    }

    public synchronized void insertTaskInfo(DownFile info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("url",info.url);
        values.put("path",info.downPath);
        values.put("name",info.name);
        values.put("downlength",info.downLength);
        values.put("totallength", info.totalLength);
        values.put("state",info.state.value);

        if (info.ranges != null && info.ranges.size() > 0){
            StringBuilder sb = new StringBuilder();
            for (int i = 0;i < info.ranges.size();i++){
                if (i > 0){
                    sb.append(",");
                }
                sb.append(info.ranges.get(i));
            }
            values.put("rangers",sb.toString());
        }
        db.insert(DLDBHelper.TABLENAME,null,values);
    }

    public synchronized void insertOrUpdate(DownFile info){
        if (queryTaskInfo(info) == null){
            insertTaskInfo(info);
        }else {
            updateTaskInfo(info);
        }
    }

    public synchronized void deleteTaskInfo(DownFile info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DLDBHelper.TABLENAME,"url=? and path=?",new String[]{info.url,info.downPath});
        db.close();
    }

    public synchronized void updateTaskInfo(DownFile info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("url",info.url);
        values.put("path",info.downPath);
        values.put("name",info.name);
        values.put("downlength",info.downLength);
        values.put("totallength", info.totalLength);
        values.put("state",info.state.value);
        if (info.ranges != null && info.ranges.size() > 0){
            StringBuilder sb = new StringBuilder();
            if (info.ranges != null){
                for (int i = 0;i < info.ranges.size();i++){
                    if (i > 0){
                        sb.append(",");
                    }
                    sb.append(info.ranges.get(i));
                }
            }
            values.put("rangers",sb.toString());
        }
        db.update(DLDBHelper.TABLENAME,values,"url = ? and path = ?",new String[]{info.url,info.downPath});
    }

    public DownFile queryTaskInfo(DownFile info) {
        DownFile downFile = null;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DLDBHelper.TABLENAME,new String[]{"name,issupportrange,downlength,totallength,rangers,state"},"url = ? and path = ?",new String[]{info.url,info.downPath},null,null,"url desc","1,2");

        while (cursor.moveToNext()){
            if (downFile == null){
                downFile = new DownFile(info.url);
                downFile.downPath = info.downPath;
            }
            downFile.name = cursor.getString(0);
            downFile.isSuppurtRanger = cursor.getInt(1) > 0;
            downFile.downLength = cursor.getInt(2);
            downFile.totalLength = cursor.getInt(3);
            String rangers = cursor.getString(4);
            if (!TextUtils.isEmpty(rangers.trim())){
                String[] strings = rangers.split(",");
                if (strings.length > 0){
                    downFile.ranges = new HashMap<Integer,Integer>();
                }
                for (int i = 0;i<strings.length;i++){
                    downFile.ranges.put(i,Integer.parseInt(strings[i]));
                }
            }
            int value = cursor.getInt(5);
            switch (value){
                case 0:
                    downFile.state = DownFile.DownloadStatus.IDLE;
                    break;
                case 1:
                    downFile.state = DownFile.DownloadStatus.FINISH;
                    break;
                case 2:
                    downFile.state = DownFile.DownloadStatus.DOWNLOADING;
                    break;
                case 3:
                    downFile.state = DownFile.DownloadStatus.ERROR;
                    break;
                case 4:
                    downFile.state = DownFile.DownloadStatus.PAUSE;
                    break;
                case 5:
                    downFile.state = DownFile.DownloadStatus.CANCEL;
                    break;
                case 6:
                    downFile.state = DownFile.DownloadStatus.WAITING;
                    break;
            }
            break;
        }
        cursor.close();
        db.close();
        return downFile;
    }

}