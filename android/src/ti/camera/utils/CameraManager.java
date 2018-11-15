package ti.camera.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ti.camera.activities.CustomCameraActivity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it.
 */
public class CameraManager {

	private static CameraManager cameraManager;
	private Context context;
	private static Camera camera;
	public static int SDK_INT;
	private static final String TAG = CameraManager.class.getSimpleName();
	private static CustomCameraActivity mCustomCameraActivity;
	private static File cameraImagesStorageDirectory;
	private static ModuleEventListener mModuleEventListener;
	private static JSONArray mImagesJSONArray;

	private CameraManager(Context context) {

		this.context = context;
	}

	/**
	 * Initializes this static object with the Context of the calling Activity.
	 * 
	 * @param context
	 *            The Activity which wants to use the camera.
	 */
	public static void init(Context context) {
		if (cameraManager == null) {
			Log.d(TAG, "init():CameraManager initialized.");
			cameraManager = new CameraManager(context);
		}

	}

	/**
	 * Gets the CameraManager singleton instance.
	 * 
	 * @return A reference to the CameraManager's singleton instance.
	 */
	public static CameraManager getCameraManagerInstance() {

		return cameraManager;
	}

	/**
	 * Helper method to access the camera returns null if it cannot get the
	 * camera or does not exist
	 * 
	 * @return Camera
	 */
	public static Camera getCameraInstance() {
		if (camera == null) {
			try {
				camera = Camera.open();
				Log.d(TAG, "getCameraInstance() : Camera opened.");
			} catch (Exception e) {
				Log.d(TAG,
						"getCameraInstance() : Cannot get camera or does not exist.");
			}
		}

		return camera;
	}

	/**
	 * Closes the camera if still in use.
	 */
	public static void releaseCamera() {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
			Log.d(TAG, "releaseCamera() : released.");
		}
	}

	/**
	 * Fire saveImages() which is implemented in CustomcameraModiule
	 */
	public static void fireSaveEventListener() {
		mModuleEventListener.saveImages(ImageAdapter.getImagesInfo());
	}

	/**
	 * Fire saveImages() which is implemented in CustomcameraModiule
	 */
	public static void fireSetDefaultEventListener(JSONObject imageInfo) {
		// Log.d(TAG, " jsonArray.toString()111 :" +
		// getImagesInfo(getImageDirecotry()));
		mModuleEventListener.setDefaultImage(imageInfo);
	}

	/**
	 * Registers Save,Delete and setDefault event listener.
	 * 
	 * @param moduleEventListener
	 */

	public static void registerModuleEventListener(
			ModuleEventListener moduleEventListener) {
		mModuleEventListener = moduleEventListener;
	}

	/**
	 * Fires deleteImage() method which is implemented in
	 * CustomcameraModiule.java
	 */
	public static void fireDeleteImageEventListener(JSONObject imageInfo) {
		Log.d(TAG, " fireDeleteImageEventListener() called");
		mModuleEventListener.deleteImage(imageInfo);
	}

	/**
	 * Creates directory CustomCameraModule for saving images on sd card.
	 */
	public static void createCameraImagesStorageDirectory() {
		cameraImagesStorageDirectory = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"CustomCameraModule");
		if (!cameraImagesStorageDirectory.exists()) {
			if (!cameraImagesStorageDirectory.mkdirs()) {
				Log.d(TAG, "Failed to create cameraImagesStorageDirectory");
				return;
			}
		}
		Log.d(TAG,
				"createCameraImagesStorageDirectory() : CameraImagesStorageDirectory created : "
						+ cameraImagesStorageDirectory.getAbsolutePath());
	}

	/**
	 * Creates absolute path with image name for saving images to sd card.This
	 * path is only for images snaped by the camera not for those downloaded
	 * from the server.
	 */
	public static File getOutputMediaFile() {
		// Create a image file name

		String timeStamp = Long.toString(System.currentTimeMillis());
		File mediaFile;
		mediaFile = new File(cameraImagesStorageDirectory.getPath()
				+ File.separator + "TMP_" + timeStamp + ".jpg");
		Log.d(TAG, "Image saved as : " + mediaFile.getAbsolutePath()
				+ " mediaFile.getParent() : " + mediaFile.getParent());

		return mediaFile;
	}

	/**
	 * Get the image directory path
	 */
	public static File getImageDirecotry() {
		return cameraImagesStorageDirectory;
	}

	/**
	 * This method is called when cancel button of the module is pressed.
	 * 
	 * @param fileOrDirectory
	 */
	public static void deleteImageDirectory(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteImageDirectory(child);
			}
		}

		fileOrDirectory.delete();
	}

	/**
	 * Holds the reference of CustomCameraActivity so that on save button click
	 * in the PreviewActivity this activity can be finished using
	 * finishCustomCameraActivity() method.
	 * 
	 * @param customCameraActivity
	 */
	public static void setCustomCameraActivity(
			CustomCameraActivity customCameraActivity) {
		mCustomCameraActivity = customCameraActivity;
	}

	public static CustomCameraActivity getCustomCameraActivity() {
		return mCustomCameraActivity;
	}

	/**
	 * Finish the CustomCameraActivity
	 */
	public static void finishCustomCameraActivity() {
		mCustomCameraActivity.finish();
	}

	/*
	 * Returns the image index from the mImagesJSONArray which holds the images
	 * data to render on the grid view.
	 */
	public static int getImageIndex(String code) {
		int index = 0;
		for (int i = 0; i < mImagesJSONArray.length(); ++i) {
			JSONObject rec;
			try {
				rec = mImagesJSONArray.getJSONObject(i);
				String imageCode = rec.getString("code");
				if (imageCode.equals(code)) {
					index = i;
					return index;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		return index;
	}

	/**
	 * JSONArray object which keeps the original array came from the
	 * titanium.List of images to be downloaded.
	 * 
	 * @param imagesJSONArray
	 */
	public static void setImagesJSONArray(JSONArray imagesJSONArray) {
		mImagesJSONArray = imagesJSONArray;
	}

	/**
	 * Returns the image file extension (.png, .jpg ect).
	 * 
	 * @param url
	 *            url of the image to be downloaded form the server.
	 * @return
	 */
	public static String getFileExtension(String url) {
		String extension = null;
		extension = MimeTypeMap.getFileExtensionFromUrl(url);
		return extension;
	}

	public static void clearReferences() {
		mCustomCameraActivity = null;
		cameraImagesStorageDirectory = null;
		mModuleEventListener = null;
		mImagesJSONArray = null;
	}
}
