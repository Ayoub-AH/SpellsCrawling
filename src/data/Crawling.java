package data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.List;

public class Crawling {
	private String filesRoot;
	private JSONArray DataArray;
	private List<BasicDBObject> ListDBSpell = new ArrayList<BasicDBObject>();
	private String[] pages;
	private String prefix = "http://paizo.com/pathfinderRPG/prd/";
	
	public Crawling(String filesRoot) {
		pages = new String[] {prefix+"bestiary/monsterIndex.html", 
				prefix+"bestiary2/additionalMonsterIndex.html",
				prefix+"bestiary3/monsterIndex.html",
				prefix+"bestiary4/monsterIndex.html",
				prefix+"bestiary5/index.html"
		};
		this.filesRoot = filesRoot;
		//this.DataArray = new JSONArray();
		File directory = new File(this.filesRoot+"BDDR_test");

	    if (!directory.exists()) {
	        directory.mkdir();
	    }
	    else {
	    	for (File f : directory.listFiles()) f.delete();
	    }
	}
	
	public Crawling(String filesRoot, String[] _pages) {
		pages = _pages;
		this.DataArray = new JSONArray();
		File directory = new File(this.filesRoot+"BDDR_test");

	    if (!directory.exists()) {
	        directory.mkdir();
	    }
	    else {
	    	for (File f : directory.listFiles()) f.delete();
	    }
	}

