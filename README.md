# lib_download
处理异步下载的库，包含图片下载和ormlite(马天宇的开源)，且全局公用一个线程池
支持非ImageView的view也支持哦
初次从网络下载的图第一次显示为淡入效果，对于1m以上的大图在使用本地缓存的情况下边下载边显示
同最新glide一样只缓存最大的图
虽然有默认的正在加载和加载失败的图片，但是还是可以打成jar包使用
支持预加载：BmLoader.preLoad(uri);
wifi模式：BmLoader.setOnlyWifiMode（true）
仅内存缓存模式：BmLoader.setOnlyMemoryMode（true）
支持本地和网络图片，径格式示例为："http://img.blog.csdn.net/20160114230048304",//网络
    			"assets://anim.gif",
                "drawable://"+R.drawable.anim,//资源路径gif图
                	"file:///mnt/sdcard/paint.png",//sd卡
使用方法简单：BmLoader.load(uri, imageView);
对于特殊的需求可以使用回调自己处理：
        BmLoader.load(uri, imageView, new BackListener() {
                @Override
                public void onProcess(int percent) {}
                @Override
                public void onSuccess(Bitmap bitmap, Movie movie) {
		public void onFailed()｛｝;
            });
            
如果view使用或者继承download.imageLoader.view.GifMovieView这个类的话支持gif图，否则只能用回调自己自定义view实现。
如果使用类download.imageLoader.view.GifMovieView，调用方法更简单了：view.bind(uri)。是否极致方便？总大小仅200多k。
