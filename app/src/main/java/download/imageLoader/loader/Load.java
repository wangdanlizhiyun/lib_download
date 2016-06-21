package download.imageLoader.loader;

//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖镇楼                  BUG辟易
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？


import java.util.HashMap;
import java.util.Map;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;

/**
 * 图片加载器
 * 
 * @author Administrator
 *
 */
public class Load {
	private static HashMap<String,LoadInterface> map;
	private static HashMap<String,Class> classHashMap;
	static {
		if (classHashMap == null){
			classHashMap = new HashMap<String, Class>();
			classHashMap.put("http:",HttpLoader.class);
			classHashMap.put("https:",HttpLoader.class);
			classHashMap.put("assets:",AssetsLoader.class);
			classHashMap.put("drawable:",DrawableLoader.class);
			classHashMap.put("file:",FileLoader.class);
		}
		if (map == null){
			map = new HashMap<String, LoadInterface>();
		}


	}
	public static void loadBitmap(BitmapRequest request, ImageConfig config) {
		for (Map.Entry<String,Class> entry: classHashMap.entrySet()
			 ) {
			if (request.path.contains(entry.getKey())){
				try {
					map.put(entry.getKey(),((LoadInterface) entry.getValue().newInstance()));

				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (map.get(entry.getKey()) != null){
					map.get(entry.getKey()).load(request,config);
				}
				break;
			}
		}
	}
}
