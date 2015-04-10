package jerome.news;

import jerome.news.adapter.FragAdapter;
import jerome.news.fragment.BaseFragment;
import jerome.news.lazy.ImageLoader2;
import jerome.news.title.TitleView;
import jerome.news.util.DepthPageTransformer;
import jerome.news.util.NetUtil2;
import jerome.news.util.TtsUtil;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import jerome.news.R;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

public class Main extends FragmentActivity{

	private String TAG = "zwq";
	private ViewPager vPager;
	private FragAdapter adapter;
	private LinearLayout titleLayout = null;
	
	private BaseFragment bFragment = null;

	Button speak = null;//com.sohu.newsclient

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		initLayout();
		initViewPage();
		initTitle();
	}

	private void initLayout() {
		vPager = (ViewPager) findViewById(R.id.main_page);
		titleLayout = (LinearLayout) findViewById(R.id.main_title);

		speak = (Button) findViewById(R.id.main_speak);
		speak.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				index = 0;
				TtsUtil.getInstance(Main.this).stopSpeak();
				speak();
			}
			
		});
	}

	private void initViewPage() {
		Resources res = getResources();
		vPager.setOffscreenPageLimit(8);
		adapter = new FragAdapter(getSupportFragmentManager(),
				res.getStringArray(R.array.channel_names),
				res.getIntArray(R.array.channel_ids));
		vPager.setAdapter(adapter);

		changeTitleState(0);

		vPager.setOnPageChangeListener(new MyVPageChangeListener());
		vPager.setPageTransformer(true, new DepthPageTransformer());
		NetUtil2.getInstance().start();
	}

	private void initTitle() {
		Resources res = getResources();
		String[] names = res.getStringArray(R.array.channel_names);
		int[] ids = res.getIntArray(R.array.channel_ids);
		for (int i = 0; i < names.length; i++) {
			TitleView titleView = new TitleView(this);
			titleView.setText(names[i]);
			titleView.setIndex(ids[i]);
			if (i == 0) {
				titleView.setSelectedState(1);
			}
			titleView.setOnClickListener(titleClick);
			titleLayout.addView(titleView);
		}
	}

	private class MyVPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int location) {
			Log.i(TAG, "onPageSelected:" + location);
			changeTitleState(location);
			if (location <= 1 && titleLayout.getScrollX() != 0) {
				
			}
		}

	}

	private void changeTitleState(int location) {
		// 改变分类状态
		for (int i = 0; i < titleLayout.getChildCount(); i++) {
			TitleView titleView = (TitleView) titleLayout.getChildAt(i);
			if (location == titleView.getIndex()) {
				titleView.setSelectedState(1);
			} else {
				titleView.setSelectedState(0);
			}
		}
		vPager.setCurrentItem(location);
		NetUtil2.getInstance().setCurrentPage(location);
	}

	OnClickListener titleClick = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			TitleView currentView = (TitleView) arg0;
			changeTitleState(currentView.getIndex());
		}

	};
	
	@Override
	protected void onPause() {
		NetUtil2.getInstance().onThreadPause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		NetUtil2.getInstance().onThreadResume();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageLoader2.getInstance(getApplicationContext()).clearPic();
		TtsUtil.getInstance(Main.this).stopSpeak();
		System.exit(0);
	}
	
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitAPP();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
    }
	
	private void exitAPP() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.exit_title)).setMessage(getString(R.string.exit_tip))
				.setPositiveButton(getString(R.string.exit_submit), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						NetUtil2.getInstance().closeThread();
						finish();
					}

				})
				.setNegativeButton(getString(R.string.exit_cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}

				}).show();
	}

	int index=0;
	private void speak() {
		bFragment = (BaseFragment) adapter.getItem(vPager.getCurrentItem());
		if (bFragment.getDataSize() > index) {
			String text = bFragment.getSpeakText(index++);
			int code = TtsUtil.getInstance(Main.this).startSpeak(text, mTtsListener);
			if (code != ErrorCode.SUCCESS) {
				NetUtil2.CURRENT_SPEAK="";
				if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
					// 未安装则跳转到提示安装页面
					Toast.makeText(Main.this, getString(R.string.tip_speak_not_installed), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(Main.this, getString(R.string.tip_speak_error), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			
		}

		@Override
		public void onSpeakPaused() {
			
		}

		@Override
		public void onSpeakResumed() {
			
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			
		}

		@Override
		public void onCompleted(SpeechError error) {
			NetUtil2.CURRENT_SPEAK = "";
			speak();
		}
	};
}
