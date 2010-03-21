package classify;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SearchResult {
	protected String clickurl;
	protected String date;
	protected String abstractTxt;
	protected long size;
	protected String title;
	protected String url;

	SearchResult(String abstractTxt, String clickurl, String date, String size,
			String title, String url) {
		this.abstractTxt = abstractTxt;
		this.clickurl = clickurl;
		this.date = date;
		this.size = Long.parseLong(size);
		this.title = title;
		this.url = url;
		System.out.println("abstract:" + abstractTxt);
		System.out.println("clickurl:" + clickurl);
		System.out.println("date:" + date);
		System.out.println("size:" + size);
		System.out.println("title:" + title);
		System.out.println("url:" + url);
	}

	/**
	 * Construct SearchResult with result node
	 * 
	 * @param node
	 *            <result> node of search result
	 */
	SearchResult(Node node) {
		Node tempInnerNode;
		NodeList tempList = node.getChildNodes();
		System.out.println("------------------------------------------");
		for (int j = 0; j < tempList.getLength(); j++) {
			tempInnerNode = tempList.item(j);
			if (!tempInnerNode.getNodeName().equalsIgnoreCase("#text")) {
				// get title if node name is 'title'
				if (tempInnerNode.getNodeName().equalsIgnoreCase("title")) {
					this.title = textParse(tempInnerNode.getTextContent());
				}
				// get abstract if node name is 'abstract'
				else if (tempInnerNode.getNodeName().equalsIgnoreCase(
						"abstract")) {
					this.abstractTxt = textParse(tempInnerNode.getTextContent());
				} else if (tempInnerNode.getNodeName().equalsIgnoreCase("size")) {
					this.size = Long.parseLong(textParse(tempInnerNode
							.getTextContent()));
				} else if (tempInnerNode.getNodeName().equalsIgnoreCase("url")) {
					this.url = textParse(tempInnerNode.getTextContent());
				} else if (tempInnerNode.getNodeName().equalsIgnoreCase(
						"clickurl")) {
					this.clickurl = textParse(tempInnerNode.getTextContent());
				} else if (tempInnerNode.getNodeName().equalsIgnoreCase("date")) {
					this.date = textParse(tempInnerNode.getTextContent());
				}
				System.out.println(tempInnerNode.getNodeName() + ":"
						+ textParse(tempInnerNode.getTextContent()));
			}
		}
	}

	protected boolean hasWord(String word) {
		if (abstractTxt.toLowerCase().contains(word))
			return true;
		else
			return false;
	}

	/**
	 *textParse responsible for parsing XML content to eliminate <b> html tag
	 * from content
	 * 
	 * @param text
	 *            the text content of each tag in the response XML
	 */
	private String textParse(String text) {
		text = text.replaceAll("\\<[^>]*>", "");
		return text;
	}

}
