package btclient;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class BTClient {

	private static FileOutputStream savefile = null; 
	
	public static void main(String[] args) throws Exception {

		if (args.length!=2){
			System.out.println("Error: Provide torrent file name and save file name. \n");
			System.exit(1);
		} 
		
		savefile = new FileOutputStream(new File(args[1]));
		
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
			input.close(); 
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
		
	
		byte [] serverreply = EstablishConnection(torrentinfo);
		if (serverreply == null){
			closer(); 
		}
		
		if ( validatePeers(serverreply,torrentinfo) == false){
			closer(); 
		}
		
	}
	
	

	
	
	private static boolean validatePeers (byte [] fromServer1, TorrentInfo torrentinfo) throws Exception{
		Map<ByteBuffer,Object> obj = null;
		try {
			obj=(Map<ByteBuffer, Object>)Bencoder2.decode(fromServer1);
		} catch (BencodingException e) {
			return false; 		
		} 
		
		
		ArrayList availablepeers = (ArrayList)obj.get(ByteBuffer.wrap(new byte [] {'p','e','e','r','s'}));
		Map<ByteBuffer, Object> firstpeer = (Map<ByteBuffer, Object>) availablepeers.get(0);
		int peerport = (Integer)firstpeer.get((ByteBuffer.wrap(new byte [] {'p','o','r','t'})));
		String peerip = new String (((ByteBuffer) firstpeer.get((ByteBuffer.wrap(new byte [] {'i','p'})))).array(), "ASCII");	
		
		Socket s = new Socket(peerip, peerport);
		InputStream input= s.getInputStream();
		OutputStream output = s.getOutputStream(); 
		DataOutputStream dataout= new DataOutputStream(output);
		DataInputStream datain=new DataInputStream(input);
		
	
		byte[] toShake=new byte[68]; 
		toShake[0]= (byte) 19;
		byte [] bittorrent = new byte [] {'B','i','t','T','o','r','r','e','n','t',' ','p','r','o','t','o','c','o','l'};
		
		System.arraycopy(bittorrent, 0, toShake, 1, 19);

		System.arraycopy(torrentinfo.info_hash.array(), 0, toShake, 28, 20); 
		System.arraycopy("AdrianAndKosti@@@@@@".getBytes(), 0, toShake, 48, 20 ); // too short???
		dataout.write(toShake);
		dataout.flush(); 
		s.setSoTimeout(1000);
		
		byte[] fromShake=new byte[68]; 
		datain.readFully(fromShake); 
	
		
		byte[] infohashpart = Arrays.copyOfRange(fromShake, 28, 48);
		
		if (Arrays.equals(infohashpart, torrentinfo.info_hash.array()) == false){
			s.close(); 
			dataout.close();
			datain.close(); 
			return false; 
		}
	
		boolean unchoke = false; 	
		int lastpiecelength = torrentinfo.file_length - (torrentinfo.piece_length * (torrentinfo.piece_hashes.length-1));
		
		for (;;){
			if (datain.readByte() ==-2)
				break;  		
		}
				
		
		while (unchoke == false ){
			byte [] interested = new byte [5];	
			System.arraycopy(toEndianArray(1), 0, interested, 0, 4);
			interested[4] = (byte) 2;
			dataout.write(interested);
			dataout.flush(); 
			s.setSoTimeout(1300000);
			
			for (int c = 0; c<5; c++){
				if (c==4){
					if (datain.readByte() ==1){
						unchoke = true; 
						break; 
					}
				}
				datain.readByte();
			}
		}		
		
		 
		for (int count = 0; count <torrentinfo.piece_hashes.length; count++){
			int temp = 0; 
			for (;;){
				byte [] msgrequest = new byte [17];
				System.arraycopy(toEndianArray(13), 0, msgrequest, 0, 4);
				msgrequest[4] = (byte)6;
				if (count < torrentinfo.piece_hashes.length-1){
					System.arraycopy(toEndianArray(count), 0, msgrequest, 5, 4);
					System.arraycopy(toEndianArray(temp), 0, msgrequest, 9, 4);
					System.arraycopy(toEndianArray(16384), 0, msgrequest, 13, 4);
					
					dataout.write(msgrequest);
					dataout.flush();
					s.setSoTimeout(130000);
					
					for (int i = 0; i < 13; i++) {
						 datain.readByte();
					} 
					
				
					byte [] peerresponse = new byte [16384];
					for (int c = 0; c< 16384; c++){
						peerresponse [c] = datain.readByte();
					}
					
					savefile.write(peerresponse);
				
					if (temp + 16384 == torrentinfo.piece_length)
						break; 
					else
						temp = temp + 16384; 
				}
				else {
					int size = 16384;  
					if (lastpiecelength < 16384)
						size = lastpiecelength; 
					
					lastpiecelength = lastpiecelength - 16384;
					
					System.arraycopy(toEndianArray(count), 0, msgrequest, 5, 4);
					System.arraycopy(toEndianArray(temp), 0, msgrequest, 9, 4);
					System.arraycopy(toEndianArray(size), 0, msgrequest, 13, 4);
					
					dataout.write(msgrequest);
					dataout.flush(); 
					s.setSoTimeout(1000);
					
					for (int i = 0; i < 13; i++) {
						 datain.readByte();
					} 
	
					byte [] peerresponse = new byte [size];
					for (int c = 0; c< size; c++){
						peerresponse [c] = datain.readByte();
					}
					savefile.write(peerresponse);
					
					if (lastpiecelength < 0 )
						break; 
					
					temp  = temp + count; 
					
				}
			}
			
		}
		
		s.close();
		datain.close(); 
		dataout.close();
		savefile.close();
		
		
		return true; 
	}
	
	
	private static byte [] EstablishConnection(TorrentInfo torrentinfo){
			
		String infohash, peerid, urlstring;
		StringBuilder temp; 
		
		
		temp = new StringBuilder((torrentinfo.info_hash.array().length) * 2);
		for(byte temp2: torrentinfo.info_hash.array())
			 temp.append('%').append(String.format("%02x", temp2 & 0xff));
		infohash = temp.toString(); 
	
		temp = new StringBuilder(("AdrianAndKosti@@@@@@".getBytes().length) * 2);
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
		
		byte [] serverreply = null; 
		
		try {
			con = (HttpURLConnection) url.openConnection();
			in = new DataInputStream( con.getInputStream());
	
			
			
			serverreply = new byte[con.getContentLength()];
			in.readFully(serverreply);
			
			in.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 
			return null; 
		}
	
		con.disconnect(); 
		
		return serverreply;
		
	}
	
	
	  public static String byteArrayToHex(byte[] a) {
		 StringBuilder sb = new StringBuilder(a.length * 2);
		 for(byte b: a)
			 sb.append('%').append(String.format("%02x", b & 0xff));
		 return sb.toString();
	  }
	  
	
	public static  int fromEndianArray(byte[] x){
	    ByteBuffer temp = ByteBuffer.wrap(x);
	    temp.order(ByteOrder.BIG_ENDIAN);
	    return temp.getInt();
	}
	  
	public static byte[] toEndianArray(int x){
	    ByteBuffer temp = ByteBuffer.allocate(4);
	    temp.order(ByteOrder.BIG_ENDIAN);
	    temp.putInt(x);
	    temp.flip();
	    return temp.array();
	}

	
	
	private static void closer(){
		try {
			savefile.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		System.out.println("Error: A critical error occured");
		System.exit(1);
	}
	
}
