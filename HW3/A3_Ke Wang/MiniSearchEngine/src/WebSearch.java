import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Jama.Matrix;



public class WebSearch {
	
	HashMap<String, Integer> pages;
	double[][] matrix = new double[216][216];
	double d;
	HashMap<String,ArrayList<String>> anchors;
	double[] vector;
	ArrayList<String> allAnchorText;
	
	public static void main(String[] args) throws IOException{
		WebSearch ws = new WebSearch();
	}
	public WebSearch() throws NumberFormatException, IOException{
		this.pages = readFile("http://www.infosci.cornell.edu/Courses/info4300/2012fa/test/test3.txt");
		this.d = 0.85;
		this.anchors = new HashMap<String,ArrayList<String>>();
		this.allAnchorText = new ArrayList<String>();
		getTitle();
		this.vector = getPageRank();

		writeFile(this.pages);
	}
	
	public double[] getPageRank() throws IOException{
		setAll(this.matrix, this.pages);
		normalize(this.matrix);
		solveDeadEnd(this.matrix);
		
		Matrix B = getB(this.matrix);
		
		
		Matrix R = getR();
		Matrix G = getG(this.d, B, R);
		Matrix W1 = getW();
		Matrix W = null;
		int count = 0;
		while(true){
			count++;
			W = G.times(W1);
			if(isEqual(W1, W)){
				break;
			}
			else{
				W1 = W;
			}
		}
		double[] v = MtoV(W);
		System.out.println(count);
		return v;
	}
	
	
	public ArrayList<Map.Entry<String, Double>> Query(String str){
		ArrayList<String> query = processQuery(str);
		int[] q = setQueryVector(this.allAnchorText,query);
		HashMap<String,int[]> incidences = setAllIncidence(this.anchors,this.allAnchorText);
		HashMap<String, Double> similarity = new HashMap<String, Double>();
		Iterator vitr = incidences.keySet().iterator();
		while(vitr.hasNext()){
			String key = (String) vitr.next();
			Double simi = sim(incidences.get(key),q);
	//		simi += this.vector[this.pages.get(key)];//page rank
			similarity.put(key, simi);
		}
		ArrayList<Map.Entry<String, Double>> mp = new ArrayList<Map.Entry<String, Double>>();//double is simi
        Iterator itr = similarity.entrySet().iterator();
        while(itr.hasNext()){
        	mp.add((Entry<String, Double>) itr.next());
        }
        Collections.sort(mp, new Comparator<Map.Entry<String, Double>>(){

			@Override
			public int compare(Entry<String, Double> a1,
					Entry<String, Double> a2) {
				// TODO Auto-generated method stub
				return a2.getValue() < a1.getValue() ? 0 : 1;
			}
        	
        });
        
        HashMap<String,Double> temp = new HashMap<String,Double>();
        ArrayList<Map.Entry<String, Double>> ret = new ArrayList<Map.Entry<String, Double>>();
        
        for(int i = 0; i < 10; i++){
        	String key = mp.get(i).getKey();
        	System.out.println(mp.get(i).getKey() + " " + mp.get(i).getValue());
        	double rank = this.vector[this.pages.get(key)];
        	temp.put(key, rank);
        }
        Iterator itr2 = temp.entrySet().iterator();
        while(itr2.hasNext()){
        	ret.add((Entry<String, Double>) itr2.next());
        }
        Collections.sort(ret, new Comparator<Map.Entry<String, Double>>(){

			@Override
			public int compare(Entry<String, Double> a1,
					Entry<String, Double> a2) {
				// TODO Auto-generated method stub
				return a2.getValue() < a1.getValue() ? 0 : 1;
			}
        	
        });
        System.out.println("here");
      for(int i = 0; i < ret.size(); i++){
    	System.out.println(ret.get(i).getKey()+ret.get(i).getValue());
      }
        return ret;

 
	}
	
	public double sim(int[] v, int[] q){
		int vNum = 0;
		int qNum = 0;
		int product = 0;
		for(int i = 0; i < v.length; i++){
			product += v[i]*q[i];
			if(v[i] == 1){
				vNum++;
			}
			if(q[i] == 1){
				qNum++;
			}
		}
		
		if(vNum == 0 || qNum == 0){
			return 0;
		}
		return (double)product/((Math.sqrt(vNum))*Math.sqrt(qNum));
	
	}
	
	public  ArrayList<String> processQuery(String str){
//		String testreg = "[^a-zA-Z\\s]";
//        Pattern matchsip = Pattern.compile(testreg);
//        Matcher mp = matchsip.matcher(str);
//        str = mp.replaceAll("");
		StringTokenizer token=new StringTokenizer(str.toLowerCase()," "); 
		ArrayList<String> query = new ArrayList<String>();
		int len = token.countTokens();
		if(len > 0){
			for( int i = 0; i < len; i++){
				String s = token.nextToken().trim();
				if(!query.contains(s)){
					query.add(s);
				}
			}
		}
		
		return query;
	}
	
