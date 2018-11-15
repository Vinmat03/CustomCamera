package ti.camera.activities;

import ti.camera.utils.CameraManager;
import ti.camera.utils.ImageAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class PreviewActivity extends Activity implements OnClickListener
{
    private LinearLayout wrapperLinearLayout;
    private final String TAG = PreviewActivity.class.getSimpleName();
    private static final int SAVE_BUTTON = 1;
    public static final int THUMBNAIL_WIDTH = 70;
    public static final int MARGIN = 10;
    private static Resources res;
    private ImageAdapter imageAdapter;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	res = getResources();

	wrapperLinearLayout = new LinearLayout(this);
	wrapperLinearLayout.setBackgroundColor(Color.BLACK);
	wrapperLinearLayout.setOrientation(LinearLayout.VERTICAL);
	wrapperLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

	gridView = new GridView(this);
	gridView.setBackgroundColor(Color.BLACK);
	LayoutParams gridLayoutParameters = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
	gridView.setLayoutParams(gridLayoutParameters);
	gridView.setNumColumns(GridView.AUTO_FIT);
	gridView.setPadding(getPixels(MARGIN), getPixels(MARGIN), getPixels(MARGIN), getPixels(MARGIN));
	gridView.setColumnWidth(getPixels(THUMBNAIL_WIDTH));
	gridView.setHorizontalSpacing(getPixels(MARGIN));
	gridView.setVerticalSpacing(getPixels(MARGIN));
	gridView.setGravity(Gravity.CENTER);
	gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
	wrapperLinearLayout.addView(gridView);

	setContentView(wrapperLinearLayout);

	Button takePictureButton = new Button(this);
	takePictureButton.setId(CustomCameraActivity.TAKE_PICTURE_BUTTON);
	takePictureButton.setOnClickListener(this);
	takePictureButton.setText("Take Picture");

	Button saveButton = new Button(this);
	saveButton.setOnClickListener(this);
	saveButton.setId(SAVE_BUTTON);
	saveButton.setText("Save");
	
	
	Button cancelButton = new Button(this);
	cancelButton.setId(CustomCameraActivity.CANCEL_BUTTON);
	cancelButton.setOnClickListener(this);
	cancelButton.setText("Cancel");
	

	LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
	buttonParams.weight = 1.0f;

	takePictureButton.setLayoutParams(buttonParams);
	saveButton.setLayoutParams(buttonParams);
	cancelButton.setLayoutParams(buttonParams);

	
	

	LinearLayout buttonsHolderLinearLayout = new LinearLayout(this);
	buttonsHolderLinearLayout.setBackgroundColor(Color.BLACK);
	buttonsHolderLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
	buttonsHolderLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	buttonsHolderLinearLayout.addView(takePictureButton);
	buttonsHolderLinearLayout.addView(saveButton);
	buttonsHolderLinearLayout.addView(cancelButton);

	wrapperLinearLayout.addView(buttonsHolderLinearLayout);

	// Instance of ImageAdapter Class
	imageAdapter = new ImageAdapter(this);
	gridView.setAdapter(imageAdapter);

	/**
	 * On Click event for Single Gridview Item
	 * */
	gridView.setOnItemClickListener(new OnItemClickListener()
	{
	    @Override
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {

		Intent i = new Intent(PreviewActivity.this, FullImageActivity.class);

		String tag = (String) v.getTag();
		String name;
		try{
		    name = tag.split(":")[0];
		}
		catch(NullPointerException e)
		{
		    Log.d(TAG, "This image could not be downloaded properly.");
		    return;
		}
		String code;
		if (tag.charAt(tag.length() - 1) == ':')
		{
		    code = "";
		}
		else
		{
		    code = tag.split(":")[1];
		}

		String path = CameraManager.getImageDirecotry() + "/" + name;
		i.putExtra("path", path);
		i.putExtra("imageName", name);
		i.putExtra("code", code);
		i.putExtra("position", position);
		startActivityForResult(i, 2);
	    }
	});

    }

    // Call Back method to get the Message form other FullImageActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
	super.onActivityResult(requestCode, resultCode, data);

	// check if the request code is same as what is passed here it is 2
	if (requestCode == 2)
	{
	    if (null != data)
	    {
		// fetch the message
		boolean isImageDeleted = data.getBooleanExtra("isImageDeleted", true);
		if (isImageDeleted)
		{
		        imageAdapter = new ImageAdapter(this);
			gridView.setAdapter(imageAdapter);
		}

	    }
	}
    }

    @Override
    public void onBackPressed()
    {
	// nothing will happen.user can not go back on click back button.Only take picture button will tack back user to camera screen from where the user can exit the module on back press.
	Log.d(TAG, "onBackPressed() : Back button pressed. No action defined.");
    }

    // Convert dip to px units 
    public static int getPixels(int dip)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
    }

    @Override
    public void onClick(View v)
    {
	switch (v.getId())
	{
	case CustomCameraActivity.TAKE_PICTURE_BUTTON:
	    if (CameraManager.getCustomCameraActivity() == null)
	    {
		v.setEnabled(false);
		Intent intent = new Intent(this, CustomCameraActivity.class);
		startActivity(intent);
	    }
	    imageAdapter.clearCache();
	    finish();
	    Log.d(TAG, "onClick() : Activity finished.");
	    break;

	case SAVE_BUTTON:
	    Log.d(TAG, "onClick() : Save button clicked.");
	    imageAdapter.clearCache();
	    finish();
	    if (CameraManager.getCustomCameraActivity() != null)
	    {
		CameraManager.finishCustomCameraActivity();
	    }
	    CameraManager.fireSaveEventListener();
	    CameraManager.clearReferences();
	    break;
	case CustomCameraActivity.CANCEL_BUTTON:
	    Log.d(TAG, "onClick() : Cancel button clicked.");
	    imageAdapter.clearCache();
	    CameraManager.deleteImageDirectory(CameraManager.getImageDirecotry());
	    finish();
	    if (CameraManager.getCustomCameraActivity() != null)
	    {
		CameraManager.finishCustomCameraActivity();
	    }
	    CameraManager.clearReferences();
	    break;
	}
    }

    @Override
    protected void onResume()
    {
	super.onResume();
    }

}
