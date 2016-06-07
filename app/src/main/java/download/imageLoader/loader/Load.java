package download.imageLoader.loader;


import java.util.HashMap;

import download.imageLoader.config.ImageConfig;
import download.imageLoader.request.BitmapRequest;

/**
 * 图片加载器
 * 
 * @author Administrator
 *
 */
public class Load {
	private static String[] keys = null;
	private static Class[] loadClass = null;
	private static HashMap<String,LoadInterface> map;
	static {
		if (keys == null){
			keys = new String[]{"http:","https:","assets:","drawable:","file:"};
		}
		if (loadClass == null){
			loadClass = new Class[]{HttpLoader.class,HttpLoader.class,AssetsLoader.class,DrawableLoader.class,FileLoader.class};
		}
		if (map == null){
			map = new HashMap<String, LoadInterface>();
		}


	}
	public static void loadBitmap(BitmapRequest request, ImageConfig config) {
		for (int i = 0; i < keys.length; i++) {
			if (request.path.contains(keys[i])){
				if (map.get(keys[i]) == null){
					try {
						map.put(keys[i],((LoadInterface) loadClass[i].newInstance()));

					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				if (map.get(keys[i]) != null){
					map.get(keys[i]).load(request,config);
				}
				break;
			}
		}
	}
}
