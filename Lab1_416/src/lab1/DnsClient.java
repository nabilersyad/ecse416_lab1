package lab1;

import java.net.*;
import java.util.Arrays;
import java.util.Random;

public class DnsClient {
	

	public static void main(String args[]) throws Exception{
		
		
		int port = 53;
		int timeOut = 5;
		int maxRetry = 3;
		int atSign = 0;
		String domainName = "";
		String queueType = "A"; 									// by default A unless user input something else
		String IPstring[] = {"","","","",};
		int next = 0;
	
		try {														// checks for input arguments, outputs error if in wrong format
			for (int k = 0; k < args.length; k++) {
				char[] bitsArgs = args[k].toCharArray(); 			
				for (int m = 0; m < bitsArgs.length; m++) {						
					if (bitsArgs[m] == '@') {
						domainName = args[k+1]; 
						atSign++;
					}					
					else if (bitsArgs[m] == '.') 
						next++ ;
					
					else if (bitsArgs[m] != '@' && Character.isDigit(bitsArgs[m]) && atSign == 1) 
						IPstring[next] += bitsArgs[m];
					
					else if (bitsArgs[m] == '-'){
						if (bitsArgs[m+1] == 't') {
							timeOut = Integer.parseInt(args[k+1]);
						}
						if (bitsArgs[m+1] == 'r') {
							maxRetry = Integer.parseInt(args[k+1]);
						}
						if (bitsArgs[m+1] == 'p') {

							port = Integer.parseInt(args[k+1]);
						}

						if (bitsArgs[m+1] == 'm' && bitsArgs[m+2] == 'x')
							queueType = "MX";
							
						if (bitsArgs[m+1] == 'n' && bitsArgs[m+2] == 's')
							queueType = "NS";
					}
				}	
			}
				
				if (atSign == 0) {								
					System.out.println("ERROR	IP address is not correct. You are missing an '@'");
					System.exit(1);
				}
			
				
				if (atSign >1) {
					System.out.println("ERROR	Only enter one '@'");
					System.exit(1);
				}
				
				if (timeOut == 0){
					System.out.println("ERROR	timeOut value needs to be greater than 0");
					System.exit(1);
				}
		}
		
		catch (ArrayIndexOutOfBoundsException e) 
		{
			System.out.println("ERROR	Please input the correct format of domain name or an IP address");
			System.exit(1);
		}

		// Create a UDP socket
		DatagramSocket clientSocket = new DatagramSocket();
		
		
		// Allocate buffers for the data to be sent and received
		byte[] sendData = new byte[1024];		
		byte[] receiveData = new byte[1024];

		
		//IP address parsed from string into byte arrays to be input in getByAddress method
		int ipEntry [] = new int[4];
		ipEntry[0] = Integer.parseInt(IPstring[0]);
		ipEntry[1] = Integer.parseInt(IPstring[1]);
		ipEntry[2] = Integer.parseInt(IPstring[2]);
		ipEntry[3] = Integer.parseInt(IPstring[3]);
		
		byte[] ipAddress = new byte [] {(byte) ipEntry[0],(byte) ipEntry[1], (byte) ipEntry[2], (byte) ipEntry[3]};
		
		
		
		InetAddress IPserver = InetAddress.getByAddress(ipAddress);
	     //Returns an InetAddress object given the raw IP address . The argument is in network byte order: the highest order byte of the address is in getAddress()[0].
		
		
		sendData = packet(domainName,queueType);
		
		
		// Create a UDP packet to be sent to the server
		// This involves specifying the server's address and port number
		//DatagramPacket(byte[] data to send,array data length,IPaddress,port number)
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPserver, port);
		
		// Send the packet
		clientSocket.send(sendPacket);
		
		// timer start after sending data and retry tracker
		long startTime = System.nanoTime();
		int retry = 0;
		System.out.println(startTime);
		//if time to response exceeds timeout, will enter if statement and resend data
		if((System.nanoTime()-startTime)/1000000000 >= timeOut) {		
			retry++;
			sendData = packet(domainName,queueType);
			System.out.println("timeout");
			System.out.println(startTime);
					
		}
		
