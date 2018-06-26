package com.example.xyzreader.ui;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
		implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private Cursor mCursor;
	private long mStartId;
	
	private ViewPager mPager;
	private MyPagerAdapter mPagerAdapter;
	private TextView mTitleText;
	private TextView mSubTitleText;
	private ImageView mPhotoView;
	private ProgressBar loadingIndicator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
							View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}
		setContentView(R.layout.activity_article_detail);
		loadingIndicator = findViewById(R.id.loading);
		
		overridePendingTransition(R.anim.slide_in,R.anim.slide_out);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mTitleText = findViewById(R.id.article_title);
		mSubTitleText = findViewById(R.id.article_subtitle);
		mPhotoView = findViewById(R.id.title_image);
		
		getSupportLoaderManager().initLoader(0, null, this);
		
		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mPager = findViewById(R.id.pager);
		mPager.setPageMargin((int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
		
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}
			
			@Override
			public void onPageSelected(int position) {
				if (mCursor != null) {
					mCursor.moveToPosition(position);
					loadingIndicator.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
			
			}
		});
		
		if (savedInstanceState == null) {
			if (getIntent() != null && getIntent().getData() != null) {
				mStartId = ItemsContract.Items.getItemId(getIntent().getData());
			}
		}
		
		findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPagerAdapter.getCurrentFragment().shareArticle();
			}
		});
	}
	
	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return ArticleLoader.newAllArticlesInstance(this);
	}
	
	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
		mCursor = cursor;
		mPager.setAdapter(mPagerAdapter);
		mPagerAdapter.notifyDataSetChanged();
		// Select the start ID
		if (mStartId > 0) {
			mCursor.moveToFirst();
			// TODO: optimize
			while (!mCursor.isAfterLast()) {
				if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
					mPager.setCurrentItem(mCursor.getPosition(), false);
					break;
				}
				mCursor.moveToNext();
			}
			mStartId = 0;
		}
	}
	
	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
		mCursor = null;
		mPagerAdapter.notifyDataSetChanged();
	}
	
	
	private class MyPagerAdapter extends FragmentPagerAdapter implements ArticleDetailFragment.LoadHeadData {
		
		private ArticleDetailFragment currentFragment;
		int position;
		
		MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			super.setPrimaryItem(container, position, object);
		}
		
		@Override
		public Fragment getItem(int position) {
			mCursor.moveToPosition(position);
			this.position = position;
			currentFragment =ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), this, position);
			return currentFragment;
		}
		
		public ArticleDetailFragment getCurrentFragment() {
			return currentFragment;
		}
		
		
		@Override
		public int getCount() {
			return (mCursor != null) ? mCursor.getCount() : 0;
		}
		
		@Override
		public void onLoadHeadData(String title, Spanned subtitle, String photoUrl, int position) {
			Log.w("LoadHeadDataAc",title);
			Log.w("positions:",mPager.getCurrentItem()+":"+position);
			if(mPager.getCurrentItem()== position) {
				mTitleText.setText(title);
				mSubTitleText.setMovementMethod(new LinkMovementMethod());
				mSubTitleText.setText(subtitle);
				Picasso.get().load(photoUrl).into(mPhotoView);
				loadingIndicator.setVisibility(View.GONE);
			}
		}
	}
}
