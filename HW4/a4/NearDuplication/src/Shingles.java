import java.util.ArrayList;


public class Shingles {

	public int id;
	public String shingle;
	public ArrayList<Integer> docs;
	public Shingles(int id, String shingle, ArrayList<Integer> docs) {
		super();
		this.id = id;
		this.shingle = shingle;
		this.docs = docs;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getShingle() {
		return shingle;
	}
	public void setShingle(String shingle) {
		this.shingle = shingle;
	}
	public ArrayList<Integer> getDocs() {
		return docs;
	}
	public void setDocs(ArrayList<Integer> docs) {
		this.docs = docs;
	}
	
}
