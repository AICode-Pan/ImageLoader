package load.custom.imageload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/7/30
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class HttpClient {
    /**
     * @return Bitmap 返回类型
     * @throws
     * @Title: getBitmapFormUrl
     * @说 明:从服务器获取Bitmap
     * @参 数: @param url
     * @参 数: @return
     */
    public static Bitmap getBitmapFormUrl(String url) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        try {
            // 创建一个URL对象
            URL mURL = new URL(url);
            // 调用URL的openConnection()方法,获取HttpURLConnection对象
            conn = (HttpURLConnection) mURL.openConnection();

            conn.setReadTimeout(5 * 1000);// 设置读取超时为5秒
            conn.setConnectTimeout(20 * 1000);// 设置连接网络超时为20秒

            int responseCode = conn.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
            if (responseCode == 200) {
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);

                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();// 关闭连接
            }
        }
        return bitmap;
    }
}
