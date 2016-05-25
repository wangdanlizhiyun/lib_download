# lib_download
这是一个处理异步下载的库，包含图片下载文件下载和ormlite(马天宇的开源)，特点如下：

1.全局公用一个线程池

2.非ImageView的view也支持哦

3.初次从网络下载的图第一次显示为淡入效果，对于1m以上的大图在使用本地缓存的情况下边下载边显示

4.支持预加载：BmLoader.preLoad(uri);

5.优化了listview等快速滑动时的图片加载

6.如果view使用或者继承download.imageLoader.view.GifMovieView这个类的话支持gif图，否则只能用回调自己自定义view实现。如果使用类download.imageLoader.view.GifMovieView，调用方法更简单了：view.bind(uri)。是否极致方便？总大小仅200多k

7.支持本地和网络图片，径格式示例为：
		"http://img.blog.csdn.net/20160114230048304",
    		"assets://anim.gif",
                "drawable://"+R.drawable.anim,
                "file:///mnt/sdcard/paint.png",
		BmLoader.load(uri, imageView);

对于特殊的需求可以使用回调自己处理：
        BmLoader.load(uri, imageView, new BackListener() {
        
                @Override
                
                public void onProcess(int percent) {}
               
                @Override
                public void onSuccess(Bitmap bitmap, Movie movie) {｝
        
                @Override
	        public void onFailed()｛｝;
            });
            或者使用接口适配器：BmLoader.load(uri, imageView, new BackListenerAdapter() {
                    @Override
                    public void onSuccess(BitmapDrawable bitmap, Movie movie) {
                        super.onSuccess(bitmap, movie);
                    }
                });
                
8.可设置圆形imageView.setCircle().bind(uri);
设置矩形imageView.setRectangle().bind(uri);
设置圆角imageView.setRound(50).bind(uri);


