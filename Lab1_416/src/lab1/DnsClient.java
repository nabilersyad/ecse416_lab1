package lab1;
/**
 * UDPClient
 * 
 * Adapted from the example given in Section 2.8 of Kurose and Ross, Computer
 * Networking: A Top-Down Approach (5th edition)
 * 
 * @author michaelrabbat
 * 
 */
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.nio.ByteBuffer;

public class DnsClient {
	
	
	

	public static void main(String args[]) throws Exception{
		
		
		int port = 53;
		// Open a reader to input from the command line
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		// Create a UDP socket
		// (Note, when no port number is specified, the OS will assign an arbitrary one)
		DatagramSocket clientSocket = new DatagramSocket();
		
		
		// Allocate buffers for the data to be sent and received
		byte[] sendData = new byte[1024];		
		byte[] receiveData = new byte[1024];

		// User inputs query in the format required
		/////////
		
		System.out.println("Enter IP address");
		
		args[0] = inFromUser.readLine();
		System.out.println(args[0]);
		
		byte[] IP = parseIP(args[0]);
		
		System.out.println(IP[0]&0xFF);
		System.out.println(IP[1]&0xFF);
		System.out.println(IP[2]);
		System.out.println(IP[3]);
		InetAddress ipAddress = InetAddress.getByAddress(IP);
	     //Returns an InetAddress object given the raw IP address . The argument is in network byte order: the highest order byte of the address is in getAddress()[0].
		
		System.out.println("line 50");
		
		//second args is domain name
		
		System.out.println("Enter Domain Name");
		args[1] = inFromUser.readLine();
		//System.out.println(args[1]);
		
		String sentence = args[0] + " " + args[1];
		
		System.out.println(sentence);
		
		/// HERE I THINK SEND DATA SHOULD BE THE IN THE DNS PACKET FORMAT. 
		
		//sendData = sentence.getBytes();
		sendData = packetHeader();
		System.out.println(sendData[0]);
		System.out.println(sendData[1]);
		System.out.println(sendData[2]);
		System.out.println(sendData[3]);
		System.out.println(sendData[4]);
		System.out.println(sendData[5]);
		System.out.println(sendData[6]);
		//System.out.println(Arrays.toString(sendData));
		System.out.println(51);
		
		
		
		// Create a UDP packet to be sent to the server
		// This involves specifying the sender's address and port number
		//DatagramPacket(byte[] data to send,array data length,IPaddress,port number)
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
		System.out.println(56);
		// Send the packet
		clientSocket.send(sendPacket);
		System.out.println(59);
		// Create a packet structure to store data sent back by the server
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		System.out.println(62);
		// Receive data from the server
		clientSocket.receive(receivePacket);
		System.out.println(65);
		// Extract the sentence (as a String object) from the received byte stream
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("From Server: " + modifiedSentence);
		
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
	
	// have to create methods that will call create a packet header
	public static byte[] packetHeader() {
		
		// DNS PACKET HEADER for query
				/* 
				      ID 		16 bits 	random                         
				      QR 		1 bit 		0(query) or 1(response) 
				      Opcode  	4 bits		0 
				      AA		1 bit		0 or 1 (non or authoritative response) (only meaningful in response)
				      TC		1 bit		0 i think
				      RD		1 bit		1
				      RA   		1 bit		0
				      Z     	3 bits		0.
				      RCODE   	4 bits		0. ** Implement all 5 kinds** (only meaningful in response)
				      QDCOUNT  	16 bits		1                  
				      ANCOUNT	16 bits 	0                    
				      NSCOUNT   16 bits		0 ignore                 
				      ARCOUNT   16 bits     0   */   

				
				// ID
		
		
		ByteBuffer buf = ByteBuffer.allocate(512);	//creates space in byte buffer for 512 byte[] array
		byte ID1 = (byte)Math.random();				//Creates ID for field 1
		byte ID2 = (byte)Math.random();				//creates ID
		
		// ID field = xxxxxxxx xxxxxxxx - in 2 bytes
		buf.put(0, ID1);
		buf.put(1, ID2);
		
		////QR OPcode AA TC RD = line2_1 = 1 -- 00000001 - in byte
		byte line2_1 = 1;
		buf.put(2, line2_1);
		
		//Z and Rcode == line2_2 = 0 -- 00000000 in byte
		byte line2_2 = 0;
		buf.put(3,line2_2);
		
		//QDcount = 1 -- 00000000 00000001 in byte
		byte QDCOUNT1 = 0;
		byte QDCOUNT2 = 1;
		buf.put(4, QDCOUNT1);
		buf.put(5, QDCOUNT2);
		
		//ANCOUNT =0 - 00000000 00000000
		byte ANCOUNT = 0;
		buf.put(6, ANCOUNT);
		buf.put(7, ANCOUNT);
		
		
		//NSCOUNT = 0 - 000000000 00000000
		byte NSCOUNT = 0;
		buf.put(8, NSCOUNT);
		buf.put(9, NSCOUNT);
		
		//ARCOUNT 00000000 00000000
		byte ARCOUNT = 0;
		buf.put(10, ARCOUNT);
		buf.put(11, ARCOUNT);
		
		
		byte[] x = new byte[1];
		buf.put(x);
		
		return x;
	}
	
	//just a comment line to test github branch
	//ID field contains 2 bytes. thus occupy byte array spots
	// have to use bit manipulation techniques to change values
	//dns query size should be 62 bytes	
}
