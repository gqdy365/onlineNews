package jerome.news.activity;

import jerome.news.util.NetUtil;
import jerome.news.util.TtsUtil;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import jerome.news.R;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

public class DetailActivity extends Activity {

	TextView mTitle = null;
	TextView mWhere = null;
	TextView mContent = null;
	ImageView mImage = null;
	Button mSpeak = null;
	private String contentStr="";
	private String urlStr="";
	private boolean isSpeak = false;
	
	ScrollView mScrollView = null;
	Button reload = null;
	TextView loadTip = null;
	LinearLayout detailLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.detail);

		init();
		
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			final String[] values = bundle.getStringArray("data");
			mTitle.setText(values[0]);
			mWhere.setText(values[1] + " " + values[2]);
			mContent.setText(values[4]);
			urlStr = values[3];
			if ("".equals(values[4].trim()) && !"".equals(values[3])) {
				reloadData();
			} else {
				contentStr = values[4];
				mScrollView.setVisibility(View.VISIBLE);
				detailLayout.setVisibility(View.GONE);
			}
		}
	}
	
	private void reloadData() {
		new Thread() {
			public void run() {
				Message msg = new Message();
				msg.what = 0;
				msg.obj = NetUtil.getNewsContentByUrl(urlStr);
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				String textString = (String) msg.obj;
				if (null != textString && !"".equals(textString)) {
					contentStr = textString;
					mContent.setText(textString);
					mScrollView.setVisibility(View.VISIBLE);
					detailLayout.setVisibility(View.GONE);
				} else {
					mScrollView.setVisibility(View.GONE);
					detailLayout.setVisibility(View.VISIBLE);
					loadTip.setText(getString(R.string.tip_text_data_fail));
					reload.setVisibility(View.VISIBLE);
				}
				break;
			}
		}
	};
	
	private void init(){
		mTitle = (TextView) findViewById(R.id.detail_title);
		mWhere = (TextView) findViewById(R.id.detail_where_time);
		mContent = (TextView) findViewById(R.id.detail_content);
		mSpeak = (Button) findViewById(R.id.detail_speak);
		mSpeak.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TtsUtil.getInstance(DetailActivity.this).stopSpeak();
				speak();
			}

		});

		loadTip = (TextView) findViewById(R.id.detail_loading);
		mScrollView = (ScrollView) findViewById(R.id.main_scroll);
		reload = (Button) findViewById(R.id.detail_reload);
		detailLayout = (LinearLayout) findViewById(R.id.detail_load_layout);
		reload.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				mScrollView.setVisibility(View.GONE);
				detailLayout.setVisibility(View.VISIBLE);
				loadTip.setText(getString(R.string.tip_text_data_loading));
				reload.setVisibility(View.GONE);
				reloadData();
			}
			
		});
	}

	@Override
	protected void onDestroy() {
		if (isSpeak) {
			TtsUtil.getInstance(this).stopSpeak();
		}
		super.onDestroy();
	}

	private void speak() {
		isSpeak = true;
		int code = TtsUtil.getInstance(this).startSpeak(contentStr, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
				// 未安装则跳转到提示安装页面
				Toast.makeText(DetailActivity.this, getString(R.string.tip_speak_not_installed), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DetailActivity.this, getString(R.string.tip_speak_error), Toast.LENGTH_SHORT).show();
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
			
		}
	};
}
