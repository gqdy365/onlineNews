package jerome.news.fragment;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import jerome.news.activity.DetailActivity;
import jerome.news.adapter.DataAdapter;
import jerome.news.adapter.IObtainData;
import jerome.news.data.NewsBrief;
import jerome.news.data.SinaNewsRSSParser;
import jerome.news.data.SinaNewsRestClient;
import jerome.news.util.NetUtil2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import jerome.news.R;

import com.loopj.android.http.AsyncHttpResponseHandler;

public class BaseFragment extends Fragment implements IObtainData{
	private String TAG = "zwq";
	Context mContext = null;
	private int mChannelId = -1;
	private ListView mListView = null;
	DataAdapter mAdapter = null;
	List<NewsBrief> newsData = new ArrayList<NewsBrief>();
	TextView loading = null;
	boolean isFirst = true;

	LinearLayout loadLayout = null;
	Button reload = null;
	NetUtil2 mNetUtil = NetUtil2.getInstance();

	public BaseFragment() {
		mContext = getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arg = getArguments();
		if (null != arg) {
			mChannelId = arg.getInt("id");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fragment, container, false);
		init(view);
		return view;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			mHandler.sendEmptyMessage(0);
			if(isFirst){
				isFirst = false;
				mNetUtil.setObtainDataListener(mChannelId, this);
				loadData();
			}
		} else {
			
		}
	}

	private void init(View view) {
		loadLayout= (LinearLayout) view.findViewById(R.id.fragment_load_layout);
		loading = (TextView) view.findViewById(R.id.fragment_loading);
		reload = (Button) view.findViewById(R.id.fragment_reload);
		mListView = (ListView) view.findViewById(R.id.fragment_list);
		mAdapter = new DataAdapter(getActivity(), newsData);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent(getActivity(), DetailActivity.class);
				String[] value = new String[] {
						newsData.get(position).getTitle(),
						newsData.get(position).getSource(),
						newsData.get(position).getPubDate(),
						newsData.get(position).getUrl(),
						newsData.get(position).getContent(), 
						newsData.get(position).getImgUrl() };
				intent.putExtra("data", value);
				getActivity().startActivity(intent);
			}

		});

		reload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loading.setText(getString(R.string.tip_text_data_loading));
				reload.setVisibility(View.GONE);
				loadData();
			}

		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void loadData(){
		String url = NetUtil2.CHANNEL_URL[mChannelId];
		SinaNewsRestClient.get(url, null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(final String response) {
				new Thread() {
					public void run() {
						SinaNewsRSSParser parser = new SinaNewsRSSParser();
						try {
							Message msg = new Message();
							msg.what = 1;
							msg.obj = parser.getNewsList(mChannelId,response);
							mHandler.sendMessage(msg);
						} catch (XPathExpressionException e) {
							e.printStackTrace();
						}
					}
				}.start();

			}

			@Override
			public void onFailure(Throwable e, String response) {
				e.printStackTrace();
				isFirst = true;
				mHandler.sendEmptyMessage(2);
			}
		});
	}
	
	Handler mHandler = new Handler(){

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				mAdapter.notifyDataSetChanged();
				break;
			case 1:
				newsData.clear();
				newsData.addAll((List<NewsBrief>) msg.obj);
				mAdapter.notifyDataSetChanged();
				loadLayout.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
				mAdapter.notifyDataSetChanged();

				for (int i = 0; i < newsData.size(); i++) {
					NewsBrief newsBrief = newsData.get(i);
					mNetUtil.addNewsBrief(newsBrief);
				}
				break;
			case 2:
				reload.setVisibility(View.VISIBLE);
				if(isAdded()){
					loading.setText(getString(R.string.tip_text_data_fail));
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	public void updateNewsBrief(NewsBrief newsBrief) {
		for (NewsBrief nBrief : newsData) {
			if (nBrief.getUrl().equals(newsBrief.getUrl())) {
				Log.i(TAG, "updateNewsBrief:" + newsBrief.getTitle());
				if (NetUtil2.NOTHING.equals(newsBrief.getContent())) {
					newsData.remove(nBrief);
				} else {
					nBrief.setContent(newsBrief.getContent());
				}
				mHandler.sendEmptyMessage(0);
				break;
			}
		}
	}

	public int getDataSize() {
		return newsData.size();
	}
	
	public String getSpeakText(int index) {
		if (index >= newsData.size()) {
			return "";
		}
		NetUtil2.CURRENT_SPEAK = newsData.get(index).getUrl();
		mHandler.sendEmptyMessage(0);
		String result = newsData.get(index).getTitle()+" "+newsData.get(index).getContent();
		if ("".equals(result)) {
			result = newsData.get(index).getTitle();
		}
		return result;
	}
}
