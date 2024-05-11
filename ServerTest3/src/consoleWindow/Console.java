package consoleWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import SimpleLogging.Logging.*;

public class Console extends JFrame implements Runnable{
	
	private static final long serialVersionUID = 4812262384995736639L;

	private boolean quit;
	
	private JTextArea outputArea;
	private JTextField inputField;
	
	private Scanner fieldInput;
	private PrintStream fieldOutput;
	
	PipedOutputStream outputFromField;
	PipedInputStream inputFromField;
	PipedInputStream consoleInputI;
	PipedInputStream consoleInputE;
	
	private Thread consoleThread1;
	private Thread consoleThread2;
	
	private LoggingLevel mLvl = new LoggingLevel("Console");

	public Console() {
		//create components
		super("Java Console");
		outputArea = new JTextArea();
		inputField = new JTextField();

		//Make outputArea read-only
		outputArea.setEditable(false);

		outputArea.setBackground(Color.white);
		inputField.setBackground(Color.white);
		outputArea.setForeground(Color.black);
		inputField.setForeground(Color.black);

		//Add components
		this.setLayout(new BorderLayout());
		this.add(outputArea,BorderLayout.CENTER);
		this.add(inputField,BorderLayout.SOUTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1000,600);
		this.setVisible(true);

		//Setup Piped IO
		outputFromField = new PipedOutputStream();
		consoleInputI = new PipedInputStream();
		consoleInputE = new PipedInputStream();
		inputFromField = new PipedInputStream();
		
		redirectConsole();
		startConsoleThreads();
		

		Logging.disableColors();
		Logging.setStartTime();
		Logging.buildLogMessage(mLvl, new ActionMessage("Constructor"));
		
		try {
			outputFromField.connect(inputFromField);
		} catch (IOException e) {
			e.printStackTrace();
  //		  System.exit(1);
		}
		
		fieldInput = new Scanner(inputFromField);
		fieldOutput = new PrintStream(outputFromField);
		
		//Setup listeners

		//This listener listens for ENTER key on the inputField.
		inputField.addActionListener(
			(e) ->{
				String text = inputField.getText();
				fieldOutput.println(text);
				inputField.setText("");
				//Wake up the other thread for an immediate response.
				synchronized (inputFromField) {
					inputFromField.notify();
				}			
			}
		);

		//Setup Frame for display
		//Start other thread that will run Console.run()
		Thread mainProgram = new Thread(this);
		mainProgram.start();
	}

	public Console(String s) {
		//create components
		super(s);
		outputArea = new JTextArea();
		inputField = new JTextField();

		//Make outputArea read-only
		outputArea.setEditable(false);

		outputArea.setBackground(Color.white);
		inputField.setBackground(Color.white);
		outputArea.setForeground(Color.black);
		inputField.setForeground(Color.black);

		//Add components
		this.setLayout(new BorderLayout());
		this.add(outputArea,BorderLayout.CENTER);
		this.add(inputField,BorderLayout.SOUTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1000,600);
		this.setVisible(true);

		//Setup Piped IO
		outputFromField = new PipedOutputStream();
		consoleInputI = new PipedInputStream();
		consoleInputE = new PipedInputStream();
		inputFromField = new PipedInputStream();
		
		redirectConsole();
		startConsoleThreads();
		

		Logging.disableColors();
		Logging.setStartTime();
		Logging.buildLogMessage(mLvl, new ActionMessage("Constructor"));
		
		try {
			outputFromField.connect(inputFromField);
		} catch (IOException e) {
			e.printStackTrace();
  //		  System.exit(1);
		}
		
		fieldInput = new Scanner(inputFromField);
		fieldOutput = new PrintStream(outputFromField);
		
		//Setup listeners

		//This listener listens for ENTER key on the inputField.
		inputField.addActionListener(
			(e) ->{
				String text = inputField.getText();
				fieldOutput.println(text);
				inputField.setText("");
				//Wake up the other thread for an immediate response.
				synchronized (inputFromField) {
					inputFromField.notify();
				}			
			}
		);

		//Setup Frame for display
		//Start other thread that will run Console.run()
		Thread mainProgram = new Thread(this);
		mainProgram.start();
	}
	
	private void redirectConsole() {
		try {
			PipedOutputStream pout = new PipedOutputStream(this.consoleInputI);
			System.setOut(new PrintStream(pout,true));
			System.out.println("Redirecting Console");
		} catch (IOException io) {
//			outputArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
		} catch (SecurityException se) {
//			outputArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
		} 
		try {
			PipedOutputStream pout2=new PipedOutputStream(this.consoleInputE);
			System.setErr(new PrintStream(pout2,true));
		} catch (java.io.IOException io) {
			outputArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
		} catch (SecurityException se) {
			outputArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
		}
	}

	private void startConsoleThreads() {
		consoleThread1 = new Thread(
			()->{
				try {			
					while (true)
					{
						if (consoleInputI.available()!=0)
						{
							String input=this.readConsoleLine(consoleInputI);
							outputArea.append(input);
						}
						if (quit) return;
					}
				} catch (Exception e) {
					outputArea.append("\nConsole reports an Internal error.");
					outputArea.append("The error is: "+e);			
				}
			}
		);		
		consoleThread2 = new Thread(
			()->{
				try {
					while (true) {
						if (consoleInputE.available()!=0) {
							String input=this.readConsoleLine(consoleInputE);
							outputArea.append(input);
						}
						if (quit) return;
					}			
				} catch (Exception e) {
					outputArea.append("\nConsole reports an Internal error.");
					outputArea.append("The error is: "+e);			
				}
			}
		);
		consoleThread1.setDaemon(true);
		consoleThread2.setDaemon(true);
		consoleThread1.start();
		consoleThread2.start();		
	}
	
	private String readConsoleLine(PipedInputStream stream) throws IOException {
		String input="";
		do {
			int available=stream.available();
			if (available==0) break;
			byte b[]=new byte[available];
			stream.read(b);
			input=input+new String(b,0,b.length);														
		} while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}
	
	public void run() {
		while (fieldInput.hasNextLine()) {
			String line = fieldInput.nextLine();
			outputArea.append(line+"\n");
			System.out.println("Program recieved input: "+line);
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Console console = new Console();
	}
}