package jerome.news.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jerome.news.adapter.IObtainData;
import jerome.news.data.NewsBrief;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class NetUtil2 extends Thread {

	public static final String NOTHING = "nothing";
	public static String CURRENT_SPEAK = "";
	
	private List<NewsBrief> loopList = new ArrayList<NewsBrief>();
	private boolean isClose = false;
	private Map<Integer, IObtainData> obtainMap = new HashMap<Integer, IObtainData>();
	private int currentPage = 0;
	private boolean isPause = false;

	public static final String[] CHANNEL_URL = new String[] {
			//"combine.php?wm=b207&version=1&rank=auto&from=6028195012&chwm=5062_0001&uid=",//汽车频道，无数据，无法使用
			"t/operating/api.php?wm=b207&version=2&type=news&from=6028195012&chwm=5062_0001&uid=",// 头条新闻
			"combine.php?wm=b207&version=1&cid=2&channel=gn&from=6028195012&chwm=5062_0001&uid=",// 国内频道
			"combine.php?wm=b207&version=1&cid=1&channel=gj&from=6028195012&chwm=5062_0001&uid=",// 国际频道
			"combine.php?wm=b207&version=1&rank=finance&channel=finance&from=6028195012&chwm=5062_0001&uid=",// 财经频道
			"combine.php?wm=b207&version=1&rank=tech&channel=tech&from=6028195012&chwm=5062_0001&uid=",// 科技频道
			"combine.php?wm=b207&version=1&cid=3&channel=sh&from=6028195012&chwm=5062_0001&uid=",// 社会频道
			"combine.php?wm=b207&version=1&rank=sports&channel=sports&from=6028195012&chwm=5062_0001&uid=",// 体育频道
//			"combine.php?wm=b207&version=1&rank=ent&channel=ent&from=6028195012&chwm=5062_0001&uid=",// 娱乐频道
	};

	private NetUtil2() {
	}

	private static NetUtil2 single = null;

	public synchronized static NetUtil2 getInstance() {
		if (single == null) {
			single = new NetUtil2();
		}
		return single;
	}

	public synchronized void addNewsBrief(NewsBrief newsBrief) {
		loopList.add(newsBrief);
		this.notify();
	}

	public void setObtainDataListener(int channelId, IObtainData iod) {
		if (!obtainMap.containsKey(channelId)) {
			obtainMap.put(channelId, iod);
		}
	}

	public void removeObtainDataListener() {
		obtainMap.clear();
	}
	
	public void setCurrentPage(int page) {
		currentPage = page;
	}

	public synchronized void onThreadPause() {
		isPause = true;
	}

	public void onThreadWait() {
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public synchronized void onThreadResume() {
		isPause = false;
		this.notify();
	}

	public synchronized void closeThread() {
		try {
			notify();
			setClose(true);
			interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isClose() {
		return isClose;
	}

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	@Override
	public void run() {
		while (!isClose && !isInterrupted()) {
			if (loopList.size() > 0 && !isPause) {
				int index = 0;
				NewsBrief newsBrief = null;
				// 当前页面优先加载
				for (int i = 0; i < loopList.size(); i++) {
					if (loopList.get(i).getChannelId() == currentPage) {
						newsBrief = loopList.get(i);
						index = i;
						break;
					}
				}
				if (null == newsBrief) {
					newsBrief = loopList.get(0);
				}
				String reslut = getNewsContent(newsBrief);

				if (!"-1".equals(reslut)) {
					// 获取成功,更新
					if (obtainMap.containsKey(newsBrief.getChannelId())) {
						obtainMap.get(newsBrief.getChannelId()).updateNewsBrief(newsBrief);
					}
					synchronized (loopList) {
						loopList.remove(index);
					}
				} else {
					//获取失败，移至队尾
					synchronized (loopList) {
						NewsBrief nb = loopList.remove(index);
						loopList.add(nb);
					}
				}
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				onThreadWait();
			}
		}
	}

	static String[] ignoreKeys = new String[] { "浏览原图", "视频下载", };

	private String getNewsContent(NewsBrief newsBrief) {
		String result = "";
		try {
			Document doc = Jsoup.connect(newsBrief.getUrl()).ignoreContentType(true).get();
			Elements ListDiv = doc.getElementsByAttributeValue("class",
					"articleContent");
			result = ListDiv.html().toString();

			// 获取文字图片集
			if (result.contains("picbox")) {
				// newsBrief.setImgUrl(NetUtil.parserImgUrl(result));
			}
			String contentString = parserContent(result);
			if (contentString.trim().length() == 0) {
				result = NOTHING;
				contentString = NOTHING;
			}
			newsBrief.setContent(contentString);
		} catch (IOException e) {
			result = "-1";
			e.printStackTrace();
		}
		return result;
	}

	public String getNewsContentByUrl(String url) {
		String result = "";
		try {
			Document doc = Jsoup.connect(url).get();
			Elements ListDiv = doc.getElementsByAttributeValue("class",
					"articleContent");
			result = ListDiv.html().toString();
			String contentString = parserContent(result);
			result = contentString;
			if (contentString.trim().length() == 0) {
				result = NOTHING;
			}
		} catch (IOException e) {
			result = "-1";
			e.printStackTrace();
		}
		return result;
	}

	private String parserContent(String content) {
		String reslut = "";
		// 过滤正文之前的内容
		if (content.contains("<span class=\"imgMessage\">")) {
			int index = content.lastIndexOf("<span class=\"imgMessage\">");
			content = content.substring(index, content.length());
		}

		content = "<html>" + content + "</html>";
		NodeFilter contentFilter = new TagNameFilter("html");
		try {
			Parser imgParser = new Parser();
			imgParser.setResource(content);
			NodeList imgList = imgParser
					.extractAllNodesThatMatch(contentFilter);

			reslut = imgList.asString();
			for (int i = 0; i < ignoreKeys.length; i++) {
				if (reslut.contains(ignoreKeys[i])) {
					int index = reslut.indexOf(ignoreKeys[i]);
					reslut = reslut.substring(index + 8, reslut.length());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reslut;
	}

	public static String dateToString(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				Locale.PRC);
		Date tDate = new Date(date);
		String dateString = tDate.toLocaleString();
		try {
			dateString = sdf.format(tDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateString;
	}
	
	public static String replaceBlank(String str) {
		String dest = "";
		if (str!=null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}
}
