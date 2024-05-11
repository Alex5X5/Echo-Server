package serverTest;

//import SimpleLogging.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import SimpleLogging.Logging.*;

public class Console8 extends JFrame{
	
	File logFile = new File("Logs/"+String.valueOf(Date.from(Instant.now())));
	
	private static final long serialVersionUID = 4812262384995736639L;
	
	public ArrayList<ConsoleLine> lines = new ArrayList<ConsoleLine>();
	private CommandLine commandLine;

	private boolean quit;
	
	private PipedOutputStream commandOutputStream;
	private PipedInputStream commandInputStream;
	private PipedInputStream consoleInputI;
	private PipedInputStream consoleInputE;
	
	private Thread consoleThread1;
	private Thread consoleThread2;
	
	private static final LoggingLevel mLvl = new LoggingLevel("Console");

	private JPanel panel;
	
	private Object lineThreadOwner;
	private ConsoleLineHandler consoleLineHandler;
	
	private CommandHandler commandHandler;
	
	public Console8(String name) {
		super(name);
//		Logging.setStartTime();
		Logging.disableColors();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		panel = new JPanel();
		panel.setBackground(Color.black);
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(800,720));
		panel.setLayout(null);
		commandLine = new CommandLine();
		consoleLineHandler = new ConsoleLineHandler(this, commandLine);
		setupCommandField(this);
		setupConsoleLines();
		this.add(commandLine,BorderLayout.SOUTH);
		getContentPane().add(panel);
		pack();
		setVisible(true);
		redirectConsole();
		new Thread(
				()->{
					Scanner sc = new Scanner(System.in);
					while(sc.hasNext())
						System.out.println(sc.next());
						sc.close();
				}
		).start();
	}
	
	public Console8(String name, boolean redirectConsole) {
		super(name);
//		Logging.setStartTime();
		Logging.disableColors();
//		Logging.buildLogMessage(mLvl, new ActionMessage("constructor"));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		panel = new JPanel();
		panel.setBackground(Color.black);
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(800,720));
		panel.setLayout(null);
		commandLine = new CommandLine();
		consoleLineHandler = new ConsoleLineHandler(this, commandLine);
		setupCommandField(this);
		setupConsoleLines();
		this.add(commandLine,BorderLayout.SOUTH);
		getContentPane().add(panel);
		pack();
		setVisible(true);
		if(redirectConsole) {
			redirectConsole();
		}
		new Thread(
				()->{
					Scanner sc = new Scanner(System.in);
					while(sc.hasNext())
						System.out.println(sc.next());
						sc.close();
				}
		).start();
	}
	
	private void redirectConsole() {
		consoleInputI = new PipedInputStream();
		consoleInputE = new PipedInputStream();
		commandInputStream = new PipedInputStream();
		try {
			PipedOutputStream pout = new PipedOutputStream(this.consoleInputI);
			System.setOut(new PrintStream(pout,true));
//			Logging.buildLogMessage(mLvl, new ActionMessage("redirecting console"));
		} catch (IOException io) {
//			outputArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
		} catch (SecurityException se) {
//			outputArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
		} 
		try {
			PipedOutputStream pout2=new PipedOutputStream(this.consoleInputE);
			System.setErr(new PrintStream(pout2,true));
		} catch (java.io.IOException io) {
			this.consoleLineHandler.sendMessage("Couldn't redirect STDERR to this console\n"+io.getMessage());
		} catch (SecurityException se) {
			this.consoleLineHandler.sendMessage("Couldn't redirect STDERR to this console\n"+se.getMessage());
		}
		startRedirectionThreads();
	}

	private void startRedirectionThreads() {
//		Logging.buildLogMessage(mLvl, new ActionMessage("starting threads listening for main console"));
		consoleThread1 = new Thread(
			()->{
//				String fileseperator = System.getProperty("file.seperator");
				try {			
					while (true)
					{
						if (consoleInputI.available()!=0)
						{
							String input=this.readStdLine(consoleInputI);
							this.consoleLineHandler.sendMessage(input);
						}
						if (quit) {
							return;
						}
					}
				} catch (Exception e) {
					this.consoleLineHandler.sendMessage("\nConsole reports an Internal error.");
					this.consoleLineHandler.sendMessage("The error is: "+e);
				}
			}
		);
		consoleThread2 = new Thread(
			()->{
				try {
					while (true) {
						if (consoleInputE.available()!=0) {
							String input=this.readStdLine(consoleInputE);
							this.consoleLineHandler.sendMessage(input);
						}
						if (quit) return;
					}
				} catch (Exception e) {
					this.consoleLineHandler.sendMessage("\nConsole reports an Internal error.");
					this.consoleLineHandler.sendMessage("The error is: "+e);
				}
			}
		);
		consoleThread1.setDaemon(true);
		consoleThread2.setDaemon(true);
		consoleThread1.start();
		consoleThread2.start();		
	}
	
	private void setupCommandField(Console8 c) {
		c.commandOutputStream = new PipedOutputStream();
		c.commandInputStream = new PipedInputStream();
		try {
			c.commandOutputStream.connect(commandInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized(c.commandLine) {
			c.commandLine.addActionListener(
				(e)->{
					if(c.commandHandler==null)c.consoleLineHandler.sendMessage(c.commandLine.getText());
//					new Thread(
//							()->{if(c.commandHandler!=null)c.commandHandler.onCommandOutput(c.commandLine.getText(),c);}
//					).start();
					if(c.commandHandler!=null)c.commandHandler.onCommandOutput(c.commandLine.getText(),c);
					c.commandLine.setText("");
				}
			);
		}
	}
	
	private void setupConsoleLines() {
		for(int i=0; i<40; i++) {
			ConsoleLine l = new ConsoleLine(10, 700-i*18, this,i);
			l.setEditable(false);
			lines.add(l);
			panel.add(lines.get(i));
		}
	}
	
	private String readStdLine(PipedInputStream stream) throws IOException {
		String input="";
		do {
			if (stream.available()==0) break;
			byte b[]=new byte[stream.available()];
			stream.read(b);
			input=input+new String(b,0,b.length);
		} while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}
	
	public void clear() {
		consoleLineHandler.clear = true;
	}
	
	public void sendMessage(String s) {
//		System.out.println("recieved message:"+ s);
		this.consoleLineHandler.sendMessage(s);
	}
	
	public void addCommandHandler(CommandHandler ch) {
//		Logging.buildLogMessage(mLvl,new ActionMessage("adding custom Command Listener"));
		this.commandHandler = ch;
	}

	public static void main(String[] args) {
		Console8 console = new Console8("Console 8",false);
		try {Thread.sleep(500);} catch (InterruptedException e) {}
		console.addCommandHandler(
			(s,c)->{
			}
			
		);
		for(int i=0;i<30;i++) {
			console.sendMessage(String.valueOf(i));
		}
		
	}
	
	private class ConsoleLineHandler{

//		private boolean callUpdate = false;
		private boolean clear;
		private ArrayList<String> bufferedMessages = new ArrayList<String>();
		
		public ConsoleLineHandler(Console8 c, CommandLine l) {
			new Thread(
					()->{
						
						FileWriter fWriter = null;
						try {
							String dir = "Logs/";
							dir += String.valueOf((int)Math.floor(System.currentTimeMillis()/1E6));
							dir+=".txt";
							File fl = new File(dir);
//							System.out.println(fl.toString());
							fWriter = new FileWriter(fl);
						} catch(IOException io1) {
							io1.printStackTrace();
						}
						
//						Logging.buildDebugMessage(mLvl,new LoggingLevel(Thread.currentThread().getName()),new ActionMessage("constructing notifier"), new MessageParameter("owner",c.lineThreadOwner.toString()));
						c.lineThreadOwner = new Object();
//						Logging.buildDebugMessage(mLvl,new LoggingLevel(Thread.currentThread().getName()),new ActionMessage("lineThreadOwner is now"), new MessageParameter("owner",c.lineThreadOwner.toString()));
						while(true) {
							try{Thread.sleep(50);} catch(InterruptedException e) {}
							if(bufferedMessages.size()>0) {
//								Logging.buildDebugMessage(mLvl,new LoggingLevel(Thread.currentThread().getName()),new ActionMessage("suggesting"), new MessageParameter("text",bufferedMessages.get(0)));
//								System.out.println("checking lines");
								linesAvailable();
//								System.out.println("continuing");
								try {
									fWriter.write(this.bufferedMessages.get(0));
								} catch (IOException e) {}
								c.lines.get(0).suggest(this.bufferedMessages.get(0)+this.bufferedMessages.size());
								this.bufferedMessages.remove(0);
								synchronized(c.lineThreadOwner) {updateLines(c.lineThreadOwner);}
							}
							if(clear) {
								c.lines.forEach((ln)->{ln.suggest("");});
								synchronized(c.lineThreadOwner) {updateLines(c.lineThreadOwner);}
								clear=false;
							}
							if(c.quit) {
								if(fWriter!=null)
									try {
										fWriter.close();
									} catch (IOException e) {}
								break;
							}
						}
					},
					"Console Handler Thread"
			).start();
		}
		
		private void linesAvailable() {
			while(true) {
				boolean av = true;
//				try{Thread.sleep(500);} catch(InterruptedException e) {}
				for(int i=0; i<lines.size(); i++) {
//					System.out.println("i:"+String.valueOf(i)+" available: "+av);
					if(!lines.get(i).available) av = false;
				}
				if(av) break;
			}
		}
		
		private void sendMessage(String s) {
//			System.out.println("bufferingn message:"+ s);
			this.bufferedMessages.add(s);
		}
	
		private void updateLines(Object o){
//			Logging.buildDebugMessage(mLvl,new LoggingLevel(Thread.currentThread().getName()),new ActionMessage("updating lines "), new MessageParameter("owner",o.toString()));
			o.notify();
			o.notifyAll();
		}
	}
	
	private class CommandLine extends JTextField{
		
		private static final long serialVersionUID = -8293489793481667746L;

		public CommandLine() {
			super();
			this.setBackground(Color.black);
			this.setForeground(Color.white);
			this.setBorder(BorderFactory.createLineBorder(new Color(255,255,255),2));
		}
	}
	
	private class ConsoleLine extends JTextPane{
		
//		private static final LoggingLevel mLvl = new LoggingLevel("ConsoleLine");
		
		private static final long serialVersionUID = -4647715426485507274L;
		
//		private GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		private int pos;
		
		private boolean available = true;
		
		private String suggestedText;
		
		public ConsoleLine (int x, int y, Console8 c, int p) {
			super();
			this.pos = p; 
			this.setLayout(null);
			this.setBorder(BorderFactory.createEmptyBorder());
			this.setEditable(false);
			this.setBounds(x, y, 780, 18);
			this.setBackground(Color.black);
//			this.print(String.valueOf(y),null);
			new Thread(
					()->{
						run(c);
					},("Line Thread "+p)
			).start();
		}
		
		private void suggest(String s) {
			this.suggestedText = s;
		}
		
		private void print(String s, Color c) {
			if(c==null)c = new Color(255,255,255);
			StyledDocument doc = this.getStyledDocument();
			MutableAttributeSet mas = this.getInputAttributes();
//			StyleConstants.setAlignment(mas, 2);
			StyleConstants.setBold(mas, false);
			StyleConstants.setItalic(mas, false);
			StyleConstants.setFontFamily(mas,"Calibri");
			StyleConstants.setFontSize(mas, 14);
			StyleConstants.setSpaceAbove(mas,10);
			StyleConstants.setAlignment(mas, StyleConstants.ALIGN_LEFT);
//			StyleContext context = new StyleContext();
//			javax.swing.text.Style style = context.addStyle("test1", null);
			StyleConstants.setForeground(mas, c);
			try {
				doc.remove(0, doc.getLength());
				doc.insertString(0, s, mas);
			} catch (Exception bla_bla_bla_bla) {
				bla_bla_bla_bla.printStackTrace();
			}
		}
		
		private void run(Console8 c) {
//			final LoggingLevel sLvl = new LoggingLevel(Thread.currentThread().getName());
//			Logging.buildDebugMessage(mLvl,sLvl,new ActionMessage("run"));
			while(true) {
				synchronized (c.lineThreadOwner) {
					try{
//						Logging.buildDebugMessage(mLvl,sLvl,new ActionMessage("trying to wait"), new MessageParameter("owner",c.lineThreadOwner.toString()));
						c.lineThreadOwner.wait();
					}catch(InterruptedException e){
						e.printStackTrace();
					}
//					Logging.buildDebugMessage(mLvl,sLvl,new ActionMessage("got waken up"));
				}
				available = false;
				String newT = "";
				if(suggestedText!="") {
//					Logging.buildDebugMessage(mLvl,sLvl,new ActionMessage("printing suggested text"), new MessageParameter("text",this.suggestedText));
					try {Thread.sleep(10);} catch (InterruptedException e) {}
					this.print(suggestedText,null);
					suggestedText = "";
				} else {
//					Logging.buildDebugMessage(mLvl,sLvl,new ActionMessage("printing text of line above"), new MessageParameter("text",this.suggestedText));
					if(pos!=0)newT = lines.get(pos-1).getText();
//					if(pos==0)newT = commandScanner.next();
					try {Thread.sleep(20);} catch (InterruptedException e) {}
					if(pos!=0)this.print(newT,null);
				}
				available = true;
			}
		}
	}
	
	@FunctionalInterface
	public interface CommandHandler{
		public void onCommandOutput(String command, Console8 console);
	}
	
	public class CHandler implements CommandHandler{		
//		public CHandler() {
//			
//		}
		
		public CHandler() {
			
		}

		@Override
		public void onCommandOutput(String command, Console8 c) {
		}
	}
	
	class CHandler2 implements CommandHandler{
		@Override
		public void onCommandOutput(String command, Console8 c) {
			if(command.startsWith("/")) {
				System.out.println("handling message");
				command.replaceFirst("/", "");
				c.consoleLineHandler.sendMessage(command+"test");
			} else return;
		}
	}
}