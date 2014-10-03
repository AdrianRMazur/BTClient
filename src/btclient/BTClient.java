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
import java.net.MalformedURLException;
import java.net.Socket;
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
			input.close(); 
			closer(); 
		} catch (IOException e) {
			e.printStackTrace();
			input.close(); 
			closer(); 
		} catch (BencodingException e) {
			e.printStackTrace();
			input.close(); 
			closer(); 
		}

	
		byte [] serverreply = EstablishConnection(torrentinfo);
		if (serverreply == null){
			input.close();
			System.exit(1);
		}
		
		validatePeers(serverreply);
		
	}
	
	private static StringBuffer validatePeers (byte [] fromServer1){
		System.out.println("LOLOLOLOLOL");
		Map<ByteBuffer,Object> obj = null;
		try {
			obj=(Map<ByteBuffer, Object>)Bencoder2.decode(fromServer1);
		} catch (BencodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		ByteBuffer x = (ByteBuffer)obj.get("peers"); 
		
		//Peer peer
		
		//Socket toPeers= new Socket(); 
		
		ToolKit.print(obj);
		
		return null; 
	}
	
	
	private static byte [] EstablishConnection(TorrentInfo torrentinfo){
			
		String infohash, peerid, urlstring;
		StringBuilder temp; 
		
		
		temp = new StringBuilder((torrentinfo.info_hash.array().length) * 2);
		for(byte temp2: torrentinfo.info_hash.array())
			 temp.append('%').append(String.format("%02x", temp2 & 0xff));
		infohash = temp.toString(); 
	
		temp = new StringBuilder(("AdrianAndKosti".getBytes().length) * 2);
		for(byte temp2: torrentinfo.info_hash.array())
			 temp.append('%').append(String.format("%02x", temp2 & 0xff));
		peerid = temp.toString(); 

		urlstring = torrentinfo.announce_url.toString() + "?info_hash=" + infohash + "&peer_id="+peerid + "&port=6885&uploaded=0&downloaded=0&left=" +torrentinfo.file_length ; 
		
		URL url;
		try {
			url = new URL (urlstring);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			closer();
			return null; 
		}

		
		HttpURLConnection con =null;
		DataInputStream in = null;
		String tempinput=null;
		
		//int responsecode =0; 
		byte [] serverreply = null; 
		
		try {
			con = (HttpURLConnection) url.openConnection();
			in = new DataInputStream( con.getInputStream());
	
			
			//responsecode = con.getResponseCode();
			
			serverreply = new byte[con.getContentLength()];
			in.readFully(serverreply);
			
			
			//while ((tempinput = in.readLine()) != null) {
			//	serverreply.append(tempinput);
			//} 
			
			in.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 
			return null; 
		}
		/*
		System.out.println("server returned code is: " + responsecode);
		System.out.println("Server: "+ serverreply.toString());
		Looks good, makes connection and returns peer information
		*
		*/
		con.disconnect(); 
		
		return serverreply;
		
	}
	
	
	  public static String byteArrayToHex(byte[] a) {
		 StringBuilder sb = new StringBuilder(a.length * 2);
		 for(byte b: a)
			 sb.append('%').append(String.format("%02x", b & 0xff));
		 return sb.toString();
	  }
	  
	

	
	private static void closer(){
		System.out.println("Error: A critical error occured");
		System.exit(1);
	}
	
}
