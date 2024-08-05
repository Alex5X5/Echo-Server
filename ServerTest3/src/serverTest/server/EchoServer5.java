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

import javax.swing.JFrame;

import SimpleLogging.Logging.*;
//import consoleWindow.*;
import serverTest.Console9;

public class EchoServer5 {
//public class EchoServer5 extends ServerSocket {
	
	private ArrayList<EchoServerConnection> connections = new ArrayList<EchoServerConnection>();
	private ArrayList<Integer> ports = new ArrayList<Integer>();
	private int basePort = 900;
	private static final LoggingLevel mLvl= new LoggingLevel("Echo Server");
	private Console9 console;
//	cn
	
	private int portUsedFails = 0;
	
	public EchoServer5 () {
//		super()
		Logging.setStartTime();
//		@SuppressWarnings("unused")
		console = new Console9("Echo Server", true, true);
		console.setDefaultCloseOperation(0);
		Logging.disableDebug();
		console.addCommandHandler(
				(s,c)->{
					this.handleCommandInput(s);
				}
		);
		try{Thread.sleep(1000);} catch(InterruptedException e) {}
		for(int i=0; i<10; i++) {
			connections.add(new EchoServerConnection(this,this.basePort+i));
			try{Thread.sleep(20);} catch(InterruptedException e) {}
		}
		onPListCommand();
		while(true) {
			if(portUsedFails>=20) {
				onShutdownCommand();
				try{Thread.sleep(200);} catch(InterruptedException e) {}
				portUsedFails--;
			}
		}
//		Logging.buildLogMessage(mLvl,new ActionMessage("main"));
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		EchoServer5 sv = new EchoServer5();
	}
	
