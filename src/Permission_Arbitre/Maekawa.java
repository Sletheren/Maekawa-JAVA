package Permission_Arbitre;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;



public class Maekawa extends Thread {
	int[] ports = {1111,2222,3333,4444};
	boolean voted=false,accessing=false;
	private int answers = 0;
	LinkedList<Client> Ri = new LinkedList<Client>();
	LinkedList<Integer> onhold = new LinkedList<>();

	int ID;
	int port;
	
	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		Maekawa p = new Maekawa(port);
		p.start();
		p.createServer();

	}
	public Maekawa(int port){
		this.port=port;
		this.ID=port;
	}
	public void run() {
		PrintWriter pw;
		try {
			sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int port:ports){
			try {
				if(port != ID){
					Socket s = new Socket("127.0.0.1", port);
					Client p = new Client(s, port);
					pw = new PrintWriter(s.getOutputStream(), true);
					pw.println(ID);
					p.start();
				}
				
			} catch (Exception e) {
				System.out.println("Error Connecting with Other processes");
			}
		}
		
		while(true){
			try {
				System.in.read();
				voted=true;
				sendtoRi("ask");
				System.out.println("Asking Processes... ");
				while(true){
					sleep(1500);
					if(answers == ports.length-1)
					{
						accessing=true;
						System.out.println("Accessing Critical Section...");
						sleep(5000);
						System.out.println("Done Working on Critical Section!");
						sendtoRi("free");
						if(onhold.isEmpty())
							voted = false;
						else{
							int p = onhold.removeFirst();
							sendTo(p,"ok");
						}
						answers = 0;
						accessing=false;
						break;
					}
					
				}
			} catch (Exception e) {
				e.getMessage();
			}
		}

	}
	public void createServer() {
		try {
			ServerSocket server = new ServerSocket(port);
			Client p;
			
			while (true) {
				Socket s = server.accept();
				BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
				int id = Integer.parseInt(input.readLine());
				p = new Client(s,id);
				Ri.add(p);
				System.out.println(id + " is Successfully Connected.");
				sleep(500);
				p.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendtoRi(String message) {
        for (Client p : Ri)  
                p.sendMessage(ID+":"+message);
    }
	public void sendTo(int x,String message){
		 for (Client p : Ri)  
			 if(p.getIdP() == x){
				 p.sendMessage(ID+":"+message);
				 break;
			 }
	}
	class Client extends Thread {
		BufferedReader input;
		PrintWriter output;
		String msg;
		int id;

		public Client(Socket client, int id) {
			this.id = id;
			try {
				output = new PrintWriter(client.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public int getIdP() {
			return id;
		}
		public void sendMessage(String str) {
			output.println(str);
		}
	 	public void run() {
			while (true) {
				try {
					String msg = input.readLine();
					System.out.println("Message Received : " + msg);
					String messageArray[] = msg.split(":");
					
					if (messageArray[1].equals("ask")) {
						if(voted || accessing){
							onhold.add(Integer.parseInt(messageArray[0]));
							System.err.println(onhold.toString());
						}
						else
							sendTo(Integer.parseInt(messageArray[0]),"ok");
					} 
					else if(messageArray[1].equals("ok")){
						answers++;
						System.out.println("Incrementing Answer");
					}
					else if(messageArray[1].equals("free")){
						if(onhold.isEmpty())
							voted = false;
						else{
							int p = onhold.removeFirst();
							sendTo(p,"ok");
						}
					}
				} catch (IOException e) {
					System.out.println(id+"Error! Socket will be closed immediatly");
					break;	
				}

			}
		}

	}
}
