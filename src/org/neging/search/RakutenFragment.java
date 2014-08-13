package org.neging.search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class RakutenFragment extends Fragment {
	public static final String TAG = CrossSearchActivity.TAG;
	public static final boolean DEBUG = CrossSearchActivity.DEBUG;

	int mPage = 1;
	int mTotalPages = 1;
	int mTotalResults = 0;
    String mKeyword;
	int mCategoryIndex = 0;
	int mSortIndex = 0;

    //商品リスト
    ArrayList<ProductItemData> mProductList = null;
    
    View mView;
	Handler mHandler = new Handler();
	
	String[] mCategories = {
			"0",		//全て
			"200162",	//本
			"562637",	//家電・エレクトロニクス
			"101164",	//おもちゃ・ホビー
			"101164",	//ゲーム
			"101240",	//音楽
			"101240",	//DVD
			"100939",	//美容・コスメ
			"100227",	//食品
			"100938",	//ヘルスケア
			"100804",	//インテリア
			"101070",	//スポーツ・アウトドア
			"101114"	//車・バイク
	};
	
	String[] mSorts = {
			"standard",		//指定無し
			"+itemPrice",	//価格の低い順
			"-itemPrice"	//価格の高い順
	};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.rakuten_fragment, container, false);
    	LinearLayout layout = (LinearLayout)mView.findViewById(R.id.rakuten_bottom);
    	layout.setVisibility(View.GONE);
    	
		setListener();

        return mView;
    }
	
	private void setListener(){
		//ページ戻り
		Button prev = (Button)mView.findViewById(R.id.prev);
		prev.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(mPage > 1){
					searchProductInfo(mKeyword, mCategoryIndex, mSortIndex, --mPage);
				}
			}
		});
		
		//ページ送り
		Button next = (Button)mView.findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mPage < mTotalPages){
					searchProductInfo(mKeyword, mCategoryIndex, mSortIndex, ++mPage);
				}
			}
		});		
	}
    
    //TODO カテゴリの引渡し
    public void searchProductInfo(String keyword, int category, int sort, int page){
    	if(mKeyword != null && !mKeyword.equals(keyword)){
    		clearView();
    	}
    	
    	final String key = keyword;
    	mKeyword = keyword;
    	mCategoryIndex = category;
    	//カテゴリがAllの場合はsortは設定できない
    	if(category == 0){
        	mSortIndex = 0;
    	}
    	else{
    		mSortIndex = sort;
    	}
    	mCategoryIndex = category;
    	final int p = page;

        Thread t = new Thread(){
            public void run(){
            	String request = createRequest(key, p);
            	if(DEBUG){
            		Log.d(TAG, "url = " + request);
            	}
            	HttpResponse response = doHttpRequest(request);
            	//TODO レスポンスチェック(null and statusCode)
            	if(response == null){
            		Log.e(TAG, "response is null");
            	}
            	createList(response);
            }
        };
        t.start();
    }
    
    public void clearView(){
        ListView listview = (ListView)mView.findViewById(R.id.product_list);
        ProductArrayAdapter adapter = (ProductArrayAdapter)listview.getAdapter();
        if(adapter != null){
        	adapter.clear();
        }
    	mPage = 1;
    	mTotalPages = 1;
    	
    	LinearLayout layout = (LinearLayout)mView.findViewById(R.id.rakuten_bottom);
    	layout.setVisibility(View.INVISIBLE);
    	
        //サーチワードクリア
    	TextView text = (TextView)mView.findViewById(R.id.rakuten_text);
    	text.setText("");
    }

    private String createRequest(String key, int page){
    	String baseUrl = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20140222?";

    	Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
    	builder.appendQueryParameter("applicationId", "1052129032839387300");
    	builder.appendQueryParameter("affiliateId", "12f43af5.cfa8f2c5.12f43af6.5da98e59");
    	builder.appendQueryParameter("keyword", key);
    	builder.appendQueryParameter("format", "xml");
    	//builder.appendQueryParameter("genreId", "0"); //0=allmCategories[mCategoryIndex]
    	builder.appendQueryParameter("genreId", mCategories[mCategoryIndex]);
    	builder.appendQueryParameter("hits", "10");
    	builder.appendQueryParameter("page", "" + page);
		builder.appendQueryParameter("sort", mSorts[mSortIndex]);    		
    	builder.appendQueryParameter("carrier", "2"); //2=smartphone
    	builder.build();
    	if(DEBUG){
    		Log.d(TAG, "URL = " + builder.build());
    	}
    	
    	return builder.toString();
    }

    private HttpResponse doHttpRequest(String request){
    	HttpClient httpClient = new DefaultHttpClient(); 
    	HttpGet get = new HttpGet(request);

    	HttpResponse response = null;
    	try {
			response = httpClient.execute(get);
    	}
    	catch (Exception e) {
    		//do nothing
		}
    	
    	//200OK以外はnullとする
		if(response == null || response.getStatusLine().getStatusCode() != 200){
			response = null;
		}
    	
    	return response;
    }
    
	private void createList(HttpResponse response){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		try {
			response.getEntity().writeTo(baos);
			
			XmlPullParserFactory factory = null;
			factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(baos.toString()));

	        mProductList = new ArrayList<ProductItemData>();
			//tagがsmallImageUrlsのときにtrueにセットする			
			boolean imageFlag = false;
	    	ProductItemData data = null;
			for(int type = parser.getEventType();type != XmlPullParser.END_DOCUMENT; type = parser.next()){
				String tagName = "";
				switch(type){
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if(DEBUG){
						Log.d(TAG, "tag name = " + tagName);
					}
					
					//商品説明の始まり
					if(tagName.equals("Item")){
				    	data = new ProductItemData();
					}
					else if(tagName.equals("count")){
						parser.next();
						mTotalResults = Integer.valueOf(parser.getText());
					}
					else if(tagName.equals("pageCount")){
						parser.next();
						mTotalPages = Integer.valueOf(parser.getText());
					}
					else if(tagName.equals("affiliateUrl")){
						parser.next();
						String url = parser.getText();
						data.setDetailURL(url);
					}
					else if(tagName.equals("itemName")){
						parser.next();
						String title = parser.getText();
						data.setTitle(title);
					}
					else if(tagName.equals("itemPrice")){
						parser.next();
						String price = parser.getText();
						price = getString(R.string.price_prefix) + price;
						data.setPrice(price);
					}
					else if(tagName.equals("imageUrl")){
						if(imageFlag){
							parser.next();
							String url = parser.getText();
							data.setImageURL(url);
							imageFlag = false;
						}
					}
					else if(tagName.equals("smallImageUrls")){
						imageFlag = true;
					}

					break;
					
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if(DEBUG){
						Log.d(TAG, "end tag name = " + tagName);
					}
					//商品説明の終わり
					if(tagName.equals("Item")){
						if(data.getPrice() == null){
							data.setPrice(getString(R.string.price_not_found));
						}
						mProductList.add(data);
						data = null;
					}
					break;
					
				default:
					break;
				}					
			}
		}
		catch (Exception e) {
	        mProductList = new ArrayList<ProductItemData>();
	    	ProductItemData data = new ProductItemData();
	    	data.setErrFlag();
	    	mProductList.add(data);
		}
		
		mHandler.post(new Runnable(){
			@Override
			public void run() {
		        ProductArrayAdapter adapter = new ProductArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mProductList);
		        ListView listview = (ListView)mView.findViewById(R.id.product_list);
		        listview.setAdapter(adapter);
		        listview.setScrollingCacheEnabled(false); 
		        listview.setOnItemClickListener(new OnItemClickListener(){
		    		@Override
		    		public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
		    			ProductItemData item = (ProductItemData)adapter.getItemAtPosition(pos);
		    			String detailUrl = item.getDetailURL();
		    			if(detailUrl != null){
		    				Intent intent = new Intent(getActivity(), ProductWebActivity.class);
		    				intent.putExtra("url", detailUrl);
		    				startActivity(intent);
		    			}
		    		}
		        });
		        
		        //サーチワード表示
		        displayKeyword();
		        //ページ送り、戻り
		    	displayPage();
		    	//プログレスダイアログ終了
		    	try{
		    		CrossSearchActivity activity = (CrossSearchActivity)getActivity();
		    		activity.setFinished("tab2");
		    	}
		    	catch(ClassCastException e){
		    		//nothing to do
		    	}
			}
		});
    }
	
	private void displayKeyword(){
		String[] categories = getResources().getStringArray(R.array.category_array);

		String results = getString(R.string.search_result) + " " + mTotalResults + getString(R.string.search_unit);
		results = results + ": " + categories[mCategoryIndex] + " > " + "\"" + mKeyword + "\"";
    	TextView text = (TextView)mView.findViewById(R.id.rakuten_text);
    	text.setText(results);		
	}
	
	private void displayPage(){
    	LinearLayout layout = (LinearLayout)mView.findViewById(R.id.rakuten_bottom);
    	TextView view = (TextView)mView.findViewById(R.id.page);
    	view.setText("" + mPage);
    	layout.setVisibility(View.VISIBLE);
		Button prev = (Button)mView.findViewById(R.id.prev);
    	if(mPage == 1){
    		prev.setVisibility(View.INVISIBLE);
    	}
    	else{
    		prev.setVisibility(View.VISIBLE);		    		
    	}
    	
		Button next = (Button)mView.findViewById(R.id.next);		    	
    	if(mPage == mTotalPages){
    		next.setVisibility(View.INVISIBLE);
    	}
    	else{
    		next.setVisibility(View.VISIBLE);		    		
    	}		
	}
}

