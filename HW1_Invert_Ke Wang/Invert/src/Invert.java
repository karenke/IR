import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Invert {
	/**word list
	 * sorted list of terms
	 * random look-up & sequential processing
	 * contain document frequency*/
	
	
	/**Postings file
	 * higher
	 *  file00[2]:215 299
	 *  file18[3]:129 251 344
	 *  ...
	 * */

	ArrayList<String> stoplist;
	ReadFile rf;
	ArrayList<HashMap<String, LinkedList<Integer>>> list;//get all documents term-location hash map
	HashMap<String, LinkedList<Integer>> doc_freq;//get term-doc hash map
	ArrayList<String> keyList;//the whole list of terms
	public Invert() throws IOException{
		this.rf = new ReadFile();
	    this.stoplist = rf.readStopList();
	    this.list = getAllTermFreq();
	    this.doc_freq = getDocFreq(this.list);
	    this.keyList = sortKey(this.doc_freq);
	    ArrayList<ArrayList<String>> split = splitKeyList(this.keyList);

	
	}
	
	// get the term frequency for each document and the index of the arraylist is the document id
	public ArrayList<HashMap<String, LinkedList<Integer>>> getAllTermFreq() throws IOException{
		ArrayList<HashMap<String, LinkedList<Integer>>> list = new ArrayList<HashMap<String, LinkedList<Integer>>>();
		for(int i = 0; i < 40; i++){
			String index = i < 10 ? "0"+String.valueOf(i):""+i;
//			System.out.println("index: " + index);
			list.add(rf.readEachDocument(index,this.stoplist));
		}
		return list;
	}
	
	// get the document frequency for each term in the corpus
	public HashMap<String, LinkedList<Integer>>  getDocFreq(ArrayList<HashMap<String, LinkedList<Integer>>> list){
		HashMap<String, LinkedList<Integer>> doc_freq = new HashMap<String, LinkedList<Integer>>();
		for(int i = 0; i < list.size(); i++){
			Iterator itr= list.get(i).keySet().iterator(); 
			while(itr.hasNext()) { 
				Object key=itr.next(); 
				if(!doc_freq.containsKey(key)){
					LinkedList<Integer> documents = new LinkedList<Integer>();
					documents.add(i);
					doc_freq.put((String) key, documents);
				}
				else{
					LinkedList<Integer> documents = doc_freq.get(key);
					documents.add(i);
					doc_freq.put((String) key,documents);
				}
				
			} 
		}
		return doc_freq;
	}
	
	// sort the terms in alphabetic order in keyList, which will used later for sequential processing
	public ArrayList<String> sortKey(HashMap mp){
		ArrayList<String> keyList = new ArrayList<String>();
		Object[] key =  mp.keySet().toArray();   
        Arrays.sort(key);  
        for (int   i   =   0;   i   <   key.length;   i++)   {   
        	keyList.add((String) key[i]);
   //     	 System.out.println(key[i]);  
        }   
        return keyList;
	}

	public int getNum(HashMap<String, LinkedList<Integer>> mp, String key){//document frequency or term frequency
		int num = mp.get(key).size();
		return num;
	}

	public String getPostingForTerm(String key,HashMap<String, LinkedList<Integer>> doc_freq, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs){
		String posting = key;
		LinkedList<Integer> doc_ids = doc_freq.get(key);//document ids
		posting += "[" + doc_ids.size() + "] ";//document frequency
		for(int i = 0; i < doc_ids.size(); i++){
			LinkedList<Integer> locations = allDocs.get(doc_ids.get(i)).get(key);//locations
			posting +=  doc_ids.get(i);//document id
			posting += "[" + locations.size() + "]: "; // term frequency
			for(int j = 0; j < locations.size(); j++){
				posting += locations.get(j) + " "; // locations
			}
		}
		return posting;		
	}

	// iteratively call getPostingForTerm to get all the postings
	public ArrayList<String> getPostings(ArrayList<String> keys,HashMap<String, LinkedList<Integer>> doc_freq, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs){
		ArrayList<String> postings = new ArrayList<String>();
		for(int i = 0; i < keys.size(); i++){
			postings.add(getPostingForTerm(keys.get(i),doc_freq,allDocs));
		}
		return postings;
		
	}

	// split terms by the initial letter 
	public ArrayList<ArrayList<String>> splitKeyList(ArrayList<String> keys){//for A-Z together 26 list
		ArrayList<ArrayList<String>>split = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < 26; i++){
			split.add(new ArrayList<String>());
		}
		for(int i = 0; i < keys.size(); i++){
			split.get(keys.get(i).charAt(0)-'a').add(keys.get(i));
		}
		return split;
	}

	// write postings to file according to initial letter 
	public void writeFile(ArrayList<String> postings, String fileIndex) throws IOException{
		String filename = "postings/" + fileIndex + ".txt";
		File f = new File(filename);
		OutputStream outStream = new FileOutputStream(f,false);
		for(int i = 0; i < postings.size(); i++){
			outStream.write(postings.get(i).getBytes());
			outStream.write('\r');
			outStream.write('\n');
		}
		outStream.close();
	}

	public double getTF(int doc_id, String key, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs){
		double tf = 0.00;
		int f = allDocs.get(doc_id).get(key).size();
		tf = 1 + Math.log10(f);
		return tf;
	}

	public double getIDF(int TotalDocNum, String key,HashMap<String, LinkedList<Integer>> doc_freq){
		double idf = 0.00;
		int num = doc_freq.get(key).size();
		idf = Math.log10(TotalDocNum/num) + 1;
		return idf;
	}
	
	public double getTF_IDF(double tf, double idf){	
		return tf*idf;
	}

	public ArrayList<String> query(String str,HashMap<String, LinkedList<Integer>> doc_freq, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs) throws IOException{	
		String testreg = "[^a-zA-Z\\s]";
        Pattern matchsip = Pattern.compile(testreg);
        Matcher mp = matchsip.matcher(str);
        str = mp.replaceAll("");
        StringTokenizer token=new StringTokenizer(str.toLowerCase()," "); 
		int num = token.countTokens();
		
		if(num == 1){
			return querySingle(str,doc_freq,allDocs);
		}
		else{
			ArrayList<String> terms = new ArrayList<String>();
			String[] readin = new String[token.countTokens()];
			for( int i = 0; i < readin.length; i++){
				readin[i] = token.nextToken().trim();
				if(!this.stoplist.contains(readin[i])){
					terms.add(readin[i]);
				}
			}
			return queryMultiple(terms,doc_freq,allDocs);
		}
		
	}
	
	// single term
	public ArrayList<String> querySingle(String str,HashMap<String, LinkedList<Integer>> doc_freq, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs) throws IOException{
		LinkedList<Integer> doc_ids = doc_freq.get(str);//document ids
	
		ArrayList<String> out = new ArrayList<String>();
		String output;
		if(this.stoplist.contains(str) ||(!this.keyList.contains(str))){
		//	System.out.println("Sorry, we cannot find the term in the documents!");
			output = "Sorry, we cannot find the term " + str + " in the documents!";
			out.add(output);
			return out;
		}
		double[] tf = new double[doc_ids.size()];
		double idf = getIDF(40,str,doc_freq);
		out.add("term: " + str + "\n");
		for(int i = 0; i < doc_ids.size(); i++){
			output = "";
			tf[i] = getTF(doc_ids.get(i),str,allDocs);
			// first line
			output += "tf: " + tf[i];
			output += ", idf: " + idf;
			output += ", tf.idf:" + getTF_IDF(tf[i],idf);
			output += "\n";
			//doc id and locations
			//second line
			output += "document id: " + doc_ids.get(i);
			LinkedList<Integer> locations =  allDocs.get(doc_ids.get(i)).get(str);
			output += ", locations: ";
			for(int j = 0; j < locations.size(); j++){			
				output += locations.get(j);
				if(j != (locations.size() - 1)){
					output += ", ";
				}
			}
			output += "\n";
			//third line
			int index = locations.get(0);
			String fileIndex = doc_ids.get(i) < 10 ? "0"+String.valueOf(doc_ids.get(i)):""+ doc_ids.get(i);
		
			ArrayList<String> s = this.rf.readFile(fileIndex, this.stoplist);
			if(index < 10){
				for(int j = index; j < index+10; j++){
					output += s.get(j) + " ";
				}
			}
			else{
				for(int j = index - 9; j < index+1; j++){
					output += s.get(j) + " ";
				}
			}		
			output +="\n";
			output +="\n";
			out.add(output);
		}
		return out;
	}
	
	
	// multiple terms
	public ArrayList<String> queryMultiple(ArrayList<String> terms,HashMap<String, LinkedList<Integer>> doc_freq, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs) throws IOException{
		ArrayList<String> out = new ArrayList<String>();
		String output = "";
		for(int i = 0; i < terms.size(); i++){
			out.add(terms.get(i) + " ");
		}
		out.add("\n");
		for(int i = 0; i < terms.size(); i++){
			if(!this.keyList.contains(terms.get(i))){
				output = "Sorry, we cannot find the terms in the documents!";
				out.add(output);
				return out;
			}
		}
		LinkedList<Integer> doc_ids = doc_freq.get(terms.get(0));//document ids
		for(int i = 1; i < terms.size(); i++){
			LinkedList<Integer> doc = doc_freq.get(terms.get(i));
			if(doc != null){
			doc_ids.retainAll(doc);
			}
		}
//		for(int i = 0; i < doc_ids.size(); i++){
//			System.out.println(doc_ids.get(i));
//		}
		ArrayList<Node> nodes = new ArrayList<Node>();
		if(doc_ids.size()>0){
			double[] sum = new double[doc_ids.size()];
			double tf = 0.00;
			double idf = 0.00;
			for(int i = 0; i < doc_ids.size();i++){
				for(int j = 0; j < terms.size(); j++){
					idf = getIDF(40,terms.get(j),doc_freq);
					tf = getTF(doc_ids.get(i),terms.get(j),allDocs);
					sum[i] += getTF_IDF(tf,idf);
				}		
				nodes.add(new Node(i, sum[i]));
			}
	
		    Collections.sort(nodes, new Comparator<Node>() {
				@Override
				public int compare(Node n1, Node n2) {
					// TODO Auto-generated method stub
					return n2.getSum() < n1.getSum() ? 0 : 1;
				}	       
		    });

		    for (Node n : nodes) {
		    	output += "document id: " + n.getId();
		    	output += ", sum of tf.idf:" + n.getSum();
		    	output += "\n";    	
	//	        System.out.println(n.getId() + " " + n.getSum());
		    }		
		    out.add(output);
		}
		else{
			output = "Sorry, we cannot find the terms in the documents!";
			out.add(output);
		//	System.out.println("Sorry, we cannot find the terms in the documents!");
		}
		return out;
	}
}
