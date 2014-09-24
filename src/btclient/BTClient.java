package btclient;

import java.io.File;

public class BTClient {

	public static void main(String[] args) {

		
		if (args.length!=2){
			System.out.println("Error: Provide torrent file name and save file name. \n");
			System.exit(1);
		} 
		
		File torrent = new File (args[0]);
		int torrentsize = (int) torrent.length();
		
		byte[] torrentdata = new byte[torrentsize];
		
		
		
		
		
	}

}
