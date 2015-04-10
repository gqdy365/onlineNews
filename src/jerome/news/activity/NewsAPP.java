package jerome.news.activity;

import jerome.news.lazy.ImageLoader;
import jerome.news.util.TtsUtil;
import android.app.Application;
import jerome.news.R;

import com.iflytek.cloud.SpeechUtility;

public class NewsAPP extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SpeechUtility.createUtility(getApplicationContext(), "appid="+getString(R.string.app_id));
		ImageLoader.getInstance(getApplicationContext());
		TtsUtil.getInstance(getApplicationContext());
	}

}
