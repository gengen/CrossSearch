package org.neging.search;

public class ProductItemData {
	private boolean mErrFlag = false;
	private String mImageUrl;
	private String mTitle;
	private String mDetailUrl;
	private String mPrice;
	private String mReviewUrl;

	public void setErrFlag(){
		mErrFlag = true;
	}
	
	public boolean getErrFlag(){
		return mErrFlag;
	}
	
	public void setImageURL(String url){
		mImageUrl = url;
	}
	
	public String getImageURL(){
		return mImageUrl;
	}

	public void setTitle(String title){
		mTitle = title;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public void setDetailURL(String url){
		mDetailUrl = url;
	}
	
	public String getDetailURL(){
		return mDetailUrl;
	}
	
	public void setPrice(String price){
		mPrice = price;
	}
	
	public String getPrice(){
		return mPrice;
	}
	
	public void setReviewURL(String url){
		mReviewUrl = url;
	}
	
	public String getReviewURL(){
		return mReviewUrl;
	}
}
