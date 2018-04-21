package data;
import java.io.IOException;

import org.json.JSONArray;


public class Main{
	
	/*public static void main(String[] argv) throws IOException{
		
		Crawling data = new Crawling("");
		data.getMonsters();
		//JSONArray dataArray= data.getSpellArray();
		//System.out.println("\nThis is the number of all spells : "+dataArray.length());
		
	
		
	}	*/
	
	public  void play(String fileRoot) throws IOException{
		
		Crawling data = new Crawling(fileRoot);
		data.getMonsters();
	}
} 