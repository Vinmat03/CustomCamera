package ti.camera.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ti.camera.activities.FullImageActivity;
import ti.camera.activities.PreviewActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

public class FullImageLoader
{

    private static final String TAG = FullImageLoader.class.getSimpleName();
    MemoryCache memoryCache = new MemoryCache();
    private Context mContext;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    // handler to display images in UI thread
    Handler handler = new Handler();
    private Bitmap defaultBitmap;

    public FullImageLoader(Context context)
    {
	mContext = context;
	executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(ImagesTempInfo info, ImageView imageView)
    {
	imageViews.put(imageView, info.getDescription());
	Bitmap bitmap = memoryCache.get(info.getDescription());
	if (bitmap != null)
	{
	    imageView.setImageBitmap(bitmap);
	    imageView.setTag(info.getImageName() + ":" + info.getCode());
	    Log.d(TAG, "photoToLoad.imageView.getTag() = " + imageView.getTag());
	}
	else
	{
	    queuePhoto(info, imageView);
	    setDefaultImage(imageView);

	}
    }

    private void queuePhoto(ImagesTempInfo info, ImageView imageView)
    {
	PhotoToLoad p = new PhotoToLoad(info, imageView);
	executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url, String code)
    {
	File dir = CameraManager.getImageDirecotry();

	String extention = MimeTypeMap.getFileExtensionFromUrl(url);
	File picturePath = null;
	Log.d("DisplayImage", "Path=:" + url);
	if (url.contains("http"))
	{
	    picturePath = new File(dir, code + "." + extention);
	}
	else
	{
	    picturePath = new File(url);
	}

	// from SD cache
	Bitmap b = FullImageActivity.decodeSampledBitmapFromResource(picturePath.getAbsolutePath());
	if (b != null)
	{
	    return b;
	}

	// from web
	try
	{
	    Bitmap bitmap = null;
	    URL imageUrl = new URL(url);
	    HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
	    conn.setConnectTimeout(30000);
	    conn.setReadTimeout(30000);
	    conn.setInstanceFollowRedirects(true);
	    InputStream is = conn.getInputStream();
	    OutputStream os = new FileOutputStream(picturePath);
	    Utils.CopyStream(is, os);
	    os.close();
	    conn.disconnect();
	    bitmap = FullImageActivity.decodeSampledBitmapFromResource(picturePath.getAbsolutePath());
	    return bitmap;
	}
	catch (Throwable ex)
	{
	    ex.printStackTrace();
	    int size = PreviewActivity.getPixels(PreviewActivity.THUMBNAIL_WIDTH);
	    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
	    FileOutputStream fOut = null;
	    try
	    {
		fOut = new FileOutputStream(picturePath);
		bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
		fOut.flush();
		fOut.close();
		bitmap = FullImageActivity.decodeSampledBitmapFromResource(picturePath.getAbsolutePath());

		return bitmap;
	    }
	    catch (FileNotFoundException e)
	    {
		e.printStackTrace();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }

	    if (ex instanceof OutOfMemoryError)
	    {
		memoryCache.clear();
	    }
	    return null;
	}
    }

    // Task for the queue
    private class PhotoToLoad
    {
	public ImageView imageView;
	ImagesTempInfo imageInfo;

	public PhotoToLoad(ImagesTempInfo info, ImageView i)
	{

	    imageView = i;
	    imageInfo = info;
	}
    }

    class PhotosLoader implements Runnable
    {
	PhotoToLoad photoToLoad;

	PhotosLoader(PhotoToLoad photoToLoad)
	{
	    this.photoToLoad = photoToLoad;
	}

	@Override
	public void run()
	{
	    try
	    {
		if (imageViewReused(photoToLoad))
		{
		    return;
		}
		Bitmap bmp = getBitmap(photoToLoad.imageInfo.getDescription(), photoToLoad.imageInfo.getCode());
		memoryCache.put(photoToLoad.imageInfo.getDescription(), bmp);
		if (imageViewReused(photoToLoad))
		{
		    return;
		}
		BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
		handler.post(bd);
	    }
	    catch (Throwable th)
	    {
		th.printStackTrace();
	    }
	}
    }

    boolean imageViewReused(PhotoToLoad photoToLoad)
    {
	String tag = imageViews.get(photoToLoad.imageView);
	if (tag == null || !tag.equals(photoToLoad.imageInfo.getDescription()))
	{
	    return true;
	}
	return false;
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
	Bitmap bitmap;
	PhotoToLoad photoToLoad;

	public BitmapDisplayer(Bitmap b, PhotoToLoad p)
	{
	    bitmap = b;
	    photoToLoad = p;
	}

	public void run()
	{
	    if (imageViewReused(photoToLoad))
	    {

		return;
	    }
	    if (bitmap != null)
	    {
		photoToLoad.imageView.setTag(photoToLoad.imageInfo.getImageName() + ":" + photoToLoad.imageInfo.getCode());
		Log.d(TAG, "photoToLoad.imageView.getTag() = " + photoToLoad.imageView.getTag());
		photoToLoad.imageView.setImageBitmap(bitmap);
	    }
	    else
	    {
		setDefaultImage(photoToLoad.imageView);
	    }
	}
    }

    private void setDefaultImage(ImageView imgView)
    {
	if (defaultBitmap != null)
	{
	    imgView.setImageBitmap(defaultBitmap);
	}
	else
	{
	    try
	    {
		InputStream is = mContext.getAssets().open("DefaultImage.jpeg");
		Log.d(TAG, "ImageResourceId is.available():" + is.available());
		defaultBitmap = BitmapFactory.decodeStream(is);
		imgView.setImageBitmap(defaultBitmap);
	    }
	    catch (IOException e)
	    {
		Log.d(TAG, "Default image not found :" + e.getMessage());
	    }
	}
    }
     public void clearCache()
     {
     memoryCache.clear();
     //fileCache.clear();
     }

}
