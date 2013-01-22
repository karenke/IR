import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ReadFile {

	public static void main(String[] args) throws IOException{
		ReadFile rf = new ReadFile();
		ArrayList<String> stoplist = rf.readStopList();
//		System.out.println("size: " + stoplist.size());
//		for(int i = 0; i< stoplist.size();i++)
//			System.out.println(stoplist.get(i));
				
		HashMap<String, LinkedList<Integer>> term_freq = rf.readEachDocument("00",stoplist);
		Iterator itr= term_freq.keySet().iterator(); 
		while(itr.hasNext()) { 
			Object tempKey=itr.next(); 
			System.out.println("key " + tempKey); 
			Object tempValue=term_freq.get(tempKey); 
			System.out.println("value " + tempValue); 
		} 
	}
	
	public ArrayList<String> readStopList() throws IOException{
		String address = "http://www.infosci.cornell.edu/Courses/info4300/2012fa/stoplist.txt";
        URL url = new URL(address);
        URLConnection urlCon = url.openConnection();
        String[] stoplist = null;
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(urlCon.getInputStream())
        );
        
        String line = null;
        while ((line = reader.readLine()) != null) {
        	list.add(line);
    //        System.out.println(line);
        }
        
        reader.close();
        return list;
	}
	
	public HashMap<String, LinkedList<Integer>> readEachDocument(String index, ArrayList<String> stoplist) throws IOException{//index:00-39
		String address = "http://www.infosci.cornell.edu/Courses/info4300/2012fa/test/file"+index+".txt";
        URL url = new URL(address);
        URLConnection urlCon = url.openConnection();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(urlCon.getInputStream())
        );

		/** in each document, key is term. value is the location of the occurance of the term, 
		 * the size of the value is the term frequency*/
		HashMap<String, LinkedList<Integer>> term_freq = new HashMap<String, LinkedList<Integer>>();
		
        String line = null;
        while ((line = reader.readLine()) != null) {
        	String testreg = "[^a-zA-Z\\s]";
            Pattern matchsip = Pattern.compile(testreg);
            Matcher mp = matchsip.matcher(line);
            line = mp.replaceAll("");
            StringTokenizer token=new StringTokenizer(line.toLowerCase()," "); 
			String[] readin = new String[token.countTokens()];
			
			for( int i = 0; i < readin.length; i++){
				String s = token.nextToken().trim();
				if(!stoplist.contains(s)){
					if(!term_freq.containsKey(s)){//the term isn't in the hashmap
						LinkedList<Integer> location = new LinkedList<Integer>();
						location.add(i);
						term_freq.put(s, location);
					}
					else{//exsits
						LinkedList<Integer> location = term_freq.get(s);
						location.add(i);
						term_freq.put(s, location);
					}
					
				}
				
			}
			
        }
        
        reader.close();
        return term_freq;
	}

	public ArrayList<String> readFile(String index, ArrayList<String> stoplist) throws IOException{
		ArrayList<String> in = new ArrayList<String>();
		String address = "http://www.infosci.cornell.edu/Courses/info4300/2012fa/test/file"+index+".txt";
        URL url = new URL(address);
        URLConnection urlCon = url.openConnection();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(urlCon.getInputStream())
        );

		/** in each document, key is term. value is the location of the occurance of the term, 
		 * the size of the value is the term frequency*/
		HashMap<String, LinkedList<Integer>> term_freq = new HashMap<String, LinkedList<Integer>>();
		
        String line = null;
        while ((line = reader.readLine()) != null) {
        	String testreg = "[^a-zA-Z\\s]";
            Pattern matchsip = Pattern.compile(testreg);
            Matcher mp = matchsip.matcher(line);
            line = mp.replaceAll("");
            StringTokenizer token=new StringTokenizer(line.toLowerCase()," "); 
            String[] readin = new String[token.countTokens()];
			for( int i = 0; i < readin.length; i++){
				in.add(token.nextToken().trim());
			}
        }
		return in;
	}
}