	public int[] setQueryVector(ArrayList<String> terms, ArrayList<String> query){
		int[] q = new int[terms.size()];
		if(query.size() > 0){
			for(int i = 0; i < query.size(); i++){
				int col = terms.indexOf(query.get(i));
				if(col >= 0){
					q[col] = 1;
				}
			}
		}
		
		return q;
	}
	
	public HashMap<String,int[]> setAllIncidence(HashMap<String,ArrayList<String>> anchorhp, ArrayList<String> terms){
		HashMap<String,int[]> incidence = new HashMap<String,int[]>();
		Iterator itr = anchorhp.keySet().iterator();
		while(itr.hasNext()){
			String url = (String) itr.next();
			int[] v = setIncidenceVector(url,anchorhp,terms);
			incidence.put(url, v);
		}
		return incidence;
	}
	
	public int[] setIncidenceVector(String url, HashMap<String,ArrayList<String>> anchorhp, ArrayList<String> terms){
		int[] incidence = new int[terms.size()];
		ArrayList<String> list = anchorhp.get(url);
		if(list.size() > 1){// the first is title
			for(int i = 1; i < list.size(); i++){
	//			System.out.println(list.get(i).toLowerCase());
				ArrayList<String> query = processQuery(list.get(i).toLowerCase());
				if(query.size() > 0){
					 for(int j = 0; j < query.size(); j++){
						int col = terms.indexOf(query.get(j)); 
						incidence[col] = 1;
					 }	 
				}
				
				
			}
		}
		return incidence;
	}
	
	public void writeFile(HashMap<String, Integer> hp) throws IOException{
		
		ArrayList<Map.Entry<String, Integer>> mp = new ArrayList<Map.Entry<String, Integer>>();
        Iterator itr = hp.entrySet().iterator();
        while(itr.hasNext()){
        	mp.add((Entry<String, Integer>) itr.next());
        }
        Collections.sort(mp, new Comparator<Map.Entry<String, Integer>>(){

			@Override
			public int compare(Entry<String, Integer> a1,
					Entry<String, Integer> a2) {
				// TODO Auto-generated method stub
				return a1.getValue() - a2.getValue();
			}
        	
        });
        
		String filename = "metadata.txt";
		File f = new File(filename);
		OutputStream outStream = new FileOutputStream(f,false);
		int size = 216;
		for(int i = 0; i < size; i++){
			outStream.write("pageID: ".getBytes());
			int pageId = mp.get(i).getValue() + 1;
			outStream.write((""+pageId).getBytes());
			outStream.write(" pageRank: ".getBytes());
			outStream.write(Double.toString(this.vector[i]).getBytes());
			String url = mp.get(i).getKey();
			ArrayList<String> a = this.anchors.get(url);
			outStream.write(" title: ".getBytes());
			if(a.size() != 0 ){
				outStream.write(a.get(0).getBytes());
			}
			if(a.size() > 1){
				outStream.write(", Anchors: ".getBytes());
			}
			for(int j = 1; j < a.size(); j++){
				outStream.write(a.get(j).getBytes());
				if(j != a.size()-1){
					outStream.write(",".getBytes());
				}
			}
			
			outStream.write('\r');
			outStream.write('\n');
		}
		outStream.close();
	}
	
	public boolean isEqual(Matrix m1, Matrix m2){
		double[] v1 = MtoV(m1);
		double[] v2 = MtoV(m2);
		for(int i = 0; i < v1.length; i++){
			if(Math.abs(v1[i] - v2[i]) > 0.0001){
				return false;
			}
		}
		return true;
	}

	public Matrix getW(){
		int size = 216;
		double[][] w = new double[size][1];
		for(int i = 0; i < size; i++){
			w[i][0] = 1.0;
		}
		return createMatrix(w);
		
	}
	
	public double[] MtoV(Matrix M){
		double[][] arr = M.getArray();
		double[] r = new double[arr.length];
		for(int i = 0; i < r.length; i++){
			r[i] = arr[i][0];
		}
		return r;
	}
	
