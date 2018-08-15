package load.custom.imageload;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/8/15
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class MainActivity extends Activity {
    private ImageView imageView;
    private ImageLoader imageLoader;
    private String imageUrl = "https://www.baidu.com/img/superlogo_c4d7df0a003d3db9b65e9ef0fe6da1ec.png?where=super";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageview);
        imageLoader = ImageLoader.getImageDownLoader(this);
    }

    public void click(View v) {
        imageLoader.downLoader(imageUrl, new ImageLoader.ImageLoaderlistener() {
            @Override
            public void onImageLoader(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

}
