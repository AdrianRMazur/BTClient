package btclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class TrackerTalker {

	public static String MakeConnection(String ip, int port) throws IOException{
		
		if(ip == null || port==0){
			return "Error"; 
		}
		
		BufferedReader br=null; 
		Socket toTracker= new Socket();
		PrintWriter pr=null; 
		URL url= new URL("http://128.6.5.130:6969/announce");
		
		try{
			toTracker= new Socket(ip,port); 
		}
		catch(IOException e){
			System.out.println("Failed @ "+ip+", "+port);
			System.exit(1); 
		}
		
		
		pr= new PrintWriter(toTracker.getOutputStream(),true); 
		br= new BufferedReader(new InputStreamReader(toTracker.getInputStream()));
		
		pr.println(); 
		
		
		return ip; 
		
		
	}
	
	
}
