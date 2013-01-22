import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import java.util.Iterator;


public class NearDuplicates {
	ArrayList<Shingles> shingles;
	HashMap<Integer,ArrayList<Integer>> docs;
	NumberPair[] pairs;
	ArrayList<int[]> sketches;
	double[][] J;
	public NearDuplicates() throws IOException{
		ReadFile rf = new ReadFile();	
		this.shingles = rf.readAll();
		this.docs = docShingles();
		this.pairs = getRandom();
		this.sketches = getAllSketch();
		this.J = compareAll();
	}
	
	public HashMap<Integer,ArrayList<Integer>> docShingles(){
		HashMap<Integer,ArrayList<Integer>> docs = new HashMap<Integer,ArrayList<Integer>>();		
		for(int i = 0; i < this.shingles.size(); i++){
			int label = this.shingles.get(i).getId();
			ArrayList<Integer> tempValue = this.shingles.get(i).getDocs(); 
			for(int j = 0; j < tempValue.size(); j++){
				int index = tempValue.get(j);
//				System.out.println(tempValue.get(i));
				if(!docs.containsKey(index)){
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(label);
					docs.put(index, list);
				}
				else{
					ArrayList<Integer> list = docs.get(index);
					list.add(label);
					docs.put(index, list);
				}
			}
		}
		return docs;
	}

	public NumberPair[] getRandom(){
		int p = 23909;
		NumberPair[] pairs = new NumberPair[25];
		Random  r = new  Random();   
		for(int i = 0; i < 25; i++){
			int a = 0;
			int b = 0;
			while(a == 0){
				a = r.nextInt(p);
			}
			b = r.nextInt(p);
			NumberPair n = new NumberPair(a,b);
			pairs[i] = n;
		}
		return pairs;
	}
	
	public ArrayList<int[]> getAllSketch(){
		ArrayList<int[]> sketches = new ArrayList<int[]>();
		for(int i = 0; i < 100; i++){
			int[] s = getSketch(i);
			sketches.add(s);
		}
		return sketches;
	}
	
	public int[] getSketch(int docId){
		int[] sketch = new int[25];
		ArrayList<Integer> list = this.docs.get(docId);
		
		for(int j = 0; j < 25; j++){
			int min = 23909;
			int l = 0;
			for(int i = 0; i < list.size(); i++){
				int f = (this.pairs[j].getA() * list.get(i) + this.pairs[j].getB())%23909;
				if(f < min){
					min = f;
					l = list.get(i);
				}
			}
			sketch[j] = l;
		}
		return sketch;
	}

	
	public double[][] compareAll(){
		double[][] Jaccard = new double[100][100];
		for(int i = 0; i < 100; i++){
			for(int j = i; j < 100; j++){
				double p = compare(i,j);
				Jaccard[i][j] = p;
				Jaccard[j][i] = p;
			}
		}
		return Jaccard;
	}
	public double compare(int id1, int id2){
		double j = 0.0;
		int[] s1 = this.sketches.get(id1);
		int[] s2 = this.sketches.get(id2);
		int count = 0;
//		HashSet<Integer> set = new HashSet<Integer>();
//		for(int i = 0; i < s1.length; i++){
//			set.add(s1[i]);
//		}
//		for(int i = 0; i < s2.length; i++){
//			if(set.contains(s2[i])){
//				count++;
//			}
//		}
		for(int i = 0; i < 25; i++){
			if(s1[i] == s2[i]){
				count++;
			}
		}
		j = (double)count/25;
		return j;
	}
	
	public double compareT(int id1, int id2){
		ArrayList<Integer> s1 = this.docs.get(id1);
		ArrayList<Integer> s2 = this.docs.get(id2);
		ArrayList<Integer> union = new ArrayList<Integer>();
		ArrayList<Integer> intersect = new ArrayList<Integer>();
		for(int i = 0; i < s1.size(); i++){
			if(!union.contains(s1.get(i))){
				union.add(s1.get(i));
			}
		}
		for(int i = 0; i < s2.size(); i++){
			if(!union.contains(s2.get(i))){
				union.add(s2.get(i));
			}
		}
		
		for(int i = 0; i < s1.size(); i++){
			for(int j = 0; j < s2.size(); j++){
		//		System.out.println(s1.get(i)+", " +s2.get(j));
				if(s1.get(i).equals(s2.get(j))){
					intersect.add(s1.get(i));
				}
			}
		}
		return (double)intersect.size()/union.size();
	}
}
