# lib_download
这是一个处理异步下载的库，包含图片下载文件下载和ormlite－ormgo(马天宇的开源)，特点如下：

全局公用一个线程池

非ImageView的view也支持哦

初次从网络下载的图第一次显示为淡入效果，对于1m以上的大图在使用本地缓存的情况下边下载边显示

支持预加载：```javaBmLoader.preLoad(uri);```

支持本地和网络图片，径格式示例为：
		```xml
		"http://img.blog.csdn.net/20160114230048304",
    		"assets://anim.gif",
                "drawable://"+R.drawable.anim,
                "file:///mnt/sdcard/paint.png",
                ```
优化了listview等快速滑动时的图片加载

圆角图采用了性能最优的方案

如果view使用或者继承download.imageLoader.view.GifMovieView这个类的话支持gif图，否则只能用回调自己自定义view实现。

设置自定义显示方法这样就可以实现各种功能如给textviw设置上下左右的图，给子view设置网络图片，给remoteview设置网络图片等等。
```java
	BmLoader.loadImage(
	"http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg", mTv, 30, 30, 
	new CustomDisplayMethod() {
	
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void display(BitmapDrawable bitmap, Movie movie) {
                mTv.setCompoundDrawablesRelativeWithIntrinsicBounds(bitmap,null,null,null);
            }
        });
        ```
如果使用类download.imageLoader.view.GifMovieView，调用方法更简单了：
```java
view.bind(uri)
view.setCircle().bind(uri)
view.setRectangle().bind(uri)
view.setRound(50).bind(uri)
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
            
            BmLoader.load(uri, imageView, new BackListenerAdapter() {
                @Override
                public void onSuccess(BitmapDrawable bitmap, Movie movie) {
                    super.onSuccess(bitmap, movie);
                }
            });
            ```
                
可设置圆形
```java
imageView.setCircle().bind(uri);
```
设置矩形
	```java
	imageView.setRectangle().bind(uri);
	```
设置圆角
	```java
	imageView.setRound(50).bind(uri);
	```
可设置包边
	```java
	imageView.setCircle().setBorder(Color.BLACK, 10f).bind(uri);
	```

断点下载 
```java
ApkLoader.getInstance(this).downApk("");
```




