package jerome.news.adapter;

import java.util.ArrayList;
import java.util.List;

import jerome.news.data.NewsBrief;
import jerome.news.lazy.ImageLoader2;
import jerome.news.util.NetUtil2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import jerome.news.R;

public class DataAdapter extends BaseAdapter {
	Context mContext = null;
	LayoutInflater inflater;
	List<NewsBrief> newsData = new ArrayList<NewsBrief>();

	public DataAdapter(Context context, List<NewsBrief> nList) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		newsData = nList;
	}

	@Override
	public int getCount() {
		return newsData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView hView = null;
		if (null == convertView) {
			hView = new HolderView();
			convertView = inflater.inflate(R.layout.list_item, null);
			hView.image = (ImageView)convertView.findViewById(R.id.news_image);
			hView.speak = (ImageView)convertView.findViewById(R.id.news_speak);
			hView.title = (TextView)convertView.findViewById(R.id.news_title);
			hView.brief = (TextView)convertView.findViewById(R.id.news_brief);
			hView.where = (TextView)convertView.findViewById(R.id.news_where);
			hView.date = (TextView)convertView.findViewById(R.id.news_time);
			convertView.setTag(hView);
		} else {
			hView = (HolderView) convertView.getTag();
		}
		
		hView.title.setText(newsData.get(position).getTitle());
		if(!"".equals(newsData.get(position).getContent()) && newsData.get(position).getContent().length()>26){
			String str=newsData.get(position).getContent().trim().substring(0, 24);
			hView.brief.setText(NetUtil2.replaceBlank(str));
		} else {
			hView.brief.setText("");
		}
		
		if (NetUtil2.CURRENT_SPEAK.equals(newsData.get(position).getUrl())) {
			hView.speak.setVisibility(View.VISIBLE);
			hView.title.setTextColor(Color.RED);
		} else {
			hView.speak.setVisibility(View.GONE);
			hView.title.setTextColor(Color.WHITE);
		}
		
		hView.where.setText(newsData.get(position).getSource());
		hView.date.setText(newsData.get(position).getPubDate());

		if (null != newsData.get(position).getImgUrl()
				&& !"".equals(newsData.get(position).getImgUrl())) {
			hView.image.setTag(newsData.get(position).getImgUrl());
			ImageLoader2.getInstance(mContext).loadImage(newsData.get(position).getImgUrl(), hView.image);
		} else {
			hView.image.setImageResource(R.drawable.icon_image_default);
		}
		
		return convertView;
	}

	public class HolderView {
		private ImageView image = null;
		private ImageView speak = null;
		private TextView title = null;
		private TextView brief = null;
		private TextView where = null;
		private TextView date = null;
	}
	
}
