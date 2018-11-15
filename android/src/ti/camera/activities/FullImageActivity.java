package ti.camera.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import ti.camera.utils.CameraManager;
import ti.camera.utils.FullImageLoader;
import ti.camera.utils.ImageAdapter;
import ti.camera.utils.ImagesTempInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;


public class FullImageActivity extends FragmentActivity implements OnClickListener
{

    private static final int DELETE_PICTURE = 0;
    private static final int SET_DEFAULT = 1;
    private static final int PREVIOUS = 2;
    private static final int NEXT = 3;
    private LinearLayout lLayout;
    private ImageView imageView;
    private Button deletePictureButton;
    //private Button setDefaultButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private String path;
    private String imageName;
    private String code;
    private String TAG = FullImageActivity.class.getSimpleName();
    private int screenWidth;
    private int screenHeight;
    private static float requiredImageWidth;
    private static float requiredImageHeight;
    private android.support.v4.view.ViewPager viewPager;
    public FullImageLoader imageLoader;
    private ArrayList<ImagesTempInfo> imagesInfosArray;
    private int position; 

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	imageLoader = new FullImageLoader(getApplicationContext());
	imagesInfosArray = ImageAdapter.getImagesTempInfoArray();

	LayoutParams paramFull = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	lLayout = new LinearLayout(this);
	lLayout.setOrientation(LinearLayout.VERTICAL);
	lLayout.setLayoutParams(paramFull);

	viewPager = new ViewPager(this);
	LayoutParams gravityParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
	viewPager.setLayoutParams(gravityParams);
	viewPager.setOnPageChangeListener(new PageListener());
	lLayout.addView(viewPager);


	deletePictureButton = new Button(this);
	deletePictureButton.setId(DELETE_PICTURE);
	deletePictureButton.setOnClickListener(this);

	//setDefaultButton = new Button(this);
	//setDefaultButton.setId(SET_DEFAULT);
	//setDefaultButton.setOnClickListener(this);
	LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
	buttonParams.weight = 1.0f;

	deletePictureButton.setLayoutParams(buttonParams);
	//setDefaultButton.setLayoutParams(buttonParams);

	deletePictureButton.setText("Delete");
	//setDefaultButton.setText("Set Default");
	
	
	previousButton = new ImageButton(this);
	previousButton.setId(PREVIOUS);
	previousButton.setLayoutParams(buttonParams);
	previousButton.setImageBitmap(getBitmapFromAsset("move_left.png"));
	previousButton.setOnClickListener(this);
	
	nextButton = new ImageButton(this);
	nextButton.setId(NEXT);
	nextButton.setLayoutParams(buttonParams);
	nextButton.setImageBitmap(getBitmapFromAsset("move_right.png"));
	nextButton.setOnClickListener(this);
	
	
	

	LinearLayout buttonsHolderLinearLayout = new LinearLayout(this);
	buttonsHolderLinearLayout.setBackgroundColor(Color.BLACK);
	buttonsHolderLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
	buttonsHolderLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	buttonsHolderLinearLayout.addView(deletePictureButton);
	buttonsHolderLinearLayout.addView(previousButton);
	buttonsHolderLinearLayout.addView(nextButton);
	//buttonsHolderLinearLayout.addView(setDefaultButton);

	lLayout.addView(buttonsHolderLinearLayout);

	setContentView(lLayout);
	
	
	// get intent data
	Intent i = getIntent();

	// Selected image id
	position = i.getExtras().getInt("position");
	path = i.getExtras().getString("path");
	imageName = i.getExtras().getString("imageName");
	code = i.getExtras().getString("code");
	//if (code.length() == 0)
	//{
	//    setDefaultButton.setEnabled(false);
	//}
	if(imagesInfosArray.size() > 1 && position == 0)
	{
	    previousButton.setEnabled(false);
	}
	if(imagesInfosArray.size() == 1)
	{
	    previousButton.setEnabled(false);
	    nextButton.setEnabled(false);
	}

	 Display display = getWindowManager().getDefaultDisplay();
	 screenWidth = display.getWidth();
	 screenHeight = display.getHeight();
	 requiredImageWidth = screenWidth;
	
