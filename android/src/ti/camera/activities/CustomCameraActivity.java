package ti.camera.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import ti.camera.utils.CameraManager;
import ti.camera.utils.CameraPreview;
import ti.camera.utils.ImageAdapter;
import ti.camera.utils.ImagesTempInfo;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class CustomCameraActivity extends Activity implements OnClickListener {
	private Camera mCamera;
	private CameraPreview mCameraPreview;

	// For camera preview holder
	private FrameLayout frameLayout;
	private Button takePictureButton;
	private Button previewButton;
	private Button cancelButton;
	private final static String TAG = CustomCameraActivity.class
			.getSimpleName();
	public static final int TAKE_PICTURE_BUTTON = 0;
	private final int PREVIEW_BUTTON = 1;
	public static final int CANCEL_BUTTON = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		frameLayout = new FrameLayout(this);
		LayoutParams frameLayoutparams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
		frameLayout.setLayoutParams(frameLayoutparams);

		takePictureButton = new Button(this);
		takePictureButton.setId(TAKE_PICTURE_BUTTON);
		takePictureButton.setOnClickListener(this);
		takePictureButton.setText("Capture");

		cancelButton = new Button(this);
		cancelButton.setId(CANCEL_BUTTON);
		cancelButton.setOnClickListener(this);
		cancelButton.setText("Cancel");

		previewButton = new Button(this);
		previewButton.setId(PREVIEW_BUTTON);
		previewButton.setOnClickListener(this);
		previewButton.setText("Review");

		LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		buttonParams.weight = 1.0f;

		previewButton.setLayoutParams(buttonParams);
		takePictureButton.setLayoutParams(buttonParams);
		cancelButton.setLayoutParams(buttonParams);

		LinearLayout overlayLinearLayout = new LinearLayout(this);
		overlayLinearLayout.setOrientation(LinearLayout.VERTICAL);
		overlayLinearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		overlayLinearLayout.setBackgroundColor(Color.TRANSPARENT);
		LinearLayout padding = new LinearLayout(this);
		padding.setBackgroundColor(Color.TRANSPARENT);
		LinearLayout.LayoutParams paddingParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0);
		paddingParams.weight = 1.0f;
		padding.setLayoutParams(paddingParams);
		overlayLinearLayout.addView(padding);
		LinearLayout buttonsHolderLinearLayout = new LinearLayout(this);
		buttonsHolderLinearLayout.setPadding(getPixels(5), 0, getPixels(5), 0);
		buttonsHolderLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		buttonsHolderLinearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		buttonsHolderLinearLayout.addView(previewButton);
		buttonsHolderLinearLayout.addView(takePictureButton);
		buttonsHolderLinearLayout.addView(cancelButton);

		overlayLinearLayout.addView(buttonsHolderLinearLayout);

		setContentView(frameLayout);
		addContentView(overlayLinearLayout, frameLayoutparams);
	}

	AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
	    @Override
	    public void onAutoFocus(boolean success, Camera camera) {
	        camera.takePicture(null, null, mPicture);
	    }
	};
	
	// Callback when picture has been clicked.
	PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			// Compress and write to a file: Paneer
			File pictureFile = CameraManager.getOutputMediaFile();
			if (pictureFile == null) {
				return;
			}
			try {
				// Save picture at some path
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				ImagesTempInfo imagesTempInfo = new ImagesTempInfo();
				imagesTempInfo.setCode("");
				imagesTempInfo.setAdditionalInfo("");
				imagesTempInfo.setDescription(pictureFile.getAbsolutePath());
				imagesTempInfo.setImageName(pictureFile.getName());
				Log.d(TAG, "pictureFile.getName() : " + pictureFile.getName());
				ImageAdapter.putImagesTempInfo(imagesTempInfo);
				mCamera.startPreview();

				// Wait for 200 milliseconds before capture button enabled. It is
				// due to application getting crashed because the camera not
				// ready fully to take picture again.
				new CountDownTimer(200, 100) {

					@Override
					public void onTick(long millisUntilFinished) {
					}

					@Override
					public void onFinish() {
						cancel();
						previewButton.setClickable(true);
						takePictureButton.setEnabled(true);
					}
				}.start();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	public void takeFocusedPicture() {
	    mCamera.autoFocus(mAutoFocusCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// release the camera immediately on pause event for example when a call
		// comes or mobile goes to sleep.
		CameraManager.releaseCamera();
		Log.d(TAG, "Camera released on Activity paused");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume() : onResumed called.");
		mCamera = CameraManager.getCameraInstance();
		mCameraPreview = new CameraPreview(this, mCamera);
		frameLayout.addView(mCameraPreview);
		takePictureButton.setEnabled(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case TAKE_PICTURE_BUTTON:

			// Paneer add START
			Parameters mParameters = mCamera.getParameters();
			
			if (mParameters != null) {

				List<Size> sizes = mParameters.getSupportedPictureSizes();

				if (sizes != null) {

					Size optimalSize = getOptimalSize(sizes, 800, 600);

					if (optimalSize != null && !mParameters.getPictureSize().equals(optimalSize)) {
						mParameters.setPictureSize(optimalSize.width, optimalSize.height);
					}
				}

				mParameters.setJpegQuality(100);
				mCamera.setParameters(mParameters);
			}
			// Paneer add END
			try {
				mCamera.startPreview();
				
				//Hang on for a few ms
				new CountDownTimer(200, 100) {

					@Override
					public void onTick(long millisUntilFinished) {
					}

					@Override
					public void onFinish() {
						cancel();
						previewButton.setClickable(true);
						takePictureButton.setEnabled(true);
					}
				}.start();
				
				//tweak to take focused picture
				//takeFocusedPicture();
				
				mCamera.takePicture(null, null, mPicture);
				
			} catch (Exception e) {
				Log.d(TAG, "Message: " + e.getMessage() + " \nStack Trace: "
						+ getStackTrace(e));
			}
			v.setEnabled(false);
			previewButton.setClickable(false);
			break;

		case PREVIEW_BUTTON:
			CameraManager.setCustomCameraActivity(this);
			Intent intent = new Intent(this, PreviewActivity.class);
			startActivity(intent);
			break;

		case CANCEL_BUTTON:
			CameraManager.deleteImageDirectory(CameraManager
					.getImageDirecotry());
			finish();
			CameraManager.clearReferences();
			break;
		}

	}

	private static String getStackTrace(Throwable aThrowable) {
		try {
			Writer result = new StringWriter();
			PrintWriter printWriter = new PrintWriter(result);
			aThrowable.printStackTrace(printWriter);
			return result.toString();
		} catch (Exception e) {
			return "";
		}
	}

	// Paneer added START
	private Size getOptimalSize(List<Size> sizes, int w, int h) {

		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;

	}

	// Paneer added END

	// Convert dip to px units.
	private int getPixels(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dip, getResources().getDisplayMetrics());
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG,
				" onBackPressed() : No action defined on back button pressed.");
	}

}