	public void getPage(String urlString, String fileName) {
				
		final File HTMLFile = new File(fileName);         

		// Connect to web site to extract the spells ----------------------------------------------------
		try{
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			InputStream input = con.getInputStream();
					
			// Create the HTML file for the current spell
				
			HTMLFile.createNewFile();
			final FileWriter writer = new FileWriter(HTMLFile);
			Reader reader = new InputStreamReader(input, "UTF-8");
			char[] buffer = new char[10000];
			StringBuilder builder = new StringBuilder();
			int len;
				
			try {
				// Testing whether the file is empty or not by testing the length of the data that has just been read
				while ((len = reader.read(buffer)) > 0) {
					builder.append(buffer, 0, len);
				}
					
					// Write in the file
					writer.write(builder.toString());	 
				}
				finally {
		            	
					// Close the file 
					writer.close();
				}
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e);
		} // -----------------------------------------------------------------------------------------
	}
	
	
	public void getMonsters() throws IOException {
		int idPage = 1;
		while(idPage <= 5) {
			getPage(this.pages[idPage-1], this.filesRoot+"BDDR_test/monsterHTML"+idPage+".txt");
			System.out.println("Page "+idPage+" downloaded with success !");
			idPage = idPage + 1;
		}
		cleanMonster();
	}
	
	public void cleanMonster() throws IOException {
		int idPage = 1;
		
		File directory = new File(this.filesRoot+"spells");

	    if (!directory.exists()) {
	        directory.mkdir();
	    }
	    else {
	    	for (File f : directory.listFiles()) f.delete();
	    }
		
		while(idPage <= 5) {
			String monsterPath = this.filesRoot+"BDDR_test/monsterHTML"+idPage+".txt";
			monsterHTMLCleaner(monsterPath, idPage);
			System.out.println("Page "+idPage+" cleaned with success !");
			getSpells(this.filesRoot+"BDDR_test/monster"+idPage+".txt",idPage);
			idPage++;
		}
		cleanDirectory(this.filesRoot+"spells");
	}
	
	public void monsterHTMLCleaner(String spellPath, int idPage) throws IOException {
		
		try (BufferedReader br = new BufferedReader(new FileReader(spellPath))) {

			String line;
			boolean dataStarted = false;
					    
			while ((line = br.readLine()) != null) {
				String monsterUrl = "";
				String monsterName = "";
				
				// process the line.
				
				if(line.contains("<div id=\"monster-index-wrapper\" class=\"index\">")) {
					dataStarted = true;
				}
				if(dataStarted && line.contains("<li>")) {
					
					boolean urlStarted = false;
					boolean nameStarted = false;
					boolean help = false;
					
					for(int i = 0; i< line.length(); i++) {
						if(line.charAt(i) == '=') {
							urlStarted = true;
						}
						if(urlStarted && !help && line.charAt(i) == '"') {
							i++;
							help = true;
						}
						if(urlStarted && help) {
							if(line.charAt(i)=='"') {
								urlStarted = false;
								if(line.charAt(i+1) == '>') {
									i += 2;
									nameStarted = true;
								}
							}
							else {
								monsterUrl += line.charAt(i);
							}
						}
						if(nameStarted) {
							if(line.charAt(i) == '<') {
								nameStarted = false;
							}
							else {
								monsterName += line.charAt(i);
							}
						}
					}
				}
				
				if(line != null && line.contains("</div>")) {
					dataStarted = false;
				}
				
				if(monsterName != "") {
					FileWriter fw = new FileWriter(this.filesRoot+"BDDR_test/monster"+idPage+".txt", true); 
					BufferedWriter output = new BufferedWriter(fw);
					output.write(monsterName+":"+monsterUrl+"\n");
					output.flush();  
					output.close();
					
				}
			}
			br.close();
			new File(this.filesRoot+spellPath).delete();
		}
		
	}
	
	public void getSpells(String spellFile, int idPage) throws IOException {
		
		try (BufferedReader br = new BufferedReader(new FileReader(spellFile))) {
			String line;
			boolean urlStarted = false;
			int idSpell = 0;
			
			while ((line = br.readLine()) != null) {
				String url="";
				String name ="";
				for(int i =0; i<line.length();i++) {
					if(line.charAt(i) == ':') {
						name = line.substring(0,i);
						url = line.substring(i+1);
						break;
					}
				}
				if(idPage == 1) {
					url = prefix + "bestiary/" + url;
				}
				else {
					url = "http://paizo.com" + url;
				}
				getSpellsFromUrl(url, this.filesRoot+"spells/"+name+".txt",name);
				System.out.println("spell "+idSpell+" "+name+" downloaded with success !");
				idSpell++;
			}
		}
		
	}
	
	
	
	
	public void getSpellsFromUrl(String urlString, String fileName, String spellName) {
		
		final File HTMLFile = new File(fileName);         

		// Connect to web site to extract the spells ----------------------------------------------------
		try{
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			InputStream input = con.getInputStream();
					
			// Create the HTML file for the current spell
				
			HTMLFile.createNewFile();
			final FileWriter writer = new FileWriter(HTMLFile);
			Reader reader = new InputStreamReader(input, "UTF-8");
			char[] buffer = new char[10000];
			StringBuilder builder = new StringBuilder();
			int len;
				
			try {
				// Testing whether the file is empty or not by testing the length of the data that has just been read
				while ((len = reader.read(buffer)) > 0) {
					builder.append(buffer, 0, len);
				}
					
					// Write in the file
					writer.write(spellDataAnalyzer(builder.toString(),spellName));	 
				}
				finally {
		            	
					// Close the file 
					writer.close();
				}
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e);
		} // -----------------------------------------------------------------------------------------
	}
	
	
	
	
	public String spellDataAnalyzer(String spellPageHTML, String monsterName) {

		String[] lines = spellPageHTML.split("\n");
		boolean dataStarted= false;
		String data="";
		for(int i=0; i<lines.length; i++) {
			
			if(!dataStarted && lines[i].contains("h1") && lines[i].contains(monsterName)) {
				i++;
				dataStarted = true;
			}
			
			if(dataStarted && lines!=null && lines.length>i && (lines[i].contains("h1") || lines[i].contains("footer"))) {
				dataStarted = false;
			}
			
			if(dataStarted && lines!=null && lines.length>i && lines[i].contains("/spells/")) {
				String dataHelper= "";
				int indexBegin = lines[i].indexOf("<a");
				if(indexBegin>-1) {
					int indexEnd = lines[i].indexOf("</",indexBegin);
					dataHelper = lines[i].substring(indexBegin,indexEnd);
					if(data!="" && dataHelper!="")
						data +="\n";
					if(dataHelper!="")
						data += dataHelper.substring(dataHelper.lastIndexOf(">")+1);
					// If There's 2 "," and nothing in between skip it
				}
			}

		}
		
		//System.out.println(monsterName+":"+data);
		return data;
	}
	
	public void cleanDirectory(String directoryPath) {
		
		File directory = new File(directoryPath);

	    if (directory.exists()) {
	    	
	    	for (File f : directory.listFiles()) 
	    		if(f.length() == 0)
	    			f.delete();
	    }
	    
	}
	
	public  void play(String fileRoot) throws IOException{
		
		Crawling data = new Crawling(fileRoot);
		data.getMonsters();
	}
}