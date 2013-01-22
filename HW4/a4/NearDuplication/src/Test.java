import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Test {
	public static void main(String[] args) throws IOException{

		
			
			NearDuplicates nd = new NearDuplicates();
			String out = "";
			out += "A list of document pairs with J(d1,d2) > 0.5 \n";
			for(int i = 0 ; i < 100; i++){
				for(int j = i+1; j < 100; j++){
					if(nd.J[i][j] > 0.5 ){
						String nI = i < 10 ? "0"+i:""+i;
						String nJ = j < 10 ? "0"+j:""+j;
						out += "file"+nI+" and " +"file"+nJ+", Jaccard coefficient: " + nd.J[i][j]+ "; "+"\n";
			//			System.out.println("file"+nI+" " +"file"+nJ+", Jaccard coefficient: " + nd.J[i][j]+ "; ");	
					}
				}
			}
			out += "\n";
	//		System.out.println();
			for(int i = 0; i < 10; i++){
				PriorityQueue<Doc> q = new PriorityQueue<Doc>(3,new Comparator<Doc>(){
					@Override
					public int compare(Doc o1, Doc o2) {
						// TODO Auto-generated method stub
						return (o2.getSim() < o1.getSim())?-1:1;
					}
				});
				
				for(int j = 0; j < 100; j++ ){
					if(j != i){
						q.add(new Doc(j,nd.J[i][j]));
					}
				}
				out += "3 neareast neighbors of file0"+ i + ":"+"\n";
			//	System.out.println("3 neareast neighbors of file0"+ i + ":");
				for(int j = 0; j < 3; j++){
					Doc d = q.poll();
					out += "file"+d.getId()+", Jaccard coefficient: " + d.getSim() + "; ";
			//		System.out.print("file"+d.getId()+", Jaccard coefficient: " + d.getSim() + "; ");
				}
				out += "\n";
		//		System.out.println();
			}
			
			JTextArea textArea = new JTextArea(out,20,60);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			textArea.setLineWrap(true);  
			textArea.setWrapStyleWord(true);  
			JOptionPane.showMessageDialog(null, scrollPane, "results",  
			                                       JOptionPane.YES_NO_OPTION);
		}
	
}
