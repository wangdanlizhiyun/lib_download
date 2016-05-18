package download.otherFileLoader.db;

import java.util.ArrayList;
import android.content.Context;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import download.otherFileLoader.request.DownFile;


public class DownFileManager {
	private LiteOrm orm;

	private static volatile DownFileManager instance = null;
	public static DownFileManager getInstance(Context context) {
		if (instance == null) {
			synchronized (DownFileManager.class) {
				if (instance == null) {
					instance = new DownFileManager(context);
				}
			}
		}
		return instance;
	}
	private DownFileManager(Context context){
		orm = LiteOrm.newSingleInstance(context, "downfile");
	}
	public DownFile getDownFile(String path){
		return orm.queryById(path, DownFile.class);
	}
	public DownFile getDownFileByPkg(String pkg){
		QueryBuilder<DownFile> qb = new QueryBuilder<DownFile>(DownFile.class)
                .whereEquals(DownFile.COL_DOWNURL, pkg);
		ArrayList<DownFile> list = orm.query(qb);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	public synchronized void  save(DownFile mDownFile){
		if (mDownFile.getTotalLength() != 0 && mDownFile.getDownLength() == mDownFile.getTotalLength()) {
			mDownFile.setState(1);
		}
		orm.save(mDownFile);
	}
	public synchronized void delete(String path){
		orm.delete(WhereBuilder
                .create(DownFile.class)
                .equals(DownFile.COL_DOWNURL, path));
	}
}
