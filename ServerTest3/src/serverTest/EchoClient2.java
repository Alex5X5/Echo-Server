package serverTest;

import java.io.*;
import java.net.*;

import SimpleLogging.Logging.*;

public class EchoClient2 {
	public static final int port = 900;
	public static String name;
	private static final LoggingLevel mLvl = new LoggingLevel("Client");
	public static void main(String[] args){
		Console9 c = new Console9("Echo Client 2",true);
		try{Thread.sleep(500);} catch(InterruptedException e) {}
		int i = 0;
		while(i <10) {
			try{
				name = String.valueOf(InetAddress.getLocalHost().getHostAddress());
				name="127.0.0.1";
				Logging.buildLogMessage(new LoggingLevel("Client"), new ActionMessage("getting localhost"), new MessageParameter("name",name));
			}catch(UnknownHostException e){
				
			}
			
			int port_ = port + i;
			try {
				Logging.buildLogMessage(mLvl, new ActionMessage("trying to connect to server "), new MessageParameter("name",name), new MessageParameter("port",port_));
				Socket echoSocket = new Socket(name, port_);
				PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader in =	new BufferedReader(
						new InputStreamReader(echoSocket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				Logging.buildLogMessage(mLvl, new ActionMessage("connected "),new MessageParameter("port",port_),new MessageParameter("adress",name));
				String userInput;
				while ((userInput = stdIn.readLine()) != null) {
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
}