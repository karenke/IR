import java.io.IOException;
import java.text.DecimalFormat;
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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.util.Collections;

public class LSI {

	ArrayList<String> stoplist;
	ReadFile rf;
	ArrayList<HashMap<String, LinkedList<Integer>>> list;//get all documents term-location hash map
	HashMap<String, LinkedList<Integer>> doc_freq;//get term-doc hash map
	ArrayList<String> keyList;//the whole list of terms
	public LSI() throws IOException{
		this.rf = new ReadFile();
	    this.stoplist = rf.readStopList();
	    this.list = getAllTermFreq();
	    this.doc_freq = getDocFreq(this.list);
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
	public double getTF(int doc_id, String key, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs){
		double tf = 0.00;
		if(!allDocs.get(doc_id).containsKey(key)){
			return 0;
		}
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

	public double[][] getAllTF_IDF(ArrayList<HashMap<String, LinkedList<Integer>>> allDocs){
		double[][] C = new double[doc_freq.size()][40];
		Iterator itr= doc_freq.keySet().iterator(); 
		int i = 0;
		while(itr.hasNext()) {
			Object tempKey=itr.next(); 
	//		System.out.println("key " + tempKey); 
			for(int j = 0; j < 40; j++){
				double tf = getTF(j,(String) tempKey,allDocs);
				double idf = getIDF(40,(String) tempKey,doc_freq);
				C[i][j] = getTF_IDF(tf,idf);
	//			System.out.print(C[i][j]+" ");
			}
	//		System.out.print("\n");
			i++;
		} 
		return C;
	}
	
	public Matrix createMatrix(double[][] input){
		Matrix C = new Matrix(input); 
		return C;
	}
	
	public SingularValueDecomposition SVD(Matrix C){
		SingularValueDecomposition svd = new SingularValueDecomposition(C);
		return svd;
	}
	
	public Matrix getT(SingularValueDecomposition svd){
		Matrix T = svd.getU();
		return T;
	}

	public Matrix getD(SingularValueDecomposition svd){
		Matrix D = svd.getV();
		return D;
	}
		
	public Matrix getS(SingularValueDecomposition svd){
		Matrix S = svd.getS();
		return S;
	}
	public double[] getSingle(SingularValueDecomposition svd){
		return svd.getSingularValues();
	}
	
	public Matrix getSubT(Matrix T,int k){
		return T.getMatrix(0, T.getRowDimension()-1,0, k-1);
	}
	public Matrix getSubS(Matrix T,int k){
		return T.getMatrix(0, k-1,0, k-1);
	}
	public Matrix getSubD(Matrix T,int k){
		return T.getMatrix(0, k-1,0, T.getColumnDimension()-1);
	}
	
	public String[] getQueryTerm(String str,HashMap<String, LinkedList<Integer>> doc_freq, ArrayList<HashMap<String, LinkedList<Integer>>> allDocs) throws IOException{	
		String testreg = "[^a-zA-Z\\s]";
        Pattern matchsip = Pattern.compile(testreg);
        Matcher mp = matchsip.matcher(str);
        str = mp.replaceAll("");
        StringTokenizer token=new StringTokenizer(str.toLowerCase()," "); 
		int num = token.countTokens();
		String[] terms = new String[num];
		for(int i = 0; i < num; i++){
			terms[i] = token.nextToken();
		}
		return terms;
	}
	
	public ArrayList<Integer> getQueryVector(String[] terms,HashMap<String, LinkedList<Integer>> doc_freq){
		Iterator itr= doc_freq.keySet().iterator(); 
		int i = 0;
		ArrayList<Integer> index = new ArrayList<Integer>();
		while(itr.hasNext()) { 
			Object tempKey=itr.next(); 
	//		System.out.println("key " + tempKey); 
			for(int j = 0; j < terms.length; j++){
				if(terms[j].equals(tempKey)){
					index.add(i);
				}
			}
			i++;	
		} 
		return index;
	}

	public Matrix getQueryV(ArrayList<Integer> index){
		Matrix q = new Matrix(doc_freq.size(),1);
		for(int i = 0; i < index.size(); i++){
			q.set(index.get(i), 0, 1);
		}
		return q;
	}
	
	public Matrix getNewQueryV(Matrix q, Matrix S, Matrix T){
		Matrix S_1 = S.inverse();
		Matrix T_t = T.transpose();
		Matrix newQ = S_1.times(T_t).times(q);
		return newQ;
	}
	
	public Matrix getDocV(Matrix D,int doc_id){
		Matrix e = new Matrix(40,1,0);
		e.set(doc_id, 0, 1);
//		Matrix D_t = D.transpose();
		Matrix docV = D.times(e);
		return docV;
	}
	
	public Matrix[] getAllDocV(Matrix D){
		Matrix[] doc = new Matrix[40];
		for(int i = 0; i < 40; i++){
			doc[i] = getDocV(D,i);
		}
		return doc;
	}
	
	public double[] MtoV(Matrix M){
		double[][] arr = M.getArray();
		double[] r = new double[arr.length];
		for(int i = 0; i < r.length; i++){
			r[i] = arr[i][0];
		}
		return r;
	}
	
	public double sim(double[] vectori, double[] vectorj){//cosine
		DoubleMatrix1D a = new DenseDoubleMatrix1D(vectori);
		DoubleMatrix1D b = new DenseDoubleMatrix1D(vectorj);
		if(a.zDotProduct(a) == 0 || b.zDotProduct(b) == 0){
			return -100;
		}
		return a.zDotProduct(b)/Math.sqrt(a.zDotProduct(a)*b.zDotProduct(b));
	}
	
	public Node[] getAllSim(Matrix q, Matrix[] doc){
		Node[] nodes = new Node[40];
		double[] qV = MtoV(q);
		double[] similarity = new double[40];
		for(int i = 0; i < 40; i++){
			double[] docV = MtoV(doc[i]);
			nodes[i] = new Node(i,sim(qV,docV));
		}
		Arrays.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				// TODO Auto-generated method stub
				return n2.getCosine() < n1.getCosine() ? 0 : 1;
			}	       
		});
		return nodes;
	}
	
	public ArrayList<String> query(ArrayList<HashMap<String, LinkedList<Integer>>> list, int k, String terms) throws IOException{
		double[][] C = getAllTF_IDF(list);
		Matrix m = createMatrix(C);
		Matrix S = getS(SVD(m));
		Matrix T = getT(SVD(m));
		Matrix D = getD(SVD(m));
		Matrix subS = getSubS(S,k);
		Matrix subT = getSubT(T,k);
		Matrix subD = getSubD(D.transpose(),k);
		String[] str = getQueryTerm(terms, doc_freq,list);
		ArrayList<Integer> index = getQueryVector(str,doc_freq);
		Matrix q = getQueryV(index);
		Matrix q2 = getNewQueryV(q, subS, subT);
		Matrix[] docMatrix = getAllDocV(subD);
		Node[] nodes = getAllSim(q2, docMatrix);	
		return query2(nodes);
//		return nodes;
	}
	
	public ArrayList<String> query2(Node[] nodes) throws IOException{
		ArrayList<String> out = new ArrayList<String>();
		String output = "";
		if(nodes != null){
			if(nodes[0].cosine == -100){
				output += "sorry, there is no corresponding result";
				out.add(output);
			}
			else{
				for(int i = 0; i < 5; i++){
					String fileIndex = nodes[i].index < 10 ? "0"+nodes[i].index:""+nodes[i].index;
					DecimalFormat df = new DecimalFormat( "0.## "); 
					output = "File" + fileIndex + ": Score: "+  df.format(nodes[i].cosine*100) + "\n";
					ArrayList<String> s = this.rf.readFile(fileIndex, this.stoplist);
					if(s.size() >= 100){
						for(int j = 0; j < 100; j++){
							output+= s.get(j) + " ";
						}
					}
					else{
						for(int j = 0; j < s.size(); j++){
							output+= s.get(j) + " ";
						}
					}
					output +="\n";
					output +="\n";
					out.add(output);
				}
			}
		}
		return out;
	}
	public static void main(String[] args) throws IOException{
		LSI l = new LSI();
		ArrayList<String> out = l.query(l.list, 10, "graphene");
		for(int i = 0; i < out.size(); i++){
			System.out.println(out.get(i));
		}
	}
}
