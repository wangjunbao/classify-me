package classify;
import java.io.*;
import java.util.Vector;

public class Category {
	public String  name;
	public double specificity;
	public int coverage;
	public Category parent;
	public Vector<Category> subcategories;
	public Category() {
		name = "";
		specificity = 0;
		coverage = 0;
		parent = null;
//		subcategories = new Vector<Category>();
	}
	
	public void printTree() {
		System.out.println("---:" + name);
		if (subcategories == null) return;
		System.out.println("-: ");
		for (int i=0; i < subcategories.size(); i++) {
			subcategories.elementAt(i).printTree();
		}
		System.out.println("-: ");
	}
}