	public Matrix getR(){
		int size = 216;
		double[][] rArr = new double[size][size];
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				rArr[i][j] = (double)1/size;
			}
		}
		Matrix R = createMatrix(rArr);
		return R;
	}
	public Matrix getG(double d, Matrix B, Matrix R){
		Matrix G = B.times(d).plus(R.times(1-d));
		return G;
	}
	
	public Matrix getB(double[][] arr){
		return createMatrix(arr);
	}
	
	public Matrix createMatrix(double[][] input){
		Matrix C = new Matrix(input); 
		return C;
	}
	public void setAll(double[][] arr,HashMap<String, Integer> hp) throws IOException{
		Iterator itr = hp.keySet().iterator();
		while(itr.hasNext()){
			String key = (String) itr.next();
			setMatrix(key,arr,hp);
		}
		
	}
	
	public void normalize(double[][] arr){
		int size = 216;
		int[] num = new int[size];// per col
		int count=0;
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				num[i] += arr[j][i];
				if(arr[j][i]!=0){
					count++;
				}
			}
		}
		System.out.println(count);
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(num[i] != 0){
					arr[j][i] /= num[i];
				}
			}
		}
	}
	public void solveDeadEnd(double[][] arr){
		int size = 216;
		for(int i = 0; i < size; i++){
			if(isDeadEnd(i,arr)){
				for(int j = 0; j < size; j++){
					arr[j][i] = 1.0/size;
				}
			}
		}
	}
	
	public boolean isDeadEnd(int col, double[][] arr){
//		for(int i = 0; i < 217; i++){
		for(int i = 0; i < 216; i++){
			if(arr[i][col] != 0){
				return false;
			}
		}
		return true;
	}
	public void setMatrix(String url, double[][] arr,HashMap<String, Integer> hp) throws IOException{
		int index = hp.get(url);
		if(index >= 116){
			index--;
		}
	//	System.out.println("index: " + index);
		Elements links = getHyperLinks(url);
		if(links != null){
			for (Element link: links) { 
//				if(index == 58){
//					System.out.println(link.attr("href"));
//				}
				String absLink = "";
				if(link.attr("href").startsWith("/")){
		//			System.out.println(link.attr("href"));
					absLink = "http://www.library.cornell.edu" + link.attr("href");
				}
				else{
					absLink = link.absUrl("href");
				}
				if(absLink.indexOf('#') >= 0){
					absLink = absLink.substring(0,absLink.indexOf('#'));
				}
				
				if(!absLink.equals("")){
					String str = "";
					if(absLink.endsWith("/")){
						str = absLink.substring(0,absLink.lastIndexOf('/'));
					}
					else{
						str = absLink+"/";
					}
					if(hp.containsKey(absLink) || hp.containsKey(str)){
						if(!hp.containsKey(absLink)){
							absLink = str;
						}
						int index2 = hp.get(absLink);
						if(index2 >= 116){
							index2--;
						}
						if(index != index2){
							arr[index2][index] = 1;
							Element img = link.getElementsByTag("img").first();
							if(img!= null){
								 String alt = img.attr("alt");
									 
								 ArrayList<String> list = this.anchors.get(absLink);
								 if(!list.contains(alt) && !alt.equals("")){
									 list.add(alt);
									 this.anchors.put(absLink, list);
								 }
								 
								 ArrayList<String> query = processQuery(alt.toLowerCase());
								 if(query.size() > 0){
									 for(int i = 0; i < query.size(); i++){
										 if(!this.allAnchorText.contains(query.get(i))){
											 this.allAnchorText.add(query.get(i));
										 }
									 }
									 
								 }
								
									 
							}
							else{
								ArrayList<String> list = this.anchors.get(absLink);
								if(!list.contains(link.text()) && !link.text().equals("")){
									list.add(link.text());
									this.anchors.put(absLink, list);
								}
								
								
								ArrayList<String> query = processQuery(link.text().toLowerCase());
								if(query.size() > 0){
									 for(int i = 0; i < query.size(); i++){
										 if(!this.allAnchorText.contains(query.get(i))){
											 this.allAnchorText.add(query.get(i));
										 }
									 }	 
								}
			
							}
						}
	
					}
				}
	//	        System.out.println(absLink + ", " + link.text());
		    }
		}
	}
	
	public void getTitle() throws IOException{
		Iterator itr = this.pages.keySet().iterator();
		String title = "";
		while(itr.hasNext()){
			title = "";
			String key = (String) itr.next();
			if(!key.equals("")){
				Document doc = 	Jsoup.connect(key).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
				title = doc.select("title").first().text();
			}
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(title);
			this.anchors.put(key, list);
					 
//			ArrayList<String> query = processQuery(title.toLowerCase());
//			if(query.size() > 0){
//				for(int i = 0; i < query.size(); i++){
//					if(!this.allAnchorText.contains(query.get(i))){
//						this.allAnchorText.add(query.get(i));
//					}
//				}
//				 
//			}
			
		}
	}
	
	public Elements getHyperLinks(String url) throws IOException{
		if(!url.equals("")){
			Document doc = 	Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
			Element content = doc.select("body").first();
			Elements links = content.getElementsByTag("a");

			return links;
		}
		return null;
	}

	
	public HashMap<String, Integer> readFile(String url) throws NumberFormatException, IOException{
		URL newURL = new URL(url);
        URLConnection urlCon = newURL.openConnection();
        BufferedReader reader = new BufferedReader( new InputStreamReader(urlCon.getInputStream()));
        
        HashMap<String, Integer> hp = new HashMap<String, Integer>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            StringTokenizer token = new StringTokenizer(line,","); 
//				if(token.countTokens() == 1){
//					int index = Integer.parseInt(token.nextToken().trim());
//					hp.put("", index-1);
//				}
//				else{
            	if(token.countTokens() != 1){
					int index = Integer.parseInt(token.nextToken().trim());
					String s = token.nextToken();
					hp.put(s, index-1);
				}
        }
        
        reader.close();
        return hp;
	}
}
