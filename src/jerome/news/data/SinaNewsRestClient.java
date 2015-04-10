package jerome.news.data;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SinaNewsRestClient {
	private static final String BASE_URL = "http://data.3g.sina.com.cn/api/";
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void get(String url, RequestParams params,
			BinaryHttpResponseHandler responseHandler) {
		client.get(url, params, responseHandler);
	}

	public static String getAbsoluteUrl(String relativeUrl) {
		Log.i("zwq", ""+BASE_URL + relativeUrl);
		return BASE_URL + relativeUrl;
	}
}
