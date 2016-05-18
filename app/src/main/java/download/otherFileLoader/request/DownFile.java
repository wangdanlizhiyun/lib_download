package download.otherFileLoader.request;

import java.io.Serializable;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;


@SuppressWarnings("serial")
public class DownFile implements Serializable{
	public static final String COL_DOWNURL = "_downurl";
	public DownFile() {
		super();
	}

	public DownFile(String downUrl) {
		super();
		this.downUrl = downUrl;
	}
	/**
	 * 上一次的通知更新时间
	 */
	public long lastNotifyTime;
	
	/** 图标. */
	public String icon;
	
	/** 文件名称. */
	public String name;
	
	/** 文件简介. */
	public String description;
	
	/** 如果是apk文件表示包名. */
	public String pakageName;
	/** 1表示已下载完成 0表示未开始下载 2表示已开始下载. */
	public int state;
	/**
	 * 1表示安装了0表示没安装
	 */
	public int isInstall;
	
	/** 文件下载路径. */
    @PrimaryKey(AssignType.BY_MYSELF)
	@NotNull
	@Column(COL_DOWNURL)
	public String downUrl;
	
	/** 文件保存路径. */
	public String downPath;
	
	/** 文件当前下载大小. */
	public int downLength;
	
	/** 文件总大小. */
	public int totalLength;


	/**
	 * Gets the icon.
	 *
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Sets the icon.
	 *
	 * @param icon the new icon
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the pakage name.
	 *
	 * @return the pakage name
	 */
	public String getPakageName() {
		return pakageName;
	}

	/**
	 * Sets the pakage name.
	 *
	 * @param pakageName the new pakage name
	 */
	public void setPakageName(String pakageName) {
		this.pakageName = pakageName;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * Gets the down url.
	 *
	 * @return the down url
	 */
	public String getDownUrl() {
		return downUrl;
	}

	/**
	 * Sets the down url.
	 *
	 * @param downUrl the new down url
	 */
	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	/**
	 * Gets the down path.
	 *
	 * @return the down path
	 */
	public String getDownPath() {
		return downPath;
	}

	/**
	 * Sets the down path.
	 *
	 * @param downPath the new down path
	 */
	public void setDownPath(String downPath) {
		this.downPath = downPath;
	}

	/**
	 * Gets the down length.
	 *
	 * @return the down length
	 */
	public int getDownLength() {
		return downLength;
	}

	/**
	 * Sets the down length.
	 *
	 * @param downLength the new down length
	 */
	public void setDownLength(int downLength) {
		this.downLength = downLength;
	}

	/**
	 * Gets the total length.
	 *
	 * @return the total length
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * Sets the total length.
	 *
	 * @param totalLength the new total length
	 */
	public void setTotalLength(int totalLength) {
		this.totalLength = totalLength;
	}

	public int getIsInstall() {
		return isInstall;
	}

	public void setIsInstall(int isInstall) {
		this.isInstall = isInstall;
	}
	
	
}
