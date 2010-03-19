import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.Scanner;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class ClassifyMe {
	
	private static String appId = "Iu5udbvV34Fcg3uDwfJMTEY8Lb09.yMmFTaf7axWid3g4LmEN3G3iBUs6pa6jrRE";
	private String databaseURL;
	private double specificity;
	private int coverage;
	private Vector<Category> classification;
	private Category root;
	
	public ClassifyMe() {
		
		root = new Category();
		root.name = "Root";
		root.specificity = 1;
		root.subcategories = new Vector<Category>();
		
		Category hardware = new Category();
		hardware.name = "Hardware";
		
		Category programming = new Category();
		programming.name = "Programming";

		Category computers = new Category();
		computers.name = "Computers";
		computers.subcategories = new Vector<Category>();
		computers.subcategories.add(hardware);
		computers.subcategories.add(programming);
		root.subcategories.add(computers);
		
		Category fitness = new Category();
		fitness.name = "Fitness";
		
		Category diseases = new Category();
		diseases.name = "Diseases";
		
		Category health = new Category();
		health.name = "Health";
		health.subcategories = new Vector<Category>();
		health.subcategories.add(fitness);
		health.subcategories.add(diseases);
		root.subcategories.add(health);
		
		Category basketball = new Category();
		basketball.name = "Basketball";
		
		Category soccer = new Category();
		soccer.name = "Soccer";
		
		Category sports = new Category();
		sports.name = "Sports";
		sports.subcategories = new Vector<Category>();
		sports.subcategories.add(basketball);
		sports.subcategories.add(soccer);
		root.subcategories.add(sports);
		
		classification = new Vector<Category>();
		
//		root.printTree();
	}
	
	private Vector<Category> classify(Category c) {
		Vector<Category> catList = new Vector<Category>();
		try {
			String categoryName = c.name;
			if (c.subcategories == null) {
//				System.out.println("This category is a leaf");
				catList.add(c);
				return catList;
			}
			
			c.coverage = 0;
			for (int j = 0; j < c.subcategories.size(); j++) {
				c.subcategories.elementAt(j).coverage = 0;
			}
			BufferedReader input = new BufferedReader(
					new FileReader(categoryName.toLowerCase() +".txt"));
			String line = null;
			try {
				int index, count;
				String subcatName;
				String probingQuery;
				while ((line = input.readLine()) != null) {
					index = line.indexOf((int)' ');
					if (index < 0) {
						System.err.println("No space found. Error parsing a line containg category and probing query");
						index = 0;
					}
					subcatName = line.substring(0 , index);
					probingQuery = line.substring(index+1);
					System.out.println("name:" + subcatName + " query:" + probingQuery);
					count = probe(probingQuery);
//					System.out.println(count);
					c.coverage += count;
					for (int j = 0; j < c.subcategories.size(); j++) {
						if (c.subcategories.elementAt(j).name.equals(subcatName)) {
							c.subcategories.elementAt(j).coverage += count;
						}
					}
					
				}
				System.out.println(categoryName + " coverage:" + c.coverage);
				for (int j = 0; j < c.subcategories.size(); j++) {
					System.out.println(c.subcategories.elementAt(j).name + " coverage:" + c.subcategories.elementAt(j).coverage);
					c.subcategories.elementAt(j).specificity = (c.specificity) * (c.subcategories.elementAt(j).coverage) / c.coverage;
					System.out.println(c.subcategories.elementAt(j).name + " specificity:" + c.subcategories.elementAt(j).specificity);
					if (c.subcategories.elementAt(j).specificity > specificity && c.subcategories.elementAt(j).coverage > coverage) {
						catList.addAll(classify(c.subcategories.elementAt(j)));
					}
					
				}
				if (catList.size() == 0) {
					catList.add(c);
				} else {
					catList.addAll(catList);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return catList;
	}
	
	public int probe(String query) {
		// replace all spaces with "%20" to get url friendly query
		String urlQuery = query.replaceAll(" ", "%20");
		URL url;
		try {
			url = new URL("http://boss.yahooapis.com/ysearch/web/v1/'"
					+ urlQuery + "'?appid=" + appId + "&format=xml&sites=" + databaseURL);
			URLConnection con = url.openConnection();
			InputStream inStream = con.getInputStream();
			Scanner in = new Scanner(inStream);
			StringBuffer temp = new StringBuffer();
			while (in.hasNextLine()) {
				temp.append(in.nextLine());
			}
			String res = temp.toString();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(
					res)));
			NodeList nodeL = doc.getElementsByTagName("resultset_web");
			Node node = nodeL.item(0);
			NamedNodeMap nodeMap = node.getAttributes();
			node = nodeMap.getNamedItem("totalhits");
			return Integer.parseInt(node.getTextContent());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("Usage: ClassifyMe <database-url> <specificity> <coverage> <yahoo appId>");
			System.exit(1);
		}
		ClassifyMe cm = new ClassifyMe();
		cm.databaseURL = args[0];
		cm.specificity = Double.parseDouble(args[1]);
		cm.coverage = Integer.parseInt(args[2]);
		cm.classification = cm.classify(cm.root);
		for (int k=0; k < cm.classification.size(); k++) {
			System.out.println(cm.classification.elementAt(k).name);
		}
//		System.out.println(cm.probe("heart cancer"));
	}
}
