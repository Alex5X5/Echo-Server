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

public class EchoServer4 {
	
	private ArrayList<EchoConnection> connections = new ArrayList<EchoConnection>();
	private ArrayList<Integer> ports = new ArrayList<Integer>();
	private int basePort = 900;
	private static final LoggingLevel mLvl= new LoggingLevel("Echo Server");
	private Console8 console;
	
	public EchoServer4 () {
		Logging.setStartTime();
//		@SuppressWarnings("unused")
		console = new Console8("Echo Server",true);
		console.addCommandHandler(
				(s,c)->{
					this.handleCommandInput(s);
				}
		);
		try{Thread.sleep(500);} catch(InterruptedException e) {}
		
		for(int i=0; i<10; i++) {
			ports.add(basePort+i);
			connections.add(new EchoConnection(this, ports.get(i)));
			try {Thread.sleep(100);} catch (InterruptedException e) {}
//			Logging.buildLogMessage(mLvl,new ActionMessage("added new Thread "),new MessageParameter("pos",i));
		}	
//		Logging.buildLogMessage(mLvl,new ActionMessage("main"));
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		EchoServer4 sv = new EchoServer4();
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
					onPSwitchCommand(keys);
					break;
				}
				case"plist":{
					onPListCommand(keys);
					break;
				}
				case"pstart":{
					onRestartCommand(keys);
					break;
				}
				case"restart":{
					onRestartCommand(keys);
					break;
				}
				case"shutdown":{
					onShutdownCommand(keys);
					break;
				}
				case"clear":{
					onClearCommand(keys);
					break;
				}
				default:{
					Logging.buildLogMessage(mLvl, new ActionMessage("this command does not exist"));
					Logging.buildLogMessage(mLvl, new ActionMessage("available commands: /gip /plist /pswitch /bpswitch /restart /shutdown /clear /pstart"));
					}
			}
		} else {
			Logging.buildLogMessage(mLvl, new ActionMessage("this command does not exist"));
			Logging.buildLogMessage(mLvl, new ActionMessage("available commands: /gip /plist /pswitch /bpswitch /restart /shutdown /clear /pstart"));
		}
	}

	private void onClearCommand(ArrayList<String> keys) {
		this.console.clear();
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
	
	private void onPSwitchCommand(ArrayList<String> params) {
		try {
			try {
				int a = this.ports.indexOf(Integer.parseInt(params.get(0)));
				this.connections.get(a).setPort(Integer.parseInt(params.get(0)));
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
	
	private void onPstartCommand(ArrayList<String> params) {
		try {
			Logging.buildLogMessage(mLvl, new ActionMessage("changing base Port"));
			this.basePort = Integer.parseInt(params.get(0));
		} catch (NumberFormatException fe) {
			Logging.buildLogMessage(mLvl, new ActionMessage("use format /bpswitch <int new port>"));
		}
	}
	
	private void onPListCommand(ArrayList<String> params) {
		for(int i=0; i<connections.size(); i++) {
			Logging.buildLogMessage(mLvl, new MessageParameter("port",connections.get(i).port), new MessageParameter("open",connections.get(i).isOpen()), new MessageParameter("bound",connections.get(i).hasConnection()));
		}
	}
	
	private void onRestartCommand(ArrayList<String> params) {
		Logging.buildLogMessage(mLvl,new ActionMessage("restarting server"));
		connections.forEach(c->c.restart=true);
	}
	
	private void onShutdownCommand(ArrayList<String> keys) {
		connections.forEach(c->c.stop=true);
	}
	
	private class EchoConnection{
		public boolean stop = false;
		public boolean restart = false;
//		public boolean alive = true;
		public ServerSocket serverSocket;
		private int port;
		
		private Socket clientSocket;
		private Thread mt = new Thread();
		
		public EchoConnection(EchoServer4 es, int p) {
			this.port = p;
			new Thread(
					()->{
						int i = 0;
						while(serverSocket == null) {
							try {
								i++;
								serverSocket =	new ServerSocket(this.port);
							} catch (IOException e) {
								if(i>10) {
									Logging.buildLogMessage(mLvl,new LoggingLevel("Connection thread "+p),new ActionMessage("failed to open server socket"),new MessageParameter("reason",e.getMessage()));
									return;
								}
							}
						}
						while(!stop) {
							if(!mt.isAlive()) {
								Logging.buildLogMessage(mLvl,new LoggingLevel("Connection thread "+p),new ActionMessage("restarting connection thread"),new MessageParameter("port",port));
								mt = new Thread(
										()->{
											searchConnection();
										}
									);
								mt.setDaemon(true);
								mt.start();
							}
						}
					}
			).start();
		}	
		
		public boolean hasConnection() {
			if(clientSocket==null) return false;
			else return clientSocket.isConnected();
		}
		
		public boolean isOpen() {
			if(serverSocket==null) return false;
			else return !serverSocket.isClosed();
		}

		private void searchConnection() {
			LoggingLevel sLvl = new LoggingLevel("Connection thread "+port);
			try {
				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("trying to connect to client "),new MessageParameter("port",port));
				
				clientSocket = serverSocket.accept();
				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("connected"));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String inputLine;
				try {
					serverSocket.close();
				} catch (IOException e) {}
				while ((inputLine = in.readLine()) != null) {
					if(this.restart|this.stop) {
						Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("stopping thread"));
						restart=false;
						break;
					}
					out.println(inputLine);
						Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("echoing "),new MessageParameter("message",inputLine));
				}
				out.close();
				in.close();
			} catch (IOException e) {
				if(e.getMessage()=="Connection reset") {
					Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("disconnected from client "),new MessageParameter("port",port));
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
		
		public void setPort(int p){
			this.port = p;
		}
	}
}