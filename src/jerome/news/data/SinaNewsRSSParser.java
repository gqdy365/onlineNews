package jerome.news.data;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jerome.news.util.NetUtil;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.R.integer;
import android.util.Log;

public class SinaNewsRSSParser {

	private static XPath xpath = XPathFactory.newInstance().newXPath();

	private final static String QUERY_EXPR_NODES = "/rss/channel/item";
	private final static String QUERY_EXPR_TITLE = "title/text()";
	private final static String QUERY_EXPR_SOURCE = "source/text()";
	private final static String QUERY_EXPR_URL = "link/text()";
	private final static String QUERY_EXPR_PUB_DATE = "pubDate/text()";
	private final static String QUERY_EXPR_THUMBNAIL = "enclosure/@url";
	private final static String QUERY_EXPR_OUTLINE = "enclosure/@alt";

	/**
	 * 解析 指定url下的 xml结点的信息
	 * */
	public List<NewsBrief> getNewsList(int channelId,String s)
			throws XPathExpressionException {
		List<NewsBrief> mList = new ArrayList<NewsBrief>();
		if (null == s || "".equals(s)) {
			return mList;
		}
		InputSource is = new InputSource(new StringReader(s));
		NodeList nodes = getNodes(is);

		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			NewsBrief brief = new NewsBrief();
			node = nodes.item(i);
			brief.setChannelId(channelId);
			brief.setTitle(getTitle(node));
			brief.setSource(getSource(node));
			brief.setPubDate(NetUtil.dateToString(getPubDate(node)));
			brief.setUrl(getURL(node));
			brief.setImgUrl(getThumbnail(node));
			mList.add(brief);
		}
		return mList;
	}

	private NodeList getNodes(InputSource is) throws XPathExpressionException {
		return (NodeList) xpath.evaluate(QUERY_EXPR_NODES, is,
				XPathConstants.NODESET);
	}

	private String getTitle(Object item) throws XPathExpressionException {
		return xpath.evaluate(QUERY_EXPR_TITLE, item);
	}

	private String getSource(Object item) throws XPathExpressionException {
		return xpath.evaluate(QUERY_EXPR_SOURCE, item);
	}

	private String getURL(Object item) throws XPathExpressionException {
		return xpath.evaluate(QUERY_EXPR_URL, item);
	}

	private String getPubDate(Object item) throws XPathExpressionException {
		return xpath.evaluate(QUERY_EXPR_PUB_DATE, item);
	}

	private String getThumbnail(Object item) throws XPathExpressionException {
		return xpath.evaluate(QUERY_EXPR_THUMBNAIL, item);
	}

	private String getOutline(Object item) throws XPathExpressionException {
		return xpath.evaluate(QUERY_EXPR_OUTLINE, item);
	}
}