	public void handleCommandInput(String input) {
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
					onPListCommand();
					break;
				}
				case"pstart":{
					onPstartCommand(keys);
					break;
				}
				case"restart":{
					onRestartCommand(keys);
					break;
				}
				case"shutdown":{
					onShutdownCommand();
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

	public void onClearCommand(ArrayList<String> keys) {
		this.console.clear();
	}

	public void onGipCommand() {
		try{
			String name = String.valueOf(InetAddress.getLocalHost().getHostAddress());
			Logging.buildLogMessage(mLvl, new MessageParameter("localhost",name));
		}catch(UnknownHostException e){
			Logging.buildLogMessage(mLvl, new ActionMessage("could not get localhost"),new MessageParameter(e.getMessage()));
			
		}		
	}
	
	public void onBPortCommand(ArrayList<String> params) {
		try {
			Logging.buildLogMessage(mLvl, new ActionMessage("changing base Port"));
			this.basePort = Integer.parseInt(params.get(0));
		} catch (NumberFormatException fe) {
			Logging.buildLogMessage(mLvl, new ActionMessage("use format /bpswitch <int new port>"));
		}
	}
	
	public void onPSwitchCommand(ArrayList<String> params) {
		try {
			try {
				if(params.size()<2) {
					Logging.buildLogMessage(mLvl, new ActionMessage("use format /port <int old Port> <int new Port>"));
					return;
				}
				int a = -1;
				for(int i=0; i<connections.size(); i++) {
					if(connections.get(i).port==Integer.parseInt(params.get(0))) a = i;
				}
				if(a==-1) {
					Logging.buildLogMessage(mLvl, new ActionMessage("no such port in use"),new MessageParameter("port",Integer.parseInt(params.get(0))));
					return;
				}
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
	
	public void onPstartCommand(ArrayList<String> params) {
		try {
			int a = Integer.parseInt(params.get(0));
			boolean exists = false;
			for(int i=0; i<connections.size(); i++) {
				if(connections.get(i).getPort()==a)exists = true;
			}
			if(!exists) {
				connections.add(new EchoServerConnection(this,a));
				Logging.buildLogMessage(mLvl, new ActionMessage("opening new port"),new MessageParameter("port",a));
			} else {
				Logging.buildLogMessage(mLvl, new ActionMessage("port already open"),new MessageParameter("port",a));
			}
		} catch (NumberFormatException fe) {
			Logging.buildLogMessage(mLvl, new ActionMessage("use format /pstart <int new port>"));
		}
	}
	
	public void onPListCommand() {
		Logging.buildLogMessage(mLvl, new MessageParameter("active connections",connections.size()));
		for(int i=0; i<connections.size(); i++) {
			Logging.buildLogMessage(mLvl, new MessageParameter("port",connections.get(i).port), new MessageParameter("open",connections.get(i).isOpen()), new MessageParameter("connected",connections.get(i).hasConnection()));
		}
	}
	
	public void onRestartCommand(ArrayList<String> params) {
		Logging.buildLogMessage(mLvl,new ActionMessage("restarting server"));
		ArrayList<Integer> pts = new ArrayList<Integer>();
		connections.forEach(c->pts.add(c.port));
		connections.forEach(c->c.shutdown=true);
		try{Thread.sleep(1000);} catch(InterruptedException e) {}
		connections.clear();
		for(int i=0; i<pts.size(); i++) {
			connections.add(new EchoServerConnection(this, pts.get(i)));
//			Logging.buildLogMessage(mLvl,new ActionMessage("added new Thread "),new MessageParameter("pos",i));
		}
	}
	
	private void onShutdownCommand() {
		Logging.buildDebugMessage(mLvl, new ActionMessage("shutting down"));
		connections.forEach(c->c.shutdown=true);
		try{Thread.sleep(3000);} catch(InterruptedException e) {}
		connections.clear();
		System.exit(JFrame.EXIT_ON_CLOSE);
	}
	
	private class EchoServerConnection{
		public boolean shutdown = false;
		public boolean restart = false;
		
		private ServerSocket serverSocket;
		private Socket clientSocket;
		
		private int port;
		
		private Thread connectionThread = new Thread();
		
		public EchoServerConnection(EchoServer5 es, int p) {
			this.port = p;
			//here a new server socket is opened on a new thread
			new Thread(
					()->{
//						Logging.buildLogMessage(mLvl,new LoggingLevel("Connection thread "+this.port),new ActionMessage("restarting connection thread"),new MessageParameter("port",this.port));
						createSocket();
						while(true) {					
							if(!connectionThread.isAlive()) {
								connectionThread = new Thread(
										()->{
											//searching for a client to connect to server socket
//											searchConnection();
											openSocket(es);
											//if the socket is not closed start the communication with the client
											if(!serverSocket.isClosed()) {
												communicate();
											}
										}
								);
//								mt.setDaemon(true);
								connectionThread.start();
							}
							
							if(this.shutdown) {
								try {
									if(serverSocket!=null)serverSocket.close();
									if(clientSocket!=null)clientSocket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							}
							
							if(this.restart) {
								this.restart = false;
								try {
									if(serverSocket!=null)serverSocket.close();
									if(clientSocket!=null)clientSocket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							}
							//try{Thread.sleep(1000);} catch(InterruptedException e) {}
						}
					}
			).start();
		}
		
		private void createSocket() {
			int i = 0;
			while(serverSocket == null) {
				try {
					i++;
					serverSocket =	new ServerSocket();
				} catch (IOException e) {
					if(i>0) {
						Logging.buildLogMessage(mLvl,new LoggingLevel("Connection thread "+this.port),new ActionMessage("failed to create server socket"),new MessageParameter("reason",e.getMessage()));
						return;
					}
				}
			}
		}
		
		private void openSocket(EchoServer5 es) {
//			Logging.buildLogMessage(mLvl,new LoggingLevel("Connection thread "+this.port),new ActionMessage("opening server socket"),new MessageParameter("port",this.port));
			int i = 0;
			while(serverSocket.isClosed()) {
				try {
					i++;
					serverSocket =	new ServerSocket(this.port);
					serverSocket.setSoTimeout(3000);
				} catch (IOException e) {
					if(i>10) {
						Logging.buildLogMessage(mLvl,new LoggingLevel("Connection thread "+this.port),new ActionMessage("failed to open server socket"),new MessageParameter("reason",e.getMessage()));
						if(e.getMessage()!="Address already in use: bind") {
							es.portUsedFails++;
						} else {
							System.out.println("wrong message");
							System.out.println(e.getMessage());
							System.out.println("Address already in use: bind");
						}
						return;
					}
				}
			}
		}

		private void communicate() {
			LoggingLevel sLvl = new LoggingLevel("Connection thread "+this.port);
			try {
//				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("trying to connect to client "),new MessageParameter("port",port));
				//trying to connect to client
				clientSocket = serverSocket.accept();
				//if the search lasted for 3 seconds without success stop searching for client
				if(!clientSocket.isConnected()) return;
				Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("connected"));
				//setting up readers and writers
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String inputLine;
				//close the server socket for other clients
				try {
					serverSocket.close();
				} catch (IOException e) {}
				
				//reading messages from client
				while ((inputLine = in.readLine()) != null) {
					if(this.restart|this.shutdown) {
						Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("stopping thread"));
						restart=false;
						break;
					}
					//returning the same message to the client
					out.println(inputLine+" from server");
					Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("echoing "),new MessageParameter("message",inputLine));
				}
				//close readers and writers after successful communication
				out.close();
				in.close();
			} catch (IOException e) {
				if(e.getMessage()=="Connection reset") {
					Logging.buildLogMessage(mLvl,sLvl,new ActionMessage("disconnected from client "),new MessageParameter("port",port));
				} else {}
			}
			
			//closing the sockets
			try {
				serverSocket.close();
				if(clientSocket!=null)clientSocket.close();
			} catch (IOException e) {}
		}
		
		public boolean hasConnection() {
			if(clientSocket==null) return false;
			else return clientSocket.isConnected();
		}
		
		public boolean isOpen() {
			if(serverSocket==null) return false;
			else return !serverSocket.isClosed();
		}
		
		public int getPort() {
			return this.port;
		}
		
		public void setPort(int p){
			this.port = p;
		}
	}
}
