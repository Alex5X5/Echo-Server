package serverTest.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import SimpleLogging.Logging.*;
//import consoleWindow.*;
import serverTest.Console8;

public class EchoServer3 {
	
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private ArrayList<Integer> ports = new ArrayList<Integer>();
	private int basePort = 900;
	private static final LoggingLevel mLvl= new LoggingLevel("Echo Server");
	
	public EchoServer3 () {
		Logging.setStartTime();
//		@SuppressWarnings("unused")
		Console8 console = new Console8("Echo Server",true);
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
		EchoServer3 sv = new EchoServer3();
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
				case"gip":{
					onGipCommand();
					break;
				}
				case"bpswitch":{
					onBPortCommand(keys);
					break;
				}
				case"pswitch":{
					onPortSwitchCommand(keys);
					break;
				}
				case"plist":{
					onPortListCommand(keys);
					break;
				}
				case"restart":{
					onRestartCommand(keys);
					break;
				}
				case"shutdown":{
					onRestartCommand(keys);
					break;
				}
				default:{
					Logging.buildLogMessage(mLvl, new ActionMessage("this command does not exist"));
					Logging.buildLogMessage(mLvl, new ActionMessage("available commands: /gip /plist /pswitch /bpswitch /restart /shutdown"));}
			}
		} else {
			Logging.buildLogMessage(mLvl, new ActionMessage("this command does not exist"));
			Logging.buildLogMessage(mLvl, new ActionMessage("available commands: /gip /plist /pswitch /bpswitch /restart /shutdown"));
		}
	}
	
	private void onGipCommand() {
		try{
			String name = String.valueOf(InetAddress.getLocalHost().getHostAddress());
			Logging.buildLogMessage(mLvl, new MessageParameter("localhost",name));
		}catch(UnknownHostException e){
			Logging.buildLogMessage(mLvl, new ActionMessage("could not get localhost"),new MessageParameter(e.getMessage()));
			
		}		
	}
	
	private void onBPortCommand(ArrayList<String> params) {
		try {
			Logging.buildLogMessage(mLvl, new ActionMessage("changing base Port"));
			this.basePort = Integer.parseInt(params.get(0));
		} catch (NumberFormatException fe) {
			Logging.buildLogMessage(mLvl, new ActionMessage("use format /bpswitch <int new port>"));
		}
	}
	
	private void onPortSwitchCommand(ArrayList<String> params) {
		try {
			try {
				int a = this.ports.indexOf(Integer.parseInt(params.get(0)));
				this.ports.set(a,Integer.parseInt(params.get(0)));
				Logging.buildLogMessage(mLvl, new ActionMessage("changing specific port"), new MessageParameter("old port",params.get(0)), new MessageParameter("new port",params.get(1)));
			} catch (NumberFormatException fe) {
				Logging.buildLogMessage(mLvl, new ActionMessage("use format /port <int old Port> <int new Port>"));
			} catch (IndexOutOfBoundsException ie) {
				Logging.buildLogMessage(mLvl, new ActionMessage("no such port in use"), new MessageParameter("port",params.get(0)));
				Logging.buildLogMessage(mLvl, (MessageEntry)new ActionMessage("currently used ports"));
				for(int i=0; i<ports.size()-1; i++) {
				Logging.buildLogMessage(mLvl, (MessageEntry)new MessageParameter(ports.get(i)));
				}
			}
		} finally {}
	}
	
	private void onPortListCommand(ArrayList<String> params) {
		for(int i=0; i<ports.size()-1; i++) {
			Logging.buildLogMessage(mLvl, (MessageEntry)new MessageParameter(ports.get(i)));
			}
	}
	
	private void onRestartCommand(ArrayList<String> params) {
		
	}
	
	private class EchoConnection{
//		public boolean stop = false;
//		public boolean alive = true;
		public ServerSocket serverSocket;
		private Socket clientSocket;
		
		private Thread mt;
		
		public EchoConnection(EchoServer3 es, int i) {
			
			try {serverSocket =	new ServerSocket(i);} catch (IOException e) {}
			
			Thread st = new Thread(
					()->{
						while(!serverSocket.isClosed()) {
							if(!mt.isAlive()) {
								mt = new Thread(
										()->{
											searchConnection(i);
										}
									);
								mt.setDaemon(true);
								mt.start();
							}
						}
					}
			);
		}	
		
		private void searchConnection(int i) {
			LoggingLevel sLvl = new LoggingLevel("Connection thread "+i);
			try {
				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("trying to connect to client "),new MessageParameter("port",i));
				
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
					Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("disconnected from client "),new MessageParameter("port",i));
				} else {
					System.out.println(e.getMessage());
				}
					
//				System.out.println("Exception caught when trying to listen on port "
//					+ s + " or listening for a connection");
//				System.out.println(e.getMessage());
			}
			try {
				serverSocket.close();
				clientSocket.close();
			} catch (IOException e) {
					
			}
		}
	}
}