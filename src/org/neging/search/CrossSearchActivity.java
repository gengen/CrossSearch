package org.neging.search;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.widget.SearchView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class CrossSearchActivity extends ActionBarActivity {
	public static final String TAG = "CrossSearch";
	public static final boolean DEBUG = true;
    public static final String PREF_KEY = "pref";

    SearchView mSearchView = null;
    int mTabId = 0;
    
    int mCategoryIndex = 0;
    
	boolean mDisplayFlag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//super.onSaveInstanceState(savedInstanceState);
		
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setDisplayShowTitleEnabled(false); 

        //TODO icon�ݒ�
        actionBar.addTab(actionBar.newTab()/*.setIcon(R.drawable.ic_menu_sort_by_size)*/.setText(R.string.tab1).setTabListener(
        		new MyTabListener<AmazonFragment>(this, "tab1", AmazonFragment.class)));

        actionBar.addTab(actionBar.newTab()/*.setIcon(R.drawable.ic_menu_search)*/.setText(R.string.tab2).setTabListener(
        		new MyTabListener<RakutenFragment>(this, "tab2", RakutenFragment.class)));
        
        actionBar.addTab(actionBar.newTab()/*.setIcon(R.drawable.ic_menu_search)*/.setText(R.string.tab3).setTabListener(
        		new MyTabListener<YahooFragment>(this, "tab3", YahooFragment.class)));
        
        alertNotifyDialog();
	}
	
	//�A�v�������\��
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
					//Checkbox�̏�Ԃ�ۑ�
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
	
	 // SearchVIew�̃��X�i�[
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
			 
			 //�\�t�g�L�[�{�[�h���\���ɂ���
			 InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			 
			 FragmentManager manager = getSupportFragmentManager();
			 List<Fragment> fragments = manager.getFragments();

			 if(DEBUG){
				 Log.d(TAG, "Fragment num = " + fragments.size());
			 }
			 
			 Fragment amazon = CrossSearchActivity.this.getSupportFragmentManager().findFragmentByTag("tab1");
			 Fragment rakuten = CrossSearchActivity.this.getSupportFragmentManager().findFragmentByTag("tab2");
			 Fragment yahoo = CrossSearchActivity.this.getSupportFragmentManager().findFragmentByTag("tab3");
			 //�^�u���ꂼ��ŃT�[�`
			 ((AmazonFragment)amazon).searchProductInfo(query, mCategoryIndex, 1);
			 ((RakutenFragment)rakuten).searchProductInfo(query, mCategoryIndex, 1);
			 ((YahooFragment)yahoo).searchProductInfo(query, mCategoryIndex, 1);
			 
			 return true;
		 }
	 };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// menu��`��item��id��action_search�̂��̂��擾����
		MenuItem searchItem = menu.findItem(R.id.action_search);

		// SearchView���擾����
		mSearchView = (SearchView)MenuItemCompat.getActionView(searchItem);
		 
		//mSearchView.setIconifiedByDefault(true);
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
		if (id == R.id.action_category) {
			selectCategory();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
    private void selectCategory(){
        final String[] itemList = getResources().getStringArray(R.array.category_array);
        
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.action_category_title)
        .setSingleChoiceItems(itemList, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCategoryIndex = which;
            }
        })
        .setPositiveButton(R.string.dialog_submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { //which�͏��-1
        		String[] categories = getResources().getStringArray(R.array.category_array);
            	Toast.makeText(
            			CrossSearchActivity.this, 
            			"�J�e�S�����u" + categories[mCategoryIndex] + "�v�ɐݒ肵�܂���",
            			Toast.LENGTH_LONG)
            			.show();
            }
        })
       .show();        
    }
	
    protected void onResume(){
        super.onResume();
        
		//�\�t�g�L�[�{�[�h���\���ɂ���
        //TODO �߂����Ƃ��̔�\��
		InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
		if(mSearchView != null){
			imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
        
        //UP�i�r�Q�[�V�����Ŗ߂��Ă����Ƃ��́A���̃^�u��\������
        /*
        SharedPreferences pref = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        if((pref.getBoolean("navigation", false))){
        	ActionBar actionBar = getSupportActionBar();
            actionBar.setSelectedNavigationItem(1);
            
            //�ݒ�����ɖ߂�
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("navigation", false);
            editor.commit();
        }
        */
    }
	
    @Override
    protected void onPause(){
    	super.onPause();

    	//�^�u���o���Ă���
    	ActionBar bar = getSupportActionBar();
    	mTabId = bar.getSelectedNavigationIndex();
    	//Log.d(TAG, "mTabId = " + mTabId);
    }
	
    //for 2.3
    //���ꂪ�Ȃ���2.3��Fragment�̓��e��ActionBar�ɂ��Ԃ邽�ߒǉ�
    public static int getContentViewCompat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                   android.R.id.content : R.id.action_bar_activity_content;
    }
	
    //�f�x���b�p�[�y�[�W�̃T���v���̂܂܂��ƃ^�u�؂�ւ����ɕ\�����d�Ȃ錻�ۂ������������߁A�������C��
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

    		//�R���X�g���N�^�Ńt���O�����g���쐬���Ȃ��ƋN������FragmentManager�Ƀt���O�����g��1�ƔF������Ă��܂��B
    		if (mFragment == null) {
    			mFragment = Fragment.instantiate(mActivity, mClass.getName());
    			mActivity.getSupportFragmentManager().beginTransaction().add(getContentViewCompat(), mFragment, mTag).commit();

    			//�^�u���d�Ȃ��Ă��܂����߁Atab1�ȊO��hide����
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
    			//detach������hide����
    			mActivity.getSupportFragmentManager().beginTransaction().hide(mFragment).commit();
    		}
    	}
    	
    	public void onTabReselected(Tab tab, FragmentTransaction ft) {
    		// User selected the already selected tab. Usually do nothing.
    	}
    }
}
