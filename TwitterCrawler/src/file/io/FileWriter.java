package file.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FileWriter {

	public void writeTextToFile(String fileName, String text) throws FileNotFoundException, UnsupportedEncodingException  {
		
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println(text);
		writer.close();
		
	}

	public void writeListToFile(String fileName, List<String> text) throws FileNotFoundException, UnsupportedEncodingException  {
		
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		for (String line : text) {
			writer.println(line);
		}
		writer.close();
		
	}
	
	public void saveTwitterIdsList(String fileName, List<Long> twitterIds) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		for (Long twitterId : twitterIds) {
			writer.println(twitterId.toString());
		}
		writer.close();
		
	}

	public boolean fileExists(String fileName) {
		
		File file = new File(fileName);
		if(file.exists() && !file.isDirectory()) { 
			return true;
		} else {
			return false;
		}
	}
	
	public List<String> getFileContents(String fileName) throws IOException {
		
		List<String> returnValue = new ArrayList<String>();
		FileReader inputFile = new FileReader(fileName);
		BufferedReader bufferReader = new BufferedReader(inputFile);
		String line = null;
		while ((line = bufferReader.readLine()) != null)   {
			returnValue.add(line);
        }
        bufferReader.close();
		return returnValue;
		
	}

}
