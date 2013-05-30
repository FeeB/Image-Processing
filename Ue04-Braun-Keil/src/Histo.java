// Ue04 Bildverarbeitung SS2013
// Prof. K. Jung

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class Histo extends JPanel {
	
	// some constants
	//
	private static final long serialVersionUID = 1L;
	private static final int histWidth = 256;
	private static final int histHeight = 256;
	private static final int layoutBorder = 10;
	private static final int maxImageWidth = 605;
	private static final int maxImageHeight = 600;
	
	// main frame
	//
	private static JFrame frame;
	
	// layout items
	//
	private ImageView imgView;					// image view
	private ImageView histoView;				// histogram view
	private int [] copyView;					// Screen View
	private JLabel[]  label = new JLabel[8];	// text display

	// internal status
	//
	private int  		radius 			= 0;	// current drawing radius
	private int  		updateCount 	= 0;	// number of clicks
	private int[] frequency = new int[256];
	
	public Histo() {
        super(new BorderLayout(layoutBorder, layoutBorder));
        
        // load the default image
        File input = new File("mountains.png");
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        imgView = new ImageView(input);
        imgView.setMaxSize(new Dimension(maxImageWidth, maxImageHeight));
        makeGray(imgView);
        copyView = imgView.getPixels().clone();
        readFrequencies();
       
		// create an empty histogram image
		histoView = new ImageView(histWidth, histHeight);
		
		// load image button
        JButton load = new JButton("Open Image ");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File input = openFile();
        		if(input != null) {
	        		imgView.loadImage(input);
	        		imgView.setMaxSize(new Dimension(maxImageWidth, maxImageHeight));
	        		makeGray(imgView);
	        		frame.pack();
	                resetImage();
        		}
        	}        	
        });
        
        // text display
        JPanel controlPanel = new JPanel(new GridLayout(5, 2));
        
    	String[] string = {"Text 1", "Text 2", "Text 3", "Text 4", 
                           "Text 5", "Text 6", "Text 7", "Text 8"};

		for (int i = 0; i < 8; i++) {
			label[i] = new JLabel(string[i]);
			controlPanel.add(label[i]);
		}
		
		// buttons
		JButton darkerButton = new JButton("decrease brightness");
		darkerButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		changeBrightness(-10);
        	}        	
        });
		controlPanel.add(darkerButton);

		JButton enlightButton = new JButton("increase brightness");
		enlightButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		changeBrightness(10);
        	}        	
        });
		controlPanel.add(enlightButton);
		
        JPanel images = new JPanel(new FlowLayout());
        images.add(imgView);
        images.add(histoView);
        
        add(load, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
               
        setBorder(BorderFactory.createEmptyBorder(layoutBorder,layoutBorder,layoutBorder,layoutBorder));
        
        // perform the initial scaling
        resetImage();
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Statistical Image Analysis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new Histo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	private void resetImage() {
		// a new image has been laoded
		updateHistogram();
	}
	
	private ImageView makeGray(ImageView imgView) {
		int pixels[] = imgView.getPixels();

		// loop over all pixels
		for (int i = 0; i < pixels.length; i++) {
			int r = argb_read(pixels[i], 16);
			int b = argb_read(pixels[i], 8);
			int g = argb_read(pixels[i], 0);

			int grey_value = (r + b + g) / 3;

			pixels[i] = 0xff000000 | (grey_value << 16) | (grey_value << 8)
					| grey_value;

		}
		imgView.applyChanges();
		return imgView;
	}
	
	private int argb_read(int pixel, int shift_value) {
		int x = (pixel >> shift_value) & 0xff;
		return x;
	}
	
	private void changeBrightness(int delta) {
		
		radius += delta > 0 ? 1 : -1;	// some dummy operation
		
		int pixels[] = imgView.getPixels();
		
		for (int pos = 0; pos < pixels.length; pos++) {
			
			// get pixel
			int c 	= pixels[pos];
			
			// get RGB values
			int r 	= (c & 0xff0000) >> 16;
			int g 	= (c & 0x00ff00) >> 8;
			int b 	= (c & 0x0000ff);
			
			// increase RGB
			r 	+= delta;
			g 	+= delta;
			b 	+= delta;
			
			// restore pixel
			pixels[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);			
		}
		
		imgView.applyChanges();
		
		updateHistogram();
	}
    
	private void updateHistogram() {
		// ToDo: draw the histogram
		
		// some dummy operation
		updateCount++;
		drawCircle();
		
		updateText();
	}
	
	private void updateText() {
		String s = "Some dummy text.";
		
		label[2].setText(s);
		
		double number = Math.PI;
		s = "PI = " + format(number, 5);
		label[4].setText(s);
		
		s = "Number of updates is " + updateCount + ".";
		label[0].setText(s);
	}

	public String format(double x, int len) {
		double d = 1;
		
		for (int i = 0; i < len; i++) d *= 10;
		
		x = Math.round(x * d) / d;
		
		return Double.toString(x);
	}

	void drawCircle() {
		int pixels[] = histoView.getPixels();
		
		int width = histoView.getImgWidth();
		int height = histoView.getImgHeight();
		
		int xCenter = width/2;
		int yCenter = height/2;
		
		int squareRadius = radius*radius;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int squareR = ((yCenter-y)*(yCenter-y)+(xCenter-x)*(xCenter-x));
				
				int pos = y*width+x;
				
				if (squareR < squareRadius )
					pixels[pos] = 0xff008100;	// dark green
				else
					pixels[pos] = 0xff000000;	// black
			}
		}
		
		histoView.applyChanges();
	}
	// liest die hŠufigeiten der werte zw.0 -255 aus copyview und erstellt frequency table
	private void readFrequencies(){
		for (int i=0;i<copyView.length;i++){

			frequency[argb_read(copyView[i],0)]+=1;
			
		}
		System.out.println(frequency[255]);
	}

}
    
