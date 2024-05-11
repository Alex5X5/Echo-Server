package serverTest;

import java.io.*;
import java.net.*;

import SimpleLogging.Logging.ActionMessage;
import SimpleLogging.Logging.Logging;
import SimpleLogging.Logging.LoggingLevel;
import SimpleLogging.Logging.MessageParameter;

public class EchoClient {
	public static final int port = 902;
	public static String name;
	private static final LoggingLevel mLvl = new LoggingLevel("Client"); 
	public static void main(String[] args) throws IOException {
		try{
			name = String.valueOf(InetAddress.getLocalHost().getHostAddress());
			Logging.buildLogMessage(new LoggingLevel("Client"), new ActionMessage("getting localhost"), new MessageParameter("name",name));
		}catch(UnknownHostException e){
			
		}
		
		try {
			Logging.buildLogMessage(mLvl, new ActionMessage("trying to connect to server "), new MessageParameter("name",name), new MessageParameter("port",port));
			Socket echoSocket = new Socket(name, port);
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in =	new BufferedReader(
					new InputStreamReader(echoSocket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			Logging.buildLogMessage(mLvl, new ActionMessage("connected "),new MessageParameter("port",port),new MessageParameter("adress",name));
			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				out.println(userInput);
				System.out.println("echo: " + in.readLine());
			}
			echoSocket.close();
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + name);
			System.exit(1);
		} catch (IOException e) {
			Logging.buildLogMessage(mLvl, new ActionMessage("Couldn't get I/O for the connection"), new MessageParameter("name",name));
			Logging.buildLogMessage(mLvl, new ActionMessage("reason"), new MessageParameter(e.getMessage()));
			Logging.buildLogMessage(mLvl, new ActionMessage("exiting"));
			System.exit(1);
		} 
	}
}