	FullImageAdapter adapter = new FullImageAdapter(this);
	viewPager.setAdapter(adapter);
	viewPager.setCurrentItem(position);

    }

    class FullImageAdapter extends PagerAdapter
    {
	Context context;

	FullImageAdapter(Context context)
	{
	    this.context = context;
	}

	@Override
	public int getCount()
	{
	    return imagesInfosArray.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
	    return view == ((ImageView) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{

	    ImageView imageView = new ImageView(context);
	    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	    ((ViewPager) container).addView(imageView, 0);
	    imageLoader.DisplayImage(imagesInfosArray.get(position), imageView);
	    return imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
	    ((ViewPager) container).removeView((ImageView) object);
	}
    }

    @Override
    public void onClick(View v)
    {

	switch (v.getId())
	{
	case PREVIOUS:
	     showPrevious();
	     break;
	case NEXT:
	     showNext();
	     break;
	case DELETE_PICTURE:

	    AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(this);
	    confirmationDialog.setMessage("Are you sure?").setTitle("Confirm Delete").setPositiveButton("OK", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
	    break;

	case SET_DEFAULT:
	    // index will be sent to .js caller
	    code = imagesInfosArray.get(position).getCode();
	    Log.d(TAG, "setDefault called:code = "+code);
	    int index = CameraManager.getImageIndex(code);
	    JSONObject imageInfo = new JSONObject();
	    try
	    {
		imageInfo.put("Index", index);
	    }
	    catch (JSONException e)
	    {
		e.printStackTrace();
	    }
	    
	    //Fire the event
	    CameraManager.fireSetDefaultEventListener(imageInfo);

	    //clear the cache
            imageLoader.clearCache();
	    finish();
	    break;
	}

    }
    
    
    private class PageListener extends SimpleOnPageChangeListener
    {
	@Override
	public void onPageSelected(int pagePosition)
	{
	    super.onPageSelected(pagePosition);
	    position = pagePosition;
	    //if(imagesInfosArray.get(position).getDescription().contains("http"))
	    //{
		// setDefaultButton.setEnabled(true);
	   // }
	    //else
	    //{
		//setDefaultButton.setEnabled(false);
	    //}
	    
	    if(position == 0)
	    {
	    	previousButton.setEnabled(false);
	    }
	    else
	    {
	    	previousButton.setEnabled(true);
	    }
	   
	    if(position == imagesInfosArray.size()-1)
	    {
		nextButton.setEnabled(false);
	    }
	    else
	    {
		nextButton.setEnabled(true);
	    }
	}
    }
    
    
    private void showNext()
    {
	if(position < imagesInfosArray.size()-1)
	{
	    
	    position++;
	    viewPager.setCurrentItem(position,true);
	    if(position == imagesInfosArray.size()-1)
	    {
		nextButton.setEnabled(false);
	    }
	    else
	    {
		nextButton.setEnabled(true);
	    }
	}
    }
    
    private void showPrevious()
    {
	if(position > 0)
	{
	    
	    position--;
	    viewPager.setCurrentItem(position,true);
		
	}
    }
    
    private Bitmap getBitmapFromAsset(String imageName)
    {
	Bitmap bitMap = null;
	try
	    {
		InputStream is = this.getAssets().open(imageName);
		bitMap = BitmapFactory.decodeStream(is);
	    }
	    catch (IOException e)
	    {
		Log.d(TAG, imageName+" not found :" + e.getMessage());
	    }
	return bitMap;
    }
    
    // Confirmation dialog listener
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
    {
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
	    switch (which)
	    {
	    case DialogInterface.BUTTON_POSITIVE:
		// OK button clicked

		// Delete the image from the directory.
		ImagesTempInfo  info = imagesInfosArray.get(position);
		imageName = info.getImageName();
		path = CameraManager.getImageDirecotry() + "/" + info.getImageName();
		String code = ImageAdapter.deleteImage(imageName, path);
		Log.d(TAG, path + " requested for deletion.");
		if (code.length() > 0)
		{
		    // get the index of the image where this image is found in
		    // the array came from the titanium .js file.
		    int index = CameraManager.getImageIndex(code);

		    // Create a json object which will be returned to titanium
		    // .js file to deleteImage callback function.
		    JSONObject imageInfo = new JSONObject();
		    try
		    {
			imageInfo.put("Index", index);
		    }
		    catch (JSONException e)
		    {
			e.printStackTrace();
		    }

		    CameraManager.fireDeleteImageEventListener(imageInfo);
		}
		else
		{
		    // code length 0 means picture has been clicked from camera
		}

		Intent intentMessage = new Intent();

		// put the message in Intent
		intentMessage.putExtra("isImageDeleted", true);
		// Set The Result in Intent
		setResult(2, intentMessage);
		
		//clear the cache
		 imageLoader.clearCache();
		
		// finish The activity
		finish();
		break;

	    case DialogInterface.BUTTON_NEGATIVE:
		// Cancel button clicked
		dialog.dismiss();
		break;
	    }
	}
    };

    public static Bitmap decodeSampledBitmapFromResource(String path)
    {
	Bitmap bitmap = null;
	// First decode with inJustDecodeBounds=true to check dimensions
	final BitmapFactory.Options options = new BitmapFactory.Options();
	options.inJustDecodeBounds = true;

	try
	{
	    BitmapFactory.decodeStream(new FileInputStream(new File(path)), null, options);
	}
	catch (FileNotFoundException e)
	{
	    e.printStackTrace();
	}

	// Calculate inSampleSize
	options.inSampleSize = calculateInSampleSize(options);

	// Decode bitmap with inSampleSize set
	options.inJustDecodeBounds = false;
	try
	{
	    bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(path)), null, options);
	}
	catch (FileNotFoundException e)
	{
	    e.printStackTrace();
	}
	return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options)
    {
	// Raw height and width of image
	final int height = options.outHeight;
	final int width = options.outWidth;

	float original_image_ratio = (float) width / (float) height;

	requiredImageHeight = requiredImageWidth / original_image_ratio;

	int inSampleSize = 1;

	if (height > requiredImageHeight || width > requiredImageWidth)
	{

	    // Calculate ratios of height and width to requested height and
	    // width
	    final int heightRatio = Math.round((float) height / (float) requiredImageHeight);
	    final int widthRatio = Math.round((float) width / (float) requiredImageWidth);

	    // Choose the smallest ratio as inSampleSize value, this will
	    // guarantee
	    // a final image with both dimensions larger than or equal to the
	    // requested height and width.
	    inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	}

	return inSampleSize;
    }

}