		// Create a packet structure to store data sent back by the server
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		// Receive data from the server
		clientSocket.receive(receivePacket);
		
		String ansRecords = answerRecords(receiveData); 
		
		String finaldata = Arrays.toString(receiveData);
		System.out.println(finaldata);
		String []data_type = dataType(receiveData);
		String type = data_type[0];
		String data = data_type[1];
		
		String cache = cacheTTL(receiveData);
		System.out.println(cache);
		
		String AA = autho(receiveData);
		System.out.println(AA);
		
		String addRecords = additionalRecords(receiveData); 
		System.out.println(addRecords);
		
		
		long endTime = System.currentTimeMillis();
		startTime = startTime/1000;
		endTime = endTime/1000;
	
		
		System.out.println("Dns client sending request for "+ domainName);
		System.out.println("Server: " + IPstring[0]+"." +IPstring[1]+"." +IPstring[2]+"."+IPstring[3]);
		System.out.println("Request Type: " + queueType);
		
		System.out.println("\n"+ "Response received after " + ((long)(endTime - startTime))/1000 + " seconds" +" ("+retry +") retries");
		
		System.out.println("\n" + "***Answer Section ("+ansRecords +" records)***");
		
		if(type.equals("A")){		
			System.out.println("IP" + "\t" +data +"\t" + cache + "\t" +AA); //need to add IP adress from packet
		}
		
		
		// Close the socket
		clientSocket.close();
			}
		
	
	
	
	public static byte[] parseIP(String in) {
		String[] IP = in.split("@"); // to get rid of @ in input
		
		int onlyIP = 1;  			//pointer to location of IP without @ in string []
		System.out.println(IP[onlyIP]);
		IP = IP[onlyIP].split("\\.");   // splits string by decimal points
		
		byte [] IPbyte = new byte[4];	// 4 for size byte addr required for IPv4 addresses
		int counter = 0;
		for(String IPs : IP) {
			IPbyte[counter]=(byte)Integer.parseInt(IPs);
			//IPbyte[counter]= (byte) (IPbyte[counter] & 0xFF); not necessary here. but necessary when printing later on.
			// masking with 0xFF is for values over 127
			counter++;
		}
		return IPbyte;
	}
	
	//method to create a DNS packet
	public static byte[] packet(String  domainName, String qtype) {
		
		
		byte[] pack = new byte[512];	//creates space in byte buffer for 512 byte[] array

		//DNS Packet Header
		//ID field contains 2 bytes,generates 2 random byte to fill ID field
		byte[] ID = new byte[2];				
		new Random().nextBytes(ID);
		
		// ID field = xxxxxxxx xxxxxxxx - in 2 bytes
		pack[0] = ID[0];
		pack[1] = ID[1];
		
		////QR OPcode AA TC RD = line2_1 = 1 -- 00000001 - in byte   00000x00
		byte line2_1 = 1;
		pack[2] = line2_1;
		
		//Z and Rcode == line2_2 = 0 -- 00000000 in byte
		byte line2_2 = 0;
		pack[3] = line2_2;
		
		//QDcount = 1 -- 00000000 00000001 in byte
		byte QDCOUNT1 = 0;
		byte QDCOUNT2 = 1;
		pack[4] = QDCOUNT1;
		pack[5] = QDCOUNT2;
		
		//ANCOUNT =0 - 00000000 00000000
		byte ANCOUNT = 0;
		pack[6] = ANCOUNT;
		pack[7] = ANCOUNT;
		
		
		//NSCOUNT = 0 - 000000000 00000000
		byte NSCOUNT = 0;
		pack[8]= NSCOUNT;
		pack[9]= NSCOUNT;
		
		//ARCOUNT 00000000 00000000
		byte ARCOUNT = 0;
		pack [10]= ARCOUNT;
		pack [11] = ARCOUNT;
		
		
		
		// DNS Questions
		
		//Qname field 
		String [] labels = domainName.split("\\.");					//Splits domain name by "." intervals
		System.out.println(labels[0]); //www
		System.out.println(labels[1]); //mcgill
		System.out.println(labels[2]); //ca
		
		int qLocation = 12;    										//location of start of qname field
		for (int i=0; i<labels.length;i++){
			pack[qLocation++] = ((byte)labels[i].length());
			//System.out.println("outer loop");
			
			for (int j=0; j<labels[i].length(); j++){
				byte domainByte = (byte)labels[i].charAt(j);
				pack[qLocation++]= domainByte;
				//System.out.println("inner loop");
			}	

		}		
		
		byte endQName = 0;										// marking end of Qname
		pack [qLocation++] = endQName ;
		
		// Qtype field
		byte qtype1 = 0;
		pack [qLocation++] = qtype1;
		
		byte A = 1;
		byte NS = 2;
		byte MX = 15;
		
		if(qtype.equals("A")) {
			pack [qLocation++] = A;
			
		}
		
		else if(qtype.equals("NS")) {
			pack [qLocation++] = NS;
			
		}
		else if(qtype.equals("MX")) {
			pack [qLocation++] = MX;
			
		}
		
		//Qclass field
		
		pack [qLocation++] = 0;
		pack [qLocation++] = 1;
		
		return pack;
	}
	
	// method to determine data type and also the data in the answer in the response method
	public static String[] dataType(byte [] receive) {
		int nameStart = 12;
		String type ="";
		String data = "";
		String output [] = {(type),(data)};
		
		// while loop to cycle through name in packet until end of domain name
		while((receive[nameStart] != 0)) {
			nameStart++;			
		}
		//offset by 8 to get to location of type field
		nameStart= nameStart +8;			
		if((receive[nameStart] == 1)) {
			type = "A";	
			nameStart = nameStart+9;	//offset to location of RData
			int i =1;
			while(i<5) {
				data = data + (0xFF &receive[nameStart]);
				System.out.println(receive[nameStart]);
				nameStart++;
				i++;
				if(i<5) {
					data = data + ".";
				}
			}	
		}
		else if((receive[nameStart] == 2)) {
			type = "NS";
			
		}
		else if((receive[nameStart] == 15)) {
			type = "MX";
			
		}
		else if((receive[nameStart] == 5)) {
			type = "CNAME";
			
		}
		output[0] = type;
		output[1] = data;
		
		
		return output;
	}
	
	public static String cacheTTL(byte [] receive) {
		int nameStart = 12;
		String cache ="";
		while((receive[nameStart] != 0)) {
			nameStart++;	
		}
		nameStart= nameStart +5;   // to get to array with TLL
		byte [] TTL = new byte[4];
		TTL[0] = (byte) (receive[nameStart] & 0xFF);
		TTL[1] = (byte) (receive[nameStart+1] & 0xFF);
		TTL[2] = (byte) (receive[nameStart+2] & 0xFF);
		TTL[3] = (byte) (receive[nameStart+3] & 0xFF);
		cache = ""+TTL[0]+""+TTL[1]+""+TTL[2]+""+TTL[3];
		return cache;
	}
	
	public static String autho(byte [] receive) {
		int authostart = 3;
		byte x = receive[authostart];
		String autho = byteToBin(x);
		char AA = autho.charAt(5);
		//System.out.println(AA);
		if(AA == '0') {
			autho = "non-authoritative";

		}
		
		if(AA == '1') {
			autho = "authoritative";

		}
		
		return autho;
	}
	public static String answerRecords(byte [] receive) {
		int recordArray = 7;
		byte x = receive[recordArray];
		String number = ""+x;
		
		return number;
	}
	
	public static String additionalRecords(byte [] receive) {
		int recordArray = 11;
		String number = "";
		byte x = receive[recordArray];
		if(x == 0) {
			number = "NOTFOUND";
			return number;
		}
		number = ""+x;
		
		return number;
	}
	
	public static String byteToBin(byte byteIn) {
    	String binOut = "";
    	if (byteIn < 0) {
    		int intIn = byteIn;
    		intIn = intIn + 256;  
    		binOut = String.format("%8s", Integer.toBinaryString(intIn & 0xFF)).replace(' ', '0');
    	}
    	else {
        	binOut = String.format("%8s", Integer.toBinaryString(byteIn & 0xFF)).replace(' ', '0');
    	}
    	return binOut;
    }
}

