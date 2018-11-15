package ti.camera.utils;

import java.io.Serializable;

/**
 * This contains the information of newly snapped image from the camera or image  downloaded  from the server.
 */
public class ImagesTempInfo implements Serializable
{

	/**
     * 
     */
    private static final long serialVersionUID = 2202306488900609293L;
	private String additionalInfo;
	private String code;
	private String description;
	private String localPath;
	private String imageName;
	public String getAdditionalInfo()
	{
	    return additionalInfo;
	}
	public void setAdditionalInfo(String additionalInfo)
	{
	    this.additionalInfo = additionalInfo;
	}
	public String getCode()
	{
	    return code;
	}
	public void setCode(String code)
	{
	    this.code = code;
	}
	public String getDescription()
	{
	    return description;
	}
	public void setDescription(String description)
	{
	    this.description = description;
	}
	public String getLocalPath()
	{
	    return localPath;
	}
	public void setLocalPath(String localPath)
	{
	    this.localPath = localPath;
	}
	public String getImageName()
	{
	    return imageName;
	}
	public void setImageName(String imageName)
	{
	    this.imageName = imageName;
	}
	


}
