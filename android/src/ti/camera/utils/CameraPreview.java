package ti.camera.utils;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	private final String TAG = CameraPreview.class.getSimpleName();
	private Context mContext;

	// Constructor that obtains context and camera
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mContext = context;
		mCamera = camera;
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		
		if(Build.VERSION.SDK_INT < 11)
		// deprecated setting, but required on Android versions prior to 3.0
		{
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.d(TAG, "surfaceCreated() : Method called ");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		Log.d(TAG, "surfaceDestroyed() : Method called ");
		// empty. Take care of releasing the Camera preview in your activity.
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
			int width, int height) {
		Log.d(TAG, "surfaceChanged(): width:" + width + " height:" + height);

		// Make sure to stop the preview before resizing or reformatting it.

		if (mSurfaceHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			Log.d(TAG,
					"Tried to stop a non-existent preview: " + e.getMessage());
		}

		// start preview with new settings
		try {

			Parameters parameters = mCamera.getParameters();
			Display display = ((WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();

			if (display.getRotation() == Surface.ROTATION_0) {
				parameters.setPreviewSize(height, width);
				mCamera.setDisplayOrientation(90);
			}

			if (display.getRotation() == Surface.ROTATION_90) {
				parameters.setPreviewSize(width, height);
			}

			if (display.getRotation() == Surface.ROTATION_180) {
				parameters.setPreviewSize(height, width);
			}

			if (display.getRotation() == Surface.ROTATION_270) {
				parameters.setPreviewSize(width, height);
				mCamera.setDisplayOrientation(180);
			}

			mCamera.setParameters(parameters);
		} catch (Exception e) {
			Log.d(TAG, "Error setting parameters: " + e.getMessage());
			
		}
		
		try	{
			mCamera.setPreviewDisplay(mSurfaceHolder);
			//Hang on for a few ms
			new CountDownTimer(200, 100) {

				@Override
				public void onTick(long millisUntilFinished) {
				}

				@Override
				public void onFinish() {
					cancel();
					mCamera.startPreview();
				}
			}.start();
		}
		catch(Exception e){
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());	
		}
		

	}
}
