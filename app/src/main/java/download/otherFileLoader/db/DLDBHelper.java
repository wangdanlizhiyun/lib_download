package download.otherFileLoader.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class DLDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "df.db";
    private static final int DB_VERSION = 3;
    public static final String TABLENAME = "downloadfile";
    private static final String TB_CREATE =
            "CREATE TABLE "+
                    TABLENAME+"(id INTEGER PRIMARY KEY AUTOINCREMENT, url CHAR, path CHAR NOT NULL," +
                    " name CHAR,"+
                    " issupportrange INTEGER, downlength INTEGER, totallength INTEGER, rangers CHAR, state INTEGER" +
                    ")";

    public DLDBHelper(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLENAME);
        onCreate(db);
    }
}
