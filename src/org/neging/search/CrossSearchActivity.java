package org.neging.search;

import java.io.File;
import java.util.List;

import jp.beyond.sdk.Bead;
import jp.beyond.sdk.Bead.ContentsOrientation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.widget.SearchView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class CrossSearchActivity extends ActionBarActivity{
	public static final String TAG = "CrossSearch";
	public static final boolean DEBUG = false;
    public static final String PREF_KEY = "pref";

    SearchView mSearchView = null;
    int mTabId = 0;
    
    int mCategoryIndex = 0;
    int mSortIndex = 0;
    
	boolean mDisplayFlag = false;
	
	//処理終了判定フラグ
	boolean mFinishTab1 = false;
	boolean mFinishTab2 = false;
	boolean mFinishTab3 = false;
	
    ProgressDialog mProgressDialog = null;
    
    //BEAD ad
    private Bead mBeadExit = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setDisplayShowTitleEnabled(false); 

        //TODO icon設定
        actionBar.addTab(actionBar.newTab()/*.setIcon(R.drawable.ic_menu_sort_by_size)*/.setText(R.string.tab1).setTabListener(
        		new MyTabListener<AmazonFragment>(this, "tab1", AmazonFragment.class)));

        actionBar.addTab(actionBar.newTab()/*.setIcon(R.drawable.ic_menu_search)*/.setText(R.string.tab2).setTabListener(
        		new MyTabListener<RakutenFragment>(this, "tab2", RakutenFragment.class)));
        
        actionBar.addTab(actionBar.newTab()/*.setIcon(R.drawable.ic_menu_search)*/.setText(R.string.tab3).setTabListener(
        		new MyTabListener<YahooFragment>(this, "tab3", YahooFragment.class)));
        
        initProgressDialog();
        alertNotifyDialog();
        
        //BEAD ad
        mBeadExit = Bead. createExitInstance("df90e2a0ddfc86513c57c79fe09f2326699e6fb5ad2df0d6", ContentsOrientation.Auto);
        mBeadExit.requestAd(this);		
	}
	
	//アプリ説明表示
    private void alertNotifyDialog(){
		SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean flag = sharePref.getBoolean("displayFlag", true);
		if(flag){
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			View view = inflater.inflate(R.layout.notify_dialog, null);
			
			AlertDialog dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_notify_title)
			.setMessage(getString(R.string.dialog_notify_message))
			.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//Checkboxの状態を保存
					SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(CrossSearchActivity.this);
					Editor editor = sharePref.edit();
					editor.putBoolean("displayFlag", !mDisplayFlag);
					editor.commit();
				}
			}).create();
			dialog.setView(view, 0, 0, 0, 0);
			dialog.show();

			CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkBox1);
			checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
					mDisplayFlag = isChecked;
				}
			});
		}
    }
    
    void initProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.dialog_progress_message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }
    
    public void setFinished(String tab){
    	if(tab.equals("tab1")){
    		mFinishTab1 = true;
    	}
    	else if(tab.equals("tab2")){
    		mFinishTab2 = true;
    	}
    	if(tab.equals("tab3")){
    		mFinishTab3 = true;
    	}

    	//現状はどれかひとつ終了すればよい
    	if(mFinishTab1 || mFinishTab2 || mFinishTab3){
    		//TODO プログレスバー終了
			 mProgressDialog.dismiss();
    		
    		mFinishTab1 = false;
    		mFinishTab2 = false;
    		mFinishTab3 = false;
    		
    		//フォーカスをはずしてソフトキーボードを非表示にする
    		if(mSearchView != null){
    			mSearchView.clearFocus();
    		}
    	}
    }
	
	 // SearchVIewのリスナー
	 private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
		 @Override
		 public boolean onQueryTextChange(String newText) {
			 return true;
		 }
	 
		 @Override
		 public boolean onQueryTextSubmit(String query) {
			 if(DEBUG){
				 Log.d(TAG, "TextSubmit = " + query);
			 }
			 
			 //ソフトキーボードを非表示にする
			 /*
			 InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
             */
			 //フォーカスをはずしてソフトキーボードを非表示にする
			 if(mSearchView != null){
				 mSearchView.clearFocus();
			 }
			 
			 FragmentManager manager = getSupportFragmentManager();
			 List<Fragment> fragments = manager.getFragments();

			 if(DEBUG){
				 Log.d(TAG, "Fragment num = " + fragments.size());
			 }
			 
			 //プログレスダイアログ表示
			 mProgressDialog.show();
		    	
			 Fragment amazon = CrossSearchActivity.this.getSupportFragmentManager().findFragmentByTag("tab1");
			 Fragment rakuten = CrossSearchActivity.this.getSupportFragmentManager().findFragmentByTag("tab2");
			 Fragment yahoo = CrossSearchActivity.this.getSupportFragmentManager().findFragmentByTag("tab3");
			 //タブそれぞれでサーチ
			 ((AmazonFragment)amazon).searchProductInfo(query, mCategoryIndex, mSortIndex, 1);
			 ((RakutenFragment)rakuten).searchProductInfo(query, mCategoryIndex, mSortIndex, 1);
			 ((YahooFragment)yahoo).searchProductInfo(query, mCategoryIndex, mSortIndex, 1);
			 
			 return true;
		 }
	 };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// menu定義のitemのidがaction_searchのものを取得する
		MenuItem searchItem = menu.findItem(R.id.action_search);

		// SearchViewを取得する
		mSearchView = (SearchView)MenuItemCompat.getActionView(searchItem);
		 
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setSubmitButtonEnabled(true);
		mSearchView.setQueryHint(getString(R.string.search_title));
		mSearchView.setOnQueryTextListener(mOnQueryTextListener);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		int id = item.getItemId();
		if(id == R.id.action_category){
			selectCategory();
			return true;
		}
		else if(id == R.id.action_sort){
			selectSort();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
    private void selectCategory(){
        final String[] itemList = getResources().getStringArray(R.array.category_array);
        
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.action_category_title)
        .setSingleChoiceItems(itemList, mCategoryIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCategoryIndex = which;
            }
        })
        .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { //whichは常に-1
        		String[] categories = getResources().getStringArray(R.array.category_array);
            	Toast.makeText(
            			CrossSearchActivity.this, 
            			"カテゴリを「" + categories[mCategoryIndex] + "」に設定しました",
            			Toast.LENGTH_LONG)
            			.show();
            }
        })
       .show();        
    }
    
    private void selectSort(){
    	//カテゴリが全ての場合は、ソート設定できないためアラートを出す
    	if(mCategoryIndex == 0){
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(R.string.dialog_notify_title)
            .setMessage(R.string.dialog_alert_message)
            .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	//nothing to do
                }
            })
           .show();
            return;
    	}
    	
        final String[] itemList = getResources().getStringArray(R.array.sort_array);
        
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.action_sort_title)
        .setSingleChoiceItems(itemList, mSortIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSortIndex = which;
            }
        })
        .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { //whichは常に-1
        		String[] sorts = getResources().getStringArray(R.array.sort_array);
            	Toast.makeText(
            			CrossSearchActivity.this, 
            			"並べ替えを「" + sorts[mSortIndex] + "」に設定しました",
            			Toast.LENGTH_LONG)
            			.show();
            }
        })
       .show();        
    }
	
    protected void onResume(){
        super.onResume();
        
		//ソフトキーボードを非表示にする
        /*
		InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
		if(mSearchView != null){
			imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		*/
		 //フォーカスをはずしてソフトキーボードを非表示にする
		 if(mSearchView != null){
			 mSearchView.clearFocus();
		 }
        
        //UPナビゲーションで戻ってきたときは、元のタブを表示する
        /*
        SharedPreferences pref = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        if((pref.getBoolean("navigation", false))){
        	ActionBar actionBar = getSupportActionBar();
            actionBar.setSelectedNavigationItem(1);
            
            //設定を元に戻す
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("navigation", false);
            editor.commit();
        }
        */
    }
    
    @Override
    public void onBackPressed(){
    	// 広告ダイアログ表示
    	mBeadExit.showAd(this);
    }
	
    @Override
    protected void onPause(){
    	super.onPause();

    	//タブを覚えておく
    	ActionBar bar = getSupportActionBar();
    	mTabId = bar.getSelectedNavigationIndex();
    	//Log.d(TAG, "mTabId = " + mTabId);
    }
    
    @Override
	public void onDestroy(){
    	super.onDestroy();
    	
    	// 広告終了
    	if(mBeadExit != null){
    		mBeadExit.endAd();
    	}
    	
    	deleteCache(getCacheDir());
    }

    public static boolean deleteCache(File dir) {
    	if(dir==null) {
    		return false;
    	}
    	if (dir.isDirectory()) {
    		String[] children = dir.list();
    		for (int i = 0; i < children.length; i++) {
    			boolean success = deleteCache(new File(dir, children[i]));
    			if (!success) {
    				return false;
    			}
    		}
    	}
    	return dir.delete();
    }
	
    //for 2.3
    //これがないと2.3でFragmentの内容がActionBarにかぶるため追加
    public static int getContentViewCompat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                   android.R.id.content : R.id.action_bar_activity_content;
    }
	
    //デベロッパーページのサンプルのままだとタブ切り替え時に表示が重なる現象が発生したため、いくつか修正
    public static class MyTabListener<T extends Fragment> implements ActionBar.TabListener{
    	private Fragment mFragment;
    	private final ActionBarActivity mActivity;
    	private final String mTag;
    	private final Class<T> mClass;

    	/** Constructor used each time a new tab is created.
    	 * @param activity  The host Activity, used to instantiate the fragment
    	 * @param tag  The identifier tag for the fragment
    	 * @param clz  The fragment's Class, used to instantiate the fragment
    	 * @return 
    	 */
    	public MyTabListener(ActionBarActivity activity, String tag, Class<T> clz) {
    		mActivity = activity;
    		mTag = tag;
    		mClass = clz;
    		
    		//mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);

    		//コンストラクタでフラグメントを作成しないと起動時にFragmentManagerにフラグメントが1つと認識されてしまう。
    		if (mFragment == null) {
    			mFragment = Fragment.instantiate(mActivity, mClass.getName());
    			mActivity.getSupportFragmentManager().beginTransaction().add(getContentViewCompat(), mFragment, mTag).commit();

    			//タブが重なってしまうため、tab1以外はhideする
    			if(!mTag.equals("tab1")){
    				mActivity.getSupportFragmentManager().beginTransaction().hide(mFragment).commit();
    			}
    		}
    	}
    	
    	/* The following are each of the ActionBar.TabListener callbacks */
    	public void onTabSelected(Tab tab, FragmentTransaction ft) {
    		// Check if the fragment is already initialized
    		/*
    		if (mFragment == null) {
    			// If not, instantiate and add it to the activity
    			mFragment = Fragment.instantiate(mActivity, mClass.getName());
    			//ft.add(android.R.id.content, mFragment, mTag);
    			mActivity.getSupportFragmentManager().beginTransaction().add(getContentViewCompat(), mFragment, mTag).commit();
    		} else {
    			// If it exists, simply attach it in order to show it
				//ft.attach(mFragment);
    			if (mFragment.isDetached()) { 
    				mActivity.getSupportFragmentManager().beginTransaction().attach(mFragment).commit();
    			}
    			mActivity.getSupportFragmentManager().beginTransaction().show(mFragment).commit();
    		}
    		*/
    		
    		if (mFragment != null) {
    			mActivity.getSupportFragmentManager().beginTransaction().show(mFragment).commit();
    		}
    	}
    	
    	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    		if (mFragment != null) {
    			// Detach the fragment, because another one is being attached
    			//ft.detach(mFragment);
				//mActivity.getSupportFragmentManager().beginTransaction().detach(mFragment).commit();
    			//detachせずにhideする
    			mActivity.getSupportFragmentManager().beginTransaction().hide(mFragment).commit();
    		}
    	}
    	
    	public void onTabReselected(Tab tab, FragmentTransaction ft) {
    		// User selected the already selected tab. Usually do nothing.
    	}
    }
}
