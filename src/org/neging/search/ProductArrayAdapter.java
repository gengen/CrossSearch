package org.neging.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductArrayAdapter extends ArrayAdapter<ProductItemData> {
    private LayoutInflater mInflater;
    Context mContext;
    
    Handler mHandler = new Handler();

    public ProductArrayAdapter(Context context, int textViewResourceId, List<ProductItemData> objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //if(convertView == null){
        convertView = mInflater.inflate(R.layout.product_item, null);
        //}

        ProductItemData data = (ProductItemData)getItem(position);

        //サムネイル
        //TODO サムネイル無い場合の処理
        ImageView image = (ImageView)convertView.findViewById(R.id.item_thumbnail);
        String url = data.getImageURL();
        ImageTask task = new ImageTask(url, image);
        task.execute();
        
        //商品名
        TextView name = (TextView)convertView.findViewById(R.id.item_title);
        //商品のURL
        //final String detailUrl = data.getDetailURL(); 
        name.setText(data.getTitle());
        /*
        name.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, ProductWebActivity.class);
				intent.putExtra("url", detailUrl);
				mContext.startActivity(intent);
			}
        });
        */
        
        //価格
        TextView price = (TextView)convertView.findViewById(R.id.item_price);
        price.setText(data.getPrice());
        
        //TODO レビュー

        return convertView;
    }
    
    class ImageTask extends AsyncTask<Void, Void, Void>{
    	String mUrl;
    	ImageView mView;
    	
    	ImageTask(String url, ImageView view){
    		mUrl = url;
    		mView = view;
    	}

		@Override
		protected Void doInBackground(Void... arg0) {
	        InputStream istream = null;
			try {
		        URL url = new URL(mUrl);
				istream = url.openStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	        final Bitmap bm = BitmapFactory.decodeStream(istream);
	        
			if(bm != null){
				mHandler.post(new Runnable(){
					@Override
					public void run() {
						mView.setImageBitmap(bm);						
					}
				});
			}
			
			try {
				istream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
    }
}
