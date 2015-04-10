package jerome.news.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jerome.news.data.NewsBrief;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class NetUtil {
	public static List<NewsBrief> DATALIST = new ArrayList<NewsBrief>();

	public static String[][] CHANNEL_URL = new String[][] {
		new String[]{"http://news.qq.com/world_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/society_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
		new String[]{"http://news.qq.com/china_index.shtml","http://news.qq.com"},
	};

	static String[] ignoreKeys=new String[]{
		"浏览原图",
		"视频下载",
	};
	
	public static String getNewsContent(NewsBrief newsBrief) {
		String result = "";
		try {
			Document doc = Jsoup.connect(newsBrief.getUrl()).get();
			Elements ListDiv = doc.getElementsByAttributeValue("class",
					"articleContent");
			result = ListDiv.html().toString();
			if (result.contains("picbox")) {
//				newsBrief.setImgUrl(NetUtil.parserImgUrl(result));
			}
			String contentString =NetUtil.parserContent(result); 
			newsBrief.setContent(contentString);
			if(contentString.trim().length()==0){
				result = "noting";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getNewsContentByUrl(String url) {
		String result = "";
		try {
			Document doc = Jsoup.connect(url).get();
			Elements ListDiv = doc.getElementsByAttributeValue("class",
					"articleContent");
			result = ListDiv.html().toString();
			String contentString =NetUtil.parserContent(result); 
			result = contentString;
			if(contentString.trim().length()==0){
				result = "noting";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String parserContent(String content) {
		String reslut = "";
		//过滤正文之前的内容
		if(content.contains("<span class=\"imgMessage\">")){
			int index = content.lastIndexOf("<span class=\"imgMessage\">");
			content = content.substring(index, content.length());
		}
		
		content = "<html>"+content+"</html>";
		NodeFilter contentFilter = new TagNameFilter("html");
		try {
			Parser imgParser = new Parser();
			imgParser.setResource(content);
			NodeList imgList = imgParser.extractAllNodesThatMatch(contentFilter);
			
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

	public static String parserImgUrl(String content) {
		String reslut = "";
		NodeFilter contentFilter = new AndFilter(
				new TagNameFilter("div"),
				new HasAttributeFilter("class","picbox"));
		try {
			Parser imgParser = new Parser();
			imgParser.setResource(content);
			NodeList imgList = imgParser.extractAllNodesThatMatch(contentFilter);
			String bodyString = imgList.toHtml();
			if (bodyString.contains("<img") && bodyString.contains("src=")) {
				String imgString = imgList.elementAt(0).toHtml();
				int imglinkstart = imgString.indexOf("src=\"");
				int imglinkend = imgString.indexOf(">");
				if (imgString.contains("\" alt=")) {
					imglinkend = imgString.indexOf("\" alt=");
				}
				reslut = imgString.substring(imglinkstart + 5, imglinkend);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reslut;
	}
	
	public static String dateToString(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.PRC);
		Date tDate = new Date(date);
		String dateString = tDate.toLocaleString();
		try {
			dateString=sdf.format(tDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateString;
	}
}
