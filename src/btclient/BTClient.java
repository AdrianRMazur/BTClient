package btclient;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class BTClient {

	public static void main(String[] args) {

		
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
		byte[] torrentdata = new byte[torrentsize];
		
		
		try {
			input = new DataInputStream (new BufferedInputStream(new FileInputStream(inputtorrent)));
			input.read(torrentdata);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			closer(); 
		} catch (IOException e) {
			e.printStackTrace();
			closer(); 
		}
		
		
		try {
			TorrentInfo torrentinfo = new TorrentInfo(torrentdata);
		} catch (BencodingException e) {
			e.printStackTrace();
			closer(); 
		}

		
		
	
		System.out.println("sdfsdfdsf");
		
		
		
	}
	
	private static void closer(){
		System.out.println("Error: A critical error occured");
		System.exit(1);
	}
	
}
