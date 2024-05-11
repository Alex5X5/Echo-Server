package serverTest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import SimpleLogging.Logging.*;
//import consoleWindow.*;

public class EchoServer {
	
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private ArrayList<Integer> ports = new ArrayList<Integer>();
	private int basePort = 900;
	private static final LoggingLevel mLvl= new LoggingLevel("Echo Server");
	
	public EchoServer () {
		Logging.setStartTime();
//		@SuppressWarnings("unused")
		Console8 console = new Console8("Echo Server",false);
		console.addCommandHandler(
				(s,c)->{
					this.handleCommandInput(s);
				}
		);
		try{Thread.sleep(500);} catch(InterruptedException e) {}
		
		for(int i=0; i<10; i++) {
			ports.add(basePort+i);
			threads.add(new Thread());
//			Logging.buildLogMessage(mLvl,new ActionMessage("added new Thread "),new MessageParameter("pos",i));
		}	
		Logging.buildLogMessage(mLvl,new ActionMessage("main"));
		
		while(true) {
			threads.forEach(
				(th)->{
					if(!th.isAlive()) {
						int a = threads.indexOf(th);
						threads.set(
							a, 
							new Thread(
								()->{
									searchConnection(ports.get(a));
								}
							)
						);
						threads.get(a).setDaemon(true);
						threads.get(a).start();
					}
				}
			);
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		EchoServer sv = new EchoServer();
	}
	
	private void searchConnection(int s) {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;	
		LoggingLevel sLvl = new LoggingLevel("Connection thread "+(s-this.basePort+1));
		try {
			Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("trying to connect to client "),new MessageParameter("port",s));
			serverSocket =	new ServerSocket(s);
			clientSocket = serverSocket.accept();
			Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("connected"));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String inputLine;
			try {
				serverSocket.close();
			} catch (IOException e) {}
			while ((inputLine = in.readLine()) != null) {
				out.println(inputLine);
				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("echoing "),new MessageParameter("message",inputLine));
			}
			out.close();
			in.close();
		} catch (IOException e) {
			if(e.getMessage()=="Connection reset") {
				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("disconnected from client "),new MessageParameter("port",s));
			} else {
				System.out.println(e.getMessage());
			}
				
//			System.out.println("Exception caught when trying to listen on port "
//				+ s + " or listening for a connection");
//			System.out.println(e.getMessage());
		}
		try {
			serverSocket.close();
			clientSocket.close();
		} catch (IOException e) {}
	}
	
	private void handleCommandInput(String input) {
		if(input.startsWith("/")) {
			ArrayList<String> keys = new ArrayList<String>();
//			Logging.buildLogMessage(mLvl, new ActionMessage("handling console event"), new MessageParameter("message",input));	
			StringBuilder sb = new StringBuilder(input+" ");
			sb.deleteCharAt(0);
			input = sb.toString();
			int i = 0;
			int k = 0;
			String command = "";
			while(input.charAt(i)!=' '&&i<input.length()-1) {
				command = command+input.charAt(i);
				i++;
			}
//			Logging.buildLogMessage(mLvl, new ActionMessage("extracted command from intput"), new MessageParameter("command",command));
			while(i<input.length()-1){
				i++;
				keys.add("");
				while(input.charAt(i)!=' '&&i<input.length()-1) {
					keys.set(k, keys.get(k)+input.charAt(i));
					i++;
				}
//				Logging.buildLogMessage(mLvl, new ActionMessage("extracted key from intput"), new MessageParameter("key",keys.get(k)), new MessageParameter("k",k));
				k++;
			}
//			Logging.buildLogMessage(mLvl, new ActionMessage("extracted keys"), new MessageParameter("list length",keys.size()));
			switch(command) {
				case"ip":{
					onIpCommand();
					break;
				}
				case"bport":{
					onBPortCommand(keys);
					break;
				}
				case"port":{
					onPortCommand(keys);
					break;
				}
			}
		} else {
//			Logging.buildLogMessage(mLvl, new ActionMessage("passing plain text"), new MessageParameter("message",input));	
		}
	}
	
	private void onIpCommand() {
		Logging.buildLogMessage(mLvl, new ActionMessage("getting ip adress"));		
	}
	
	private void onBPortCommand(ArrayList<String> params) {
		Logging.buildLogMessage(mLvl, new ActionMessage("changing base Port"));
		this.basePort = Integer.parseInt(params.get(0));
	}
	
	private void onPortCommand(ArrayList<String> params) {
		try {
			int a = this.ports.indexOf(Integer.parseInt(params.get(0)));
			this.ports.set(a,Integer.parseInt(params.get(1)));
			Logging.buildLogMessage(mLvl, new ActionMessage("changing specific port"), new MessageParameter("old port",params.get(0)), new MessageParameter("new port",params.size()));
		} catch (IndexOutOfBoundsException e) {
			Logging.buildLogMessage(mLvl, new ActionMessage("no such port in use"));
		}
	}
}