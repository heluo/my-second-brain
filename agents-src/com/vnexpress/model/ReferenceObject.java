package com.vnexpress.model;

import java.util.Date;

public class ReferenceObject {
	public static final int STATUS_VISIBLE = 1;
	public static final int STATUS_NOT_VISIBLE = 0;
	
	private String objectID;
	private String articleID;
	private String md5;
	private String url;
	private String credit;
	private String caption;
	private ReferenceType type;
	private Date creationTime;
	private Date updateTime;
	public ReferenceObject() {
		super();
	}
	
	public ReferenceObject(String url,String caption) {
		this.url = url;
		this.caption = caption;
	}
	
	public ReferenceObject(String articleID, String md5, String url,
			ReferenceType type, Date updateTime) {
		super();
		this.articleID = articleID;
		this.md5 = md5;
		this.url = url;
		this.type = type;
		this.updateTime = updateTime;
	}

	public static enum ReferenceType {
	    IMG(1),
	    VIDEO(2),
	    RELATED_LINK(3);
	    private final int index;   

	    ReferenceType(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
	public String getArticleID() {
		return articleID;
	}
	public void setArticleID(String articleID) {
		this.articleID = articleID;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public ReferenceType getType() {
		return type;
	}
	public void setType(ReferenceType type) {
		this.type = type;
	}
	public Date getUpdateTime() {
		if(updateTime == null){
			updateTime = new Date();
		}
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public Date getCreationTime() {
		if(creationTime == null){
			creationTime = new Date();
		}
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public String getObjectID() {
		return objectID;
	}
	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}
	public String getCredit() {
		if(credit == null){
			credit = "";
		}
		return credit;
	}
	public void setCredit(String credit) {		
		this.credit = credit;
	}
	public String getCaption() {
		if(caption == null){
			caption = "";
		}
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}

}
