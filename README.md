# lib_download
这是一个处理异步下载的库，包含图片下载文件下载和ormlite－ormgo(马天宇的开源)，特点如下：

1.全局公用一个线程池

2.非ImageView的view也支持哦

3.初次从网络下载的图第一次显示为淡入效果，对于1m以上的大图在使用本地缓存的情况下边下载边显示

4.支持预加载：```javaBmLoader.preLoad(uri);```

5.优化了listview等快速滑动时的图片加载

6.圆角图采用了性能最优的方案

7.如果view使用或者继承```javadownload.imageLoader.view.GifMovieView```这个类的话支持gif图，否则只能用回调自己自定义view实现。

8.设置自定义显示方法如：
```javaBmLoader.loadImage(
	"http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg", mTv, 30, 30, 
	new CustomDisplayMethod() {
	
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void display(BitmapDrawable bitmap, Movie movie) {
                mTv.setCompoundDrawablesRelativeWithIntrinsicBounds(bitmap,null,null,null);
            }
        });
        ```
  这样就可以实现各种功能如给textviw设置上下左右的图，给子view设置网络图片，给remoteview设置网络图片等等。
9.如果使用类download.imageLoader.view.GifMovieView，调用方法更简单了：```javaview.bind(uri)```，圆形图```javaview.setCircle().bind(uri)```，矩形图```javaview.setRectangle().bind(uri)```，圆角图```javaview.setRound(50).bind(uri)```是否极致方便？总大小仅200多k

10.支持本地和网络图片，径格式示例为：
		```xml
		"http://img.blog.csdn.net/20160114230048304",
    		"assets://anim.gif",
                "drawable://"+R.drawable.anim,
                "file:///mnt/sdcard/paint.png",
                ```
		

对于特殊的需求可以使用回调自己处理：
	```java 
	BmLoader.load(uri, imageView, new BackListener() {
                @Override
                public void onProcess(int percent) {
                    
                }

                @Override
                public void onSuccess(BitmapDrawable bitmap, Movie movie) {

                }

                @Override
                public void onFailed() {

                }
            });
            ```
            或者使用接口适配器：
            ```java
            BmLoader.load(uri, imageView, new BackListenerAdapter() {
                @Override
                public void onSuccess(BitmapDrawable bitmap, Movie movie) {
                    super.onSuccess(bitmap, movie);
                }
            });
            ```
                
11.可设置圆形imageView.setCircle().bind(uri);
	设置矩形imageView.setRectangle().bind(uri);
	设置圆角imageView.setRound(50).bind(uri);
	可设置包边imageView.setCircle().setBorder(Color.BLACK, 10f).bind(uri);

12.断点下载 ApkLoader.getInstance(this).downApk("");




