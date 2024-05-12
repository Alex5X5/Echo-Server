package serverTest.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.JFrame;

import SimpleLogging.Logging.*;
import serverTest.Console9;

public class EchoClient3 {
	public final int port = 900;
	public String name;
	private boolean stop;
	private final LoggingLevel mLvl = new LoggingLevel("Client");
	private Console9 csl;
	private Socket echoSocket;
	
	public EchoClient3() {
		csl = new Console9("Echo Client 2", true, true);
		try{Thread.sleep(500);} catch(InterruptedException e) {}
		this.csl.addCommandHandler((s,c)->handleCommandInput(s));
		int i = 0;
		while(i <10) {
			try{
				name = String.valueOf(InetAddress.getLocalHost().getHostAddress());
//				name="127.0.0.1";
				Logging.buildLogMessage(new LoggingLevel("Client"), new ActionMessage("getting localhost"), new MessageParameter("name",name));
			}catch(UnknownHostException e){
				
			}
			
			int port_ = port + i;
			try {
				Logging.buildLogMessage(mLvl, new ActionMessage("trying to connect to server "), new MessageParameter("name",name), new MessageParameter("port",port_));
				echoSocket = new Socket(name, port_);
				PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader in =	new BufferedReader(
						new InputStreamReader(echoSocket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				Logging.buildLogMessage(mLvl, new ActionMessage("connected "),new MessageParameter("port",port_),new MessageParameter("adress",name));
				String userInput = "";
				while (!stop) {
					
					out.println(userInput);
					System.out.println("echo: " + in.readLine());
				}
				echoSocket.close();
			} catch (UnknownHostException e) {
				Logging.buildLogMessage(mLvl, new ActionMessage("failed to open connection on port "),new MessageParameter(port));
				i++;
			} catch (IOException e) {
				Logging.buildLogMessage(mLvl, new ActionMessage("failed to open connection on port "),new MessageParameter(port));
				i++;
			}
//			Logging.buildLogMessage(mLvl,new MessageParameter(i));
		}
		if(i>=10) {
			Logging.buildLogMessage(mLvl, new ActionMessage("couldn't open any connection"));
//			Logging.buildLogMessage(mLvl, new ActionMessage("exiting"));
//			try {Thread.sleep(5000);} catch(Exception e) {}
//			System.exit(1);
		}
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
				case"cswitch":{
					onCSwitchCommand(keys);
					break;
				}
				case"cinfo":{
					onCInfoCommand(keys);
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
					Logging.buildLogMessage(mLvl, new ActionMessage("available commands: /gip /cswitch /cinfo /restart /shutdown /clear"));
					}
			}
		} else {
			System.out.println(input);
		}
	}
	
	private void onRestartCommand(ArrayList<String> keys) {
	}

	private void onClearCommand(ArrayList<String> keys) {
		this.csl.clear();
	}

	private void onGipCommand() {
		try{
			String name = String.valueOf(InetAddress.getLocalHost().getHostAddress());
			Logging.buildLogMessage(mLvl, new MessageParameter("localhost",name));
		}catch(UnknownHostException e){
			Logging.buildLogMessage(mLvl, new ActionMessage("could not get localhost"),new MessageParameter(e.getMessage()));
			
		}		
	}
	
	private void onCSwitchCommand(ArrayList<String> params) {
		if(params.size()<1) {
			Logging.buildLogMessage(mLvl, new ActionMessage("use format /port <int old Port> <int new Port>"));
			return;
		}
		this.name = params.get(0);
	}
	
	private void onCInfoCommand(ArrayList<String> params) {
		if(echoSocket!=null) {
			Logging.buildDebugMessage(mLvl, new MessageParameter("ip",this.echoSocket.getLocalAddress().toString()),new MessageParameter("port",this.echoSocket.getPort()));
		} else {
			Logging.buildDebugMessage(mLvl, new ActionMessage("no Connection avalable"));
		}
	}
	
	private void onShutdownCommand(ArrayList<String> keys) {
		System.exit(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args){
		EchoClient3 cl= new EchoClient3();
	}
}