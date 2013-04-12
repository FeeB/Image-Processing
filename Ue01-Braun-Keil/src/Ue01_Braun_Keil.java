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
	private static final int maxNoise = 30; // in per cent

	private static JFrame frame;

	private ImageView srcView; // source image view
	private ImageView dstView; // filtered image view

	private int[] origPixels = null;

	private JLabel statusLine = new JLabel("   "); // to print some status text

	private JComboBox noiseType;
	private JLabel noiseLabel;
	private JSlider noiseSlider;
	private JLabel noiseAmountLabel;
	private boolean addNoise = false;
	private double noiseFraction = 0.01; // fraction for number of pixels to be
											// modified by noise

	private JComboBox filterType;

	public Ue01_Braun_Keil() {
		super(new BorderLayout(borderWidth, borderWidth));

		setBorder(BorderFactory.createEmptyBorder(borderWidth, borderWidth,
				borderWidth, borderWidth));

		// load the default image
		File input = new File("lena_klein.png");

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

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
		c.insets = new Insets(0, borderWidth, 0, 0);

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
		String[] noiseNames = { "No Noise", "Salt & Pepper" };

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
		noiseAmountLabel = new JLabel("" + Math.round(noiseFraction * 100.0)
				+ " %");
		noiseSlider = new JSlider(JSlider.HORIZONTAL, 0, maxNoise,
				(int) Math.round(noiseFraction * 100.0));
		noiseSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				noiseFraction = noiseSlider.getValue() / 100.0;
				noiseAmountLabel.setText("" + Math.round(noiseFraction * 100.0)
						+ " %");
				calculate(true);
			}
		});
		noiseLabel.setEnabled(addNoise);
		noiseSlider.setEnabled(addNoise);
		noiseAmountLabel.setEnabled(addNoise);

		// selector for filter
		String[] filterNames = { "No Filter", "Min Filter", "Max Filter",
				"Box Filter", "Median Filter" };
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
		JPanel images = new JPanel(new GridLayout(1, 2));
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
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}

	private void loadFile(File file) {
		if (file != null) {
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
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// display the window.
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2,
				(screenSize.height - frame.getHeight()) / 2);
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

	private void calculate(boolean createNoise) {
		long startTime = System.currentTimeMillis();

		if (createNoise) {
			// start with original image pixels
			srcView.setPixels(origPixels);
			// add noise
			if (addNoise)
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
		for (int i = 0; i < pixels.length; i++) {
			int r = argb_read(pixels[i], 16);
			int b = argb_read(pixels[i], 8);
			int g = argb_read(pixels[i], 0);

			int grey_value = (r + b + g) / 3;

			pixels[i] = 0xff000000 | (grey_value << 16) | (grey_value << 8)
					| grey_value;

		}
	}

	private void makeNoise(ImageView imgView) {
		int pixels[] = imgView.getPixels();
		for (int i = 0; i < getCountOfRandomNumber(
				Math.round(noiseFraction * 100.0), pixels.length); i++) {
			int pos = getRandomNumber(pixels.length);
			if (pos % 2 == 0) {
				pixels[pos] = 0xff000000 | (0 << 16) | (0 << 8);
			} else {
				pixels[pos] = 0xff000000 | (255 << 16) | (255 << 8) | 255;
			}
		}
	}

	private int getRandomNumber(int maxValue) {
		int number = 0 + (int) (Math.random() * ((maxValue - 0) + 1));
		return number;
	}

	private int getCountOfRandomNumber(long percent, int length) {
		int pixels = length;
		int number = (int) (pixels / 100 * percent);
		return number;
	}

	private void filter() {
		int src[] = srcView.getPixels();
		int dst[] = dstView.getPixels();
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		int filter = filterType.getSelectedIndex();

		if (filter == 1) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					//Array zum Speichern der Werte des Kernels
					long argb[] = new long[9];
					//Variable fuer die Platzierung der Werte im Array
					int n = 0;
					for (int row = -1; row < 2; row++) {
						for (int col = -1; col < 2; col++) {
							//Werte werden erstmal ohne Randbehandlung eingelesen
							if (x != 0 && y != 0 && x != width-1 && y != height-1) {

								argb[n] = src[(y + row) * width + (x + col)]; // Lesen der Originalwerte
								n++;
							}
						}
						//Array sortieren
						java.util.Arrays.sort(argb);
						//Zugriff auf kleinsten Wert
						dst[y * width + x] = (int) argb[0];
					}
					border_treatment(x, y, width, height, dst);
				}
				//Bild fuer rechte Spalte laden
				dstView.setPixels(dst);
			}
		}
		
		if (filter == 2){
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					long argb[] = new long[9];
					int n = 0;
					for (int row = -1; row < 2; row++) {
						for (int col = -1; col < 2; col++) {
							if (x != 0 && y != 0 && x != width-1 && y != height-1) {

								argb[n] = src[(y + row) * width + (x + col)]; // Lesen der Originalwerte
								n++;
							}
						}
						java.util.Arrays.sort(argb);
						//Zugriff auf höchsten Wert
						dst[y * width + x] = (int) argb[argb.length - 1];
					}
					border_treatment(x, y, width, height, dst);
				}
				dstView.setPixels(dst);
			}
	
		}
		
		
		if (filter == 3) {
			for (int y = 0; y < height-1; y++) {
				for (int x = 0; x < width-1; x++) {
					int argb= 0;
					int r = 0;
					int g = 0;
					int b = 0;
					
					// 3x3 Kernel
					for (int row = -1; row < 2; row++) {
						for (int col = -1; col < 2; col++) {
							// edge handling
							if (x != 0 && y != 0 && x != width && y != height) {
								
								argb = origPixels[(y + row) * width + (x + col)]; // Lesen der Originalwerte
																
								 r += (int) (1/9f * argb_read(argb,16));

								 g += (int) (1/9f * argb_read(argb,8));

								 b += (int) (1/9f * argb_read(argb,0));

							}
						}
						dst[y * width + x] = (0xFF << 24) | (r << 16) | (g << 8)| b; 
					}
					border_treatment(x, y, width, height, dst);
				}
				dstView.setPixels(dst);
			}
		}
	
		

		
		
		if (filter == 4) {
			// Loop over the pic
			for (int y = 0; y < height-1; y++) {
				for (int x = 0; x < width-1; x++) {
					long argb[] = new long[10];
					int n = 0;
					
					// 3x3 Kernel
					for (int row = -1; row < 2; row++) {
						for (int col = -1; col < 2; col++) {
							// edge handling
							if (x != 0 && y != 0 && x != width && y != height) {
								
								argb[n] = src[(y + row) * width + (x + col)]; // reading orginal values								
								n++;
							}
						}
						java.util.Arrays.sort(argb);
						// set the median value
						dst[y * width + x] = (int) argb[4];
					}
					border_treatment(x, y, width, height, dst);
				}
				dstView.setPixels(dst);
			}
		}
	}
		

								
	private void border_treatment(int x, int y, int width, int height, int[] dst){
		//funktioniert
		if (x == width-1){
			dst[y*width+x] = dst[y * width +x-2];
		}
		//funktioniert
		if(y == height -1){
			dst[y*width+x] = dst[(y-1) * width +x];
		}
		//funktioniert nicht
		if(x == 0){
			dst[y*width+x] = dst[y*width+(x+1)];
		}
		//funktioniert nicht
		if(y == 0){
			dst[y*width+x] = dst[(y+1)*width+(x)];
		}
	}
	
	private int argb_read(int pixel, int shift_value) {
		int x = (pixel >> shift_value) & 0xff;
		return x;
	}
}
