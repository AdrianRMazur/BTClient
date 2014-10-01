package btclient;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class BTClient {

	public static void main(String[] args) throws IOException {

		if (args.length!=2){
			System.out.println("Error: Provide torrent file name and save file name. \n");
			System.exit(1);
		} 
		
		DataInputStream input = null; 
		File inputtorrent = new File (args[0]);
		int torrentsize = (int) inputtorrent.length();
		
		if (torrentsize > 1000000){
			System.out.println("Error: File size too large");
			System.exit(1);
		}
		
		byte[] torrentbyte = new byte[torrentsize];
		TorrentInfo torrentinfo = null; 
		
		try {
			input = new DataInputStream (new BufferedInputStream(new FileInputStream(inputtorrent)));
			input.read(torrentbyte);
			torrentinfo = new TorrentInfo(torrentbyte);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			closer(); 
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 
		} catch (BencodingException e) {
			e.printStackTrace();
			closer(); 
		}

		
		URL url = torrentinfo.announce_url; 
		String ip = url.getHost(); 
		int port = url.getPort();
	
		String x= torrentinfo.file_name;
	
		
		HttpURLConnection con =null;
		BufferedReader in = null;
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		
		/*
		 * public static String byteArrayToHex(byte[] a) {
   StringBuilder sb = new StringBuilder(a.length * 2);
   for(byte b: a)
      sb.append(String.format("%02x", b & 0xff));
   return sb.toString();
}
		 * */
		
		
		
		try {

			con = (HttpURLConnection) url.openConnection();
			
			in = new BufferedReader( new InputStreamReader(con.getInputStream()));
			
			int responseCode = con.getResponseCode();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		System.out.println(response.toString());

		
		//good shit
		System.out.println("path is: " + ip + "\n number is: " + port);
		

		
		
	
		//System.out.println("sdfsdfdsf");
		
		
		
	}
	
	private static void closer(){
		System.out.println("Error: A critical error occured");
		System.exit(1);
	}
	
}
