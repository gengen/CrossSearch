package org.neging.search;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ProductWebActivity extends Activity {
    ProgressDialog mProgressDialog = null;
    String mUrl;
    WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.web);
        Bundle extras = getIntent().getExtras();        
        mUrl = extras.getString("url");

        initProgressDialog();
        loadWeb();
    }
    
    void initProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.dialog_progress_message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }
    
    void loadWeb(){
    	mProgressDialog.show();
    	
        mWebView = (WebView)findViewById(R.id.web);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl(mUrl);    	
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
    		mWebView.goBack();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    public final class MyWebViewClient extends WebViewClient {
    	@Override
    	public void onPageFinished(WebView view, String url){
        	mProgressDialog.dismiss();
    	}
    }
}
