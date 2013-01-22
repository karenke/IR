import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ReadFile {
		
	public ArrayList<Shingles> readAll() throws IOException{
		HashMap<String,Integer> set = new HashMap<String,Integer>();
		ArrayList<Shingles> shingles = new ArrayList<Shingles>();
		int id = 0;
		for(int j = 0; j < 100; j++){
			//readEach(i,label,hp);
			String newIndex = j < 10? "0"+ j : ""+j;
			String filename = "./test/file" + newIndex + ".txt"; 
			BufferedReader input = new BufferedReader(new FileReader(filename));
	        String line = null;
	        while ((line = input.readLine()) != null) {	
	        	String testreg = "[^a-zA-Z\\s]";
	            Pattern matchsip = Pattern.compile(testreg);
	            Matcher mp = matchsip.matcher(line);
	            line = mp.replaceAll("");
	            StringTokenizer token=new StringTokenizer(line.toLowerCase()," "); 
				String[] readin = new String[token.countTokens()];
				for(int i = 0; i < readin.length; i++){
					readin[i] = token.nextToken();
				}
				for( int i = 0; i < readin.length-2; i++){
					String s = readin[i]+"-"+readin[i+1]+"-"+readin[i+2];
					if(!set.containsKey(s)){
						set.put(s,id);
						ArrayList<Integer> docs = new ArrayList<Integer>();
						docs.add(j);
						shingles.add(new Shingles(id,s,docs));
						id++;
					}
					else{
						int index = set.get(s);
						ArrayList<Integer> docs = shingles.get(index).getDocs();
						if(!docs.contains(j)){
							docs.add(j);
						}
						shingles.get(index).setDocs(docs);
						
					}
				}
				
	        }
	        input.close();
		}
		return shingles;
		
	}
	
}
