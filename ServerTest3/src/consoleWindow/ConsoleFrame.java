package consoleWindow;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import SimpleLogging.Logging.*;

@Deprecated
public class ConsoleFrame extends JFrame{
	
	private static final long serialVersionUID = -3131261962497103196L;
	private LoggingLevel mLvl = new LoggingLevel("Console Frame");
	public ConsoleTextArea area;
	
	public ConsoleFrame() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		this.setSize(new Dimension(500,500));
		this.setVisible( true );
		area = new ConsoleTextArea();
		this.add(area);
		Logging.buildLogMessage(mLvl, new ActionMessage("constructor"));
		area.append("ae");
	}
	
	public void log(String s) {
		area.log("awdhg");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		area.log("uiuuz");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws Exception {

	    // create a new frame
	    JFrame frame = new JFrame("Test");
	    frame.setLayout(new GridLayout(20,2));

	    // create some fields that you can update from the console
	    JTextField[] fields = new JTextField[20];
	    for (int i = 0; i < fields.length; i++) {
	        frame.add(fields[i] = new JTextField("" + i)); // add them to the frame
	        fields[i].setSize(50,10);
	        fields[i].setEditable(false);
	        fields[i].setBorder(BorderFactory.createEmptyBorder()); // add them to the frame    
	    }
	    // show the frame (it will pop up and you can interact with in - spawns a thread)
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);

	    // Create a scanner so that you can get some input from the console (the easy way)
	    Scanner s = new Scanner(System.in);

	    for (int i = 0; i < fields.length; i++) {

	        // get the field you want to update
	        final JTextField field = fields[i];

	        // get the input from the console
//	        final String line = s.nextLine();

	        // update the field (must be done thread-safe, therefore this construct)
	        SwingUtilities.invokeAndWait(new Runnable() {
	            @Override public void run() { construct(); field.setText(lastLine); }
	        });
	    }
	    s.close();
	}
	
	private static String lastLine;

	public static void construct() {
	    System.setOut(
	    	new PrintStream(System.out) {
	    		public void println(String s) {
	    			lastLine = s;
	    			super.println(s);
	    		}
	    	}
	    );
	}
	
	
	@SuppressWarnings("serial")
	private class ConsoleTextArea extends JTextArea{
		
		public ConsoleTextArea() {
			Color color=new Color(255,255,255);
			this.setForeground(color);
			this.setBackground(Color.BLACK);
			this.setFocusable(true);
			this.setVisible(true);
		}
//		private ArrayList<String> lines;
		
		public void log(String s) {
			
		}
		
	@SuppressWarnings("unused")
	private static void doaction () {
			java.awt.EventQueue.invokeLater(
				new Runnable() {
					public void run() {
						String line = null;
						Properties properties = new Properties();
						try {
							properties.load(new FileInputStream("reacp/config.properties"));
						} catch (IOException e) {
						}
						 
						try	{
							JFrame miFrame = new JFrame("Log");
							JFrame.setDefaultLookAndFeelDecorated(false);
							miFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							//miFrame.setLayout( new FlowLayout() );	
							miFrame.setSize( 250,150 );
							miFrame.setVisible( true );
							BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					
							JTextArea log = new JTextArea(20,20);
							miFrame.add(log);
							Color color=new Color(255,255,255);
							log.setForeground(color);
							log.setBackground(Color.BLACK);
							while (true) {
								line = br.readLine();
								if (line == null)break;
								System.out.println(line);
								File TextFile = new File("ola.txt");
								FileWriter TextOut = new FileWriter(TextFile, true);
								TextOut.write(line+ "\n");
								TextOut.close();
								miFrame.setVisible(true);
					
								log.append("test");
								log.append(System.getProperty("line.separator"));
												  //log.setText(log.getText()+line+"\n");
							 }
						} catch (IOException e) {
							e.printStackTrace(System.err);
							System.exit(2);
						}
					}
				}
			);
		}
	}
}
