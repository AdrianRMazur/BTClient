package btclient;


import java.io.File;
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
		
		File torrent = new File (args[0]);
		Scanner filescanner;
		try {
			 filescanner = new Scanner (torrent);
		} catch (FileNotFoundException e1) {
			System.out.println("Error: File read error");
			e1.printStackTrace();
			System.exit(1);
		}
		
		int torrentsize = (int) torrent.length();
		byte[] torrentdata = new byte[torrentsize];
		
		Path path = Paths.get(args[0]);
		try {
			torrentdata = Files.readAllBytes(path);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		try {
			ByteBuffer temp = Bencoder2.getInfoBytes(torrentdata);
		} catch (BencodingException e) {
			e.printStackTrace();
			System.out.println("Error: File cannot be decoded");
			System.exit(1);
		}
		
		System.out.println("sdfsdfdsf");
		
		
		
	}



}
