package ti.camera.utils;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ti.camera.activities.PreviewActivity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
    private Context mContext;
    private static ArrayList<ImagesTempInfo> imagesInfosArray;
    private static final String TAG = ImageAdapter.class.getSimpleName();
    public ImageLoader imageLoader;

    // Constructor
    public ImageAdapter(Context c)
    {
	mContext = c;
	imageLoader = new ImageLoader(mContext.getApplicationContext());
    }

    @Override
    public int getCount()
    {
	Log.d(TAG, " imagesInfosArray.size() :" + imagesInfosArray.size());
	return imagesInfosArray.size();
    }

    @Override
    public Object getItem(int position)
    {
	Log.d(TAG, " imagesInfosArray.get(position) :" + imagesInfosArray.get(position));
	return imagesInfosArray.get(position);
    }

    @Override
    public long getItemId(int position)
    {
	return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
	ImageView imageView;
	if (convertView == null)
	{
	    imageView = new ImageView(mContext);
	    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	    imageView.setLayoutParams(new GridView.LayoutParams(PreviewActivity.getPixels(PreviewActivity.THUMBNAIL_WIDTH), PreviewActivity.getPixels(PreviewActivity.THUMBNAIL_WIDTH)));
	}
	else
	{
	    imageView = (ImageView) convertView;
	}

	Log.d(TAG, " map.get(position).getDescription() :" + imagesInfosArray.get(position).getDescription() + "  imagesInfosArray.get(position).getCode() = "
		+ imagesInfosArray.get(position).getCode());
	ImagesTempInfo info = imagesInfosArray.get(position);
	imageLoader.DisplayImage(info, imageView);

	return imageView;

    }

    /**
     * ImagesTempInfo objects equal to number of images in to be downloaded from
     * the server are created and saved here first time the application enters
     * in module.
     * 
     * @param infoMap
     */
    public static void setImagesTempInfoMap(ArrayList<ImagesTempInfo> infoMap)
    {

	imagesInfosArray = infoMap;
    }
    
    public static ArrayList<ImagesTempInfo> getImagesTempInfoArray()
    {
	return imagesInfosArray;
    }

    /**
     * Set the image in the array immediately after the image has been snapped
     * by camera.
     * 
     * @param infoMap
     *            :New ImagesTempInfo containing the information of the image.
     */
    public static void putImagesTempInfo(ImagesTempInfo value)
    {
	imagesInfosArray.add(value);
    }

    /**
     * Code for deleting a particular image.
     * 
     * @param imageName
     *            :Name of the image
     * @param path
     *            :Absolute path of the image.
     * @return deleted image code.
     */
    public static String deleteImage(String imageName, String path)
    {
	for (int i = 0; i < imagesInfosArray.size(); i++)
	{
	    ImagesTempInfo info = imagesInfosArray.get(i);
	    if (info.getImageName().equals(imageName))
	    {
		try
		{
		    File file = new File(path);

		    if (file.delete())
		    {
			Log.d(TAG, "deleteImage() : " + path + " deleted at local index : " + i);
			imagesInfosArray.remove(i);
		    }
		    else
		    {
			Log.d(TAG, "deleteImage() : " + path + " can not be deleted at index : " + i);
		    }

		    if (info.getCode().length() == 0)
		    {
			return "";
		    }
		    else
		    {
			// Return the deleted image code.
			return info.getCode();
		    }
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}

	    }
	}

	return "";

    }

    /**
     * This method wraps the images information into JSONOArray in the form of
     * JSONObject which will be sent back to titanium when save button on the
     * preview screen will be clicked.
     */
    public static JSONObject getImagesInfo()
    {
	JSONObject imagesInfo = null;
	JSONArray jsonArray = new JSONArray();
	for (int i = 0; i < imagesInfosArray.size(); ++i)
	{
	    ImagesTempInfo imagesTempInfo = imagesInfosArray.get(i);
	    JSONObject jsonObject = new JSONObject();
	    try
	    {
		jsonObject.put("additionalInfo", imagesTempInfo.getAdditionalInfo());
		jsonObject.put("code", imagesTempInfo.getCode());
		jsonObject.put("description", CameraManager.getImageDirecotry() + "/" + imagesTempInfo.getImageName());
		jsonArray.put(jsonObject);
	    }
	    catch (JSONException e)
	    {
		e.printStackTrace();
	    }

	}
	imagesInfo = new JSONObject();
	try
	{
	    imagesInfo.put("images", jsonArray);
	}
	catch (JSONException e)
	{
	    e.printStackTrace();
	}
	Log.d(TAG, " return imagesInfo :" + imagesInfo);
	return imagesInfo;
    }
    
    
    public void clearCache()
    {
	imageLoader.clearCache();
    }

}
