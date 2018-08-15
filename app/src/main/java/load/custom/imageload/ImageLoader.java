package load.custom.imageload;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/8/2
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class ImageLoader {
    private static final int LOAD_SUCCESS = 1;
    /** 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存 */
    private LruCache<String, Bitmap> lruCache;
    /** 文件操作工具类 */
    private FileUtils utils;

    private ThreadPoolExecutor executor;

    private static ImageLoader imageDownLoader;

    private MyHandler handler;

    public static ImageLoader getImageDownLoader(Context context) {
        if (imageDownLoader == null) {
            imageDownLoader = new ImageLoader(context);
        }
        return imageDownLoader;
    }

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

    /**
     *
     * @Title: downLoader
     * @说 明: 加载图片
     * @参 数: @param url
     * @参 数: @param loaderlistener
     * @参 数: @return
     * @return Bitmap 返回类型
     * @throws
     */
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

    private static class MyHandler extends Handler {
        private ImageLoaderlistener loaderlistener;
        public void setListener(ImageLoaderlistener loaderlistener) {
            this.loaderlistener = loaderlistener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (loaderlistener != null) {
                loaderlistener.onImageLoader((Bitmap) msg.obj);
            }
        }
    }

    /**
     *
     * @Title: showCacheBitmap
     * @说 明: 获取bitmap对象 : 内存中没有就去sd卡中去找
     * @参 数: @param url 图片地址
     * @参 数: @return
     * @return Bitmap 返回类型 图片
     * @throws
     */
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

    /**
     *
     * @Title: getMemoryBitmap
     * @说 明:获取内存中的图片
     * @参 数: @param url
     * @参 数: @return
     * @return Bitmap 返回类型
     * @throws
     */
    private Bitmap getMemoryBitmap(String url) {
        return lruCache.get(url);
    }

    public interface ImageLoaderlistener {
        public void onImageLoader(Bitmap bitmap);
    }

    /**
     *
     * @Title: cancelTask
     * @说 明:停止所有下载线程
     * @参 数:
     * @return void 返回类型
     * @throws
     */
    public void cancelTask() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

}
