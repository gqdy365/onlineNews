package jerome.news.adapter;

import jerome.news.fragment.BaseFragment;
import jerome.news.lazy.ImageLoader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragAdapter extends FragmentPagerAdapter {

	private Fragment[] fragments = null;

	private String[] fChannelNames = null;
	private int[] fChannelIds = null;
	ImageLoader mImageLoader=null;

	public FragAdapter(FragmentManager fm, String[] channelNames,int[] ids) {
		super(fm);
		fChannelNames = channelNames;
		fChannelIds = ids;
		fragments = new Fragment[ids.length];
		initFragment();
	}
	
	void initFragment() {
		for (int i = 0; i < fChannelNames.length; i++) {
			BaseFragment fragment = new BaseFragment();
			Bundle value = new Bundle();
			value.putString("url", fChannelNames[i]);
			value.putInt("id", fChannelIds[i]);
			fragment.setArguments(value);
			fragments[i] = fragment;
		}
	}

	@Override
	public Fragment getItem(int position) {
		return fragments[position];
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return fChannelNames[position];
	}

	@Override
	public int getCount() {
		return fragments.length;
	}
}
