package jerome.news.title;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import jerome.news.R;

public class TitleView extends TextView {
	private Context mContext;
	private int index;

	public TitleView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	private void initView() {
		setTextColor(Color.WHITE);
		setTextSize(16);
		setPadding(20, 0, 20, 2);
	}

	public void setSelectedState(int flag) {
		if (flag == 1) {
			setTextColor(Color.RED);
			Drawable index = mContext.getResources().getDrawable(
					R.drawable.title_selected);
			setCompoundDrawablesWithIntrinsicBounds(null, null, null, index);
		} else {
			setTextColor(Color.WHITE);
			setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
