## 使用图片缓存的原因
* 提高用户体验：如果每次启动都从网络下载图片，势必会加载很慢，图片无法显示，或需要很久才能完全显示，用户体验及其不好
* 节约流量：如果每次加载页面，甚至只是滑动控件浏览就会下载的话，会消耗很多流量，占用网络资源的同时，也会因为应用耗流量而用户数量级受到影响

## 什么是图片三级缓存
* 内存缓存：优先加载，速度最快
* 本地缓存：次优先加载，速度较快
* 网络缓存：最后加载，速度较慢

### 内存缓存
获取系统分配给应用的最大内存。取一部分用来进行图片缓存
```java
    private ImageLoader(Context context) {
        // 开启线程池 最小线程数
        executor = new ThreadPoolExecutor(1, 5, 2, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        // 获取系统分配给应用程序的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int maxSize = maxMemory / 8;
        lruCache = new LruCache<String, Bitmap>(maxSize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                // 测量Bitmap的大小 默认返回图片数量
                return value.getRowBytes() * value.getHeight();
            }

        };

        utils = new FileUtils(context);
        handler = new MyHandler();
    }
```

### 本地缓存
本地在应用的目录下生成一个img文件夹，用来存放本地缓存的图片
```java
    public void savaBitmap(String url, Bitmap bitmap) throws IOException {
        if (bitmap == null) {
            return;
        }
        String path = getStorageDirectory();
        File folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        File file = new File(path + File.separator + getFileName(url));
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
    }
```
获取本地缓存的图片
<code>
    public Bitmap getBitmap(String url) {
        return BitmapFactory.decodeFile(getStorageDirectory() + File.separator + getFileName(url));
    }
<code/>

### 网络缓存
从内存缓存和本地缓存中获取图片
```java
    public Bitmap showCacheBitmap(String url) {
        Bitmap bitmap = getMemoryBitmap(url);
        if (bitmap != null) {
            return bitmap;
        } else if (utils.isFileExists(url) && utils.getFileSize(url) > 0) {
            bitmap = utils.getBitmap(url);
            lruCache.put(url, bitmap);
            return bitmap;
        }
        return null;
    }
```
当内存缓存和网络缓存中不存在，就去网络加载
```
    public void downLoader(final String url, final ImageLoaderlistener loaderlistener) {
        if (url != null) {
            final Bitmap bitmap = showCacheBitmap(url);//这里就是从缓存中去找图片
            if (bitmap != null) {                     //如果缓存中返回的图片为空的，则开启线程进行下载
                loaderlistener.onImageLoader(bitmap);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = HttpClient.getBitmapFormUrl(url);
                        if (bitmap != null) {                      //下载完毕之后将图片保存到缓存和本地，然后通知ImageView更新UI
                            handler.setListener(loaderlistener);
                            handler.sendMessage(handler.obtainMessage(LOAD_SUCCESS, bitmap));
                            lruCache.put(url, bitmap);
                            try {
                                utils.savaBitmap(url, bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }
```
