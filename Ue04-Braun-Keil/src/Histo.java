// Ue04 Bildverarbeitung SS2013
// Prof. K. Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.tools.javac.code.Attribute.Array;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

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
	private int [] copyView;
	private JLabel[]  label = new JLabel[6];	// text display

	// internal status
	//
	
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
        
        String[] string = {"Min: ", "Max: ", "Mittelwert: ", "Varianz: ", 
                "Median: ", "Entropie: "};

		for (int i = 0; i < 6; i++) {
			label[i] = new JLabel(string[i]);
			controlPanel.add(label[i]);
		}
        
//      Slider HelligkeitsŠnderung
      final JSlider brightnessSlider = new JSlider(-255, 255, 0);
      brightnessSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				changeBrightness(brightnessSlider.getValue());
			}
		});
      controlPanel.add(brightnessSlider);
      
//    Slider KontrastŠnderung
    final JSlider contrastSlider = new JSlider(0, 100, 0);
    contrastSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				changeContrast(contrastSlider.getValue());
			}
		});
    controlPanel.add(contrastSlider);
		
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
		
		int pixels[] = copyView;
		
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
			imgView.pixels[pos] = 0xFF000000 + ((limitPixel(r) & 0xff) << 16) + ((limitPixel(g) & 0xff) << 8) + (limitPixel(b) & 0xff);			
		}
		
		imgView.applyChanges();
		
		drawHist();
	}
	
	public void changeContrast(int contrast){
		double schwellenwert = 255 / 2;
		
		int pixels[] = copyView;
		
		for (int pos = 0; pos < pixels.length; pos++) {
			int c 	= pixels[pos];
			
			// get RGB values
			int r 	= (c & 0xff0000) >> 16;
			int g 	= (c & 0x00ff00) >> 8;
			int b 	= (c & 0x0000ff);
			
			if (r < schwellenwert) {
				r = (int) (r - contrast);
			}else if (r > schwellenwert) {
				 r = (int) (r + contrast);
			}
			
			if (g < schwellenwert) {
				g = (int) (g - contrast);
			}else if (g > schwellenwert) {
				 g = (int) (g + contrast);
			}
			
			if (b < schwellenwert) {
				b = (int) (b - contrast);
			}else if (g > schwellenwert) {
				b = (int) (b + contrast);
			}
			imgView.pixels[pos] = 0xFF000000 + ((limitPixel(r) & 0xff) << 16) + ((limitPixel(g) & 0xff) << 8) + (limitPixel(b) & 0xff);
		}
		imgView.applyChanges();
		
		drawHist();
	}
    
	private int limitPixel(int pixel){
		if (pixel > 255){
			return pixel = 255;
		}else if (pixel < 0){
			return pixel = 0;
		}else{
			return pixel;
		}
	}
	
	private void updateHistogram() {
		drawHist();		
		updateText();
	}
	
	private void updateText() {
		int[] histoArray = histoView.getPixels();
		Arrays.sort(histoArray);
		String maximum = "Max: " + argb_read(histoArray[histoArray.length-1], 16);
		System.out.println(histoArray[histoArray.length-1]);
		System.out.println(argb_read(histoArray[histoArray.length-1],16));
		String minimum = "Min: " + argb_read(histoArray[0], 16);
		String average = "Average: " + average(histoArray);
		String median = "Median: " + argb_read(histoArray[histoArray.length/2], 16);
		
		label[0].setText(maximum);
		label[1].setText(minimum);
		label[2].setText(average);
		label[3].setText(maximum);
		label[4].setText(median);
		label[5].setText(maximum);
	}
	
	public int average(int[] histoArray){
		int average = 0;
		for (int i = 0; i < histoArray.length; i++){
			average += argb_read(histoArray[i],0);
		}
		return average/histoArray.length;
	}

	public String format(double x, int len) {
		double d = 1;
		
		for (int i = 0; i < len; i++) d *= 10;
		
		x = Math.round(x * d) / d;
		
		return Double.toString(x);
	}
	
	// liest die hŠufigeiten der werte zw.0 -255 aus copyview und erstellt frequency table
	
	private void readFrequencies(){
		for(int i = 0; i < frequency.length; i++){
			frequency[i] = 0;
		}
		int[] pixelsOfCurrentImage = imgView.getPixels();
		
		for (int i=0;i<pixelsOfCurrentImage.length;i++){

			frequency[argb_read(pixelsOfCurrentImage[i],0)]+=1;
			
		}
		
	}
	void drawHist() {
		readFrequencies();
		int pixels[] = histoView.getPixels();	
		int width = histoView.getImgWidth();
		int height = histoView.getImgHeight();
		
		double factor=heightProp(histoView.getImgHeight());
		
		for (int x=0; x< width;x++){
			
			double frequencyWithFactor =(frequency[x]*factor);
			
			//int yfacotor=(int()(height-height*factor);
			for (int y=height-1;y>=0;y--){
				int pos = ((height-1)-y)*width+x;
				if(y>=frequencyWithFactor){
				pixels[pos] = 0xff000000;	// black
				}else{
					pixels[pos] = 0xff008100;//green
				}
				}	
			}
			//	System.out.println(y);
		
		histoView.applyChanges();
		updateText();
	}
	private double heightProp(int height){
		int[] frequencyArray = frequency.clone();
		Arrays.sort(frequencyArray);
		double x= frequencyArray[frequencyArray.length-1];
		double heightProp= height/x;
		System.out.println(x);
		System.out.println(heightProp);
		return heightProp;
		
	}
}
    
