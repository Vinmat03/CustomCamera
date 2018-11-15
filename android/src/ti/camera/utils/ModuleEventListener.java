package ti.camera.utils;

import org.json.JSONObject;

/**
 * Listener interface to implement save, set default and delete events. 
 */
public interface ModuleEventListener
{
    public abstract void saveImages(JSONObject imagesInfo);
    public void deleteImage(JSONObject jsonObject);
    public void setDefaultImage(JSONObject jsonObject);
}
