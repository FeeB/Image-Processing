// BV Ue01 SS2013
//

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

public class Ue01_Braun_Keil extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int borderWidth = 5;
	private static final int maxWidth = 400;
	private static final int maxHeight = maxWidth;
	private static final int maxNoise = 30;	// in per cent
	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView dstView;			// filtered image view

	private int[] origPixels = null;
	
	private JLabel statusLine = new JLabel("   "); // to print some status text
	
	private JComboBox noiseType;
	private JLabel noiseLabel;
	private JSlider noiseSlider;
	private JLabel noiseAmountLabel;
	private boolean addNoise = false;
	private double noiseFraction = 0.01;	// fraction for number of pixels to be modified by noise
	
	private JComboBox filterType;
	

	public Ue01_Braun_Keil() {
        super(new BorderLayout(borderWidth, borderWidth));

        setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
 
        // load the default image
        File input = new File("lena_klein.png");
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
        
		// convert to grayscale
		makeGray(srcView);
		        
        // keep a copy of the grayscaled original image pixels
        origPixels = srcView.getPixels().clone();
       
		// create empty destination image of same size
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
        // control panel
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,borderWidth,0,0);

		// load image button
        JButton load = new JButton("Open Image");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		loadFile(openFile());
        		// convert to grayscale
        		makeGray(srcView);  
                // keep a copy of the grayscaled original image pixels
                origPixels = srcView.getPixels().clone();
        		calculate(true);
        	}        	
        });
         
        // selector for the noise method
        String[] noiseNames = {"No Noise", "Salt & Pepper"};
        
        noiseType = new JComboBox(noiseNames);
        noiseType.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		addNoise = noiseType.getSelectedIndex() > 0;
        		noiseLabel.setEnabled(addNoise);
                noiseSlider.setEnabled(addNoise);
                noiseAmountLabel.setEnabled(addNoise);
        		calculate(true);
        	}
        });
        
        // amount of noise
        noiseLabel = new JLabel("Noise:");
        noiseAmountLabel = new JLabel("" + Math.round(noiseFraction * 100.0)  + " %");
        noiseSlider = new JSlider(JSlider.HORIZONTAL, 0, maxNoise, (int) Math.round(noiseFraction * 100.0));
        noiseSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		noiseFraction = noiseSlider.getValue() / 100.0;
        		noiseAmountLabel.setText("" + Math.round(noiseFraction * 100.0) + " %");
        		calculate(true);
        	}
        });
        noiseLabel.setEnabled(addNoise);
        noiseSlider.setEnabled(addNoise);
        noiseAmountLabel.setEnabled(addNoise);

        // selector for filter
        String[] filterNames = {"No Filter", "Min Filter", "Max Filter", "Box Filter", "Median Filter"};
        filterType = new JComboBox(filterNames);
        filterType.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		calculate(false);
        	}
        });
        
        controls.add(load, c);
        controls.add(noiseType, c);
        controls.add(noiseLabel, c);
        controls.add(noiseSlider, c);
        controls.add(noiseAmountLabel, c);
        controls.add(filterType, c);
        
        // images panel
        JPanel images = new JPanel(new GridLayout(1,2));
        images.add(srcView);
        images.add(dstView);
        
        // status panel
        JPanel status = new JPanel(new GridBagLayout());
        
        status.add(statusLine, c);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        
        calculate(true);
                       
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	
	private void loadFile(File file) {
		if(file != null) {
    		srcView.loadImage(file);
    		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
    		// create empty destination image of same size
    		dstView.resetToSize(srcView.getImgWidth(), srcView.getImgHeight());
    		frame.pack();
		}
		
	}
	
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Ue01_Nachname_Vorname");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new Ue01_Braun_Keil();
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
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	private void calculate(boolean createNoise) {
		long startTime = System.currentTimeMillis();
		
		if(createNoise) {
			// start with original image pixels
			srcView.setPixels(origPixels);
			// add noise
			if(addNoise) 
				makeNoise(srcView);
			// make changes visible
			srcView.applyChanges();
		}
		
		// apply filter
		filter();

		// make changes visible
		dstView.applyChanges();
		
		long time = System.currentTimeMillis() - startTime;
    	statusLine.setText("Processing Time = " + time + " ms");
	}

	
	private void makeGray(ImageView imgView) {
		int pixels[] = imgView.getPixels();
		// TODO: convert pixels to grayscale
		
		// loop over all pixels
		for(int i = 0; i < pixels.length; i++) {
			int r = argb_read(pixels[i],16);  
			int b = argb_read(pixels[i],8);
			int g =argb_read(pixels[i],0);
			
			int grey_value = (r+b+g)/3;
			
			pixels[i] = 0xff000000 | (grey_value<<16) | (grey_value<<8) | grey_value;
			
		}
		//imgView.setPixels(pixels);
	}
	
	private void makeNoise(ImageView imgView) {
		int pixels[] = imgView.getPixels();
		
	}
	
	
	private void filter() {
		int src[] = srcView.getPixels();
		int dst[] = dstView.getPixels();
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		int filter = filterType.getSelectedIndex();
		
		// TODO: implement filters 
		
	}
	
	private int argb_read(int pixel, int shift_value){
		int x = (pixel >> shift_value) & 0xff;
		return x; 
	}
}

