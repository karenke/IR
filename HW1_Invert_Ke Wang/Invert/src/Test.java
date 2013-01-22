import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Test {

	public static void main(String[] args) throws IOException{

		while(true){
			String s = JOptionPane.showInputDialog(null,"Please input the searching terms:\n");  
			if(s == null || s.toLowerCase().equals("zzz") || s.equals("")){
				System.exit(0);
			}
			Invert invert = new Invert();
			ArrayList<String> output = invert.query(s, invert.doc_freq, invert.list);
			String out = "";
			if(output != null){
				for(int i = 0; i < output.size(); i++){
				//	System.out.println(output.get(i));
					out += output.get(i);
				}
				
				JTextArea textArea = new JTextArea(out,20,60);
				JScrollPane scrollPane = new JScrollPane(textArea);  
				textArea.setLineWrap(true);  
				textArea.setWrapStyleWord(true);  
				JOptionPane.showMessageDialog(null, scrollPane, "results",  
				                                       JOptionPane.YES_NO_OPTION);

		

			}
			
		}
	}
}
