import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Test {

	public static void main(String[] args) throws IOException{

		while(true){
			String s = JOptionPane.showInputDialog(null,"Please input the searching terms:\n");  
			if(s == null || s.equals("")){
				System.exit(0);
			}
			WebSearch ws = new WebSearch();
//			ws.writeFile(ws.pages);	
			ArrayList<Map.Entry<String, Double>> ret = ws.Query(s);
			String output = "";
			for(int i = 0; i < ret.size(); i++){
				String str = "";
				String key = ret.get(i).getKey();
				str += "PageId: " + (ws.pages.get(key)+1) + ", url: " + key+"\n";
				str += "Snippet: " + ws.anchors.get(key).get(0);
				output += str + "\n";
			}
			
			if(output.equals("")){
				output = "Sorry, we cannot find a matching page";
			}
			JTextArea textArea = new JTextArea(output,20,60);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			textArea.setLineWrap(true);  
			textArea.setWrapStyleWord(true);  
			JOptionPane.showMessageDialog(null, scrollPane, "results",  
			                                       JOptionPane.YES_NO_OPTION);
		}
	}
}
