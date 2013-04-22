import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

//Verschaffen Sie sich einen groben �berblick dar�ber, wie diese Applikation programmiert ist. Lokalisieren Sie die Stelle, an der die Rotation aufgerufen wird. Sie sollen die fehlenden Teile implementieren:
//	 
//private void initDstView() 
//Hier m�ssen Sie die richtige Gr��e der Zielanzeige berechnen. Die Anzeigefl�che soll so gro� (und nicht gr��er) sein, dass das rotierte Bild bei allen Winkeln vollst�ndig zu sehen ist.
// 
//void rotateNearestNeigbour()
//Das Ausgangsbild soll um den eingestellten Winkel gedreht werden. Als Interpolationsmethode soll die einfache Pixelwiederholung verwendet werden. Die Hintergrundfarbe soll wei� sein.
// 
//void rotateBilinear()
//Wie 2, allerdings ist hierbei die bilinare Interpolationstechnik zu verwenden. Siehe Folie 19 aus GeometrischeBildmanipulation.pdf oder �bung 6 aus dem letzten Semester.
// 

public class Rotate extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "<Your Name>"; // type in your name
														// here
	private static final String initialFilename = "59009_512.jpg";
	private static final int border = 10;
	private static final int maxWidth = 900;
	private static final int maxHeight = 900;
	private static final double angleStepSize = 5.0; // size used for angle
														// increment and
														// decrement

	private static JFrame frame;

	private ImageView srcView = null; // source image view
	private ImageView dstView = null; // rotated image view

	private JComboBox methodList; // the selected rotation method
	private JSlider angleSlider; // the selected rotation angle
	private JLabel statusLine; // to print some status text
	private double angle = 0.0; // current rotation angle in degrees
	


	/**
	 * Constructor. Constructs the layout of the GUI components and loads the
	 * initial image.
	 */
	public Rotate() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
		initDstView();

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					initDstView();
					rotateImage(false);
				}
			}
		});

		// selector for the rotation method
		String[] methodNames = { "Nearest Neighbour", "Bilinear Interpolation" };

		methodList = new JComboBox(methodNames);
		methodList.setSelectedIndex(0); // set initial method
		methodList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rotateImage(false);
			}
		});

		// rotation angle minus button
		JButton decAngleButton = new JButton("-");
		decAngleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				angle -= angleStepSize;
				if (angle < -180)
					angle += 360;
				angleSlider.setValue((int) angle);
				rotateImage(false);
			}
		});

		// rotation angle plus button
		JButton incAngleButton = new JButton("+");
		incAngleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				angle += angleStepSize;
				if (angle > 180)
					angle -= 360;
				angleSlider.setValue((int) angle);
				rotateImage(false);
			}
		});

		// rotation angle slider
		angleSlider = new JSlider(-180, 180, (int) angle);
		angleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				angle = angleSlider.getValue();
				rotateImage(false);
			}
		});

		// speed test button
		JButton speedTestButton = new JButton("Speed Test");
		speedTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long startTime = System.currentTimeMillis();
				double lastAngle = angle;
				int cnt = 0;
				for (angle = 0; angle < 360; angle += angleStepSize) {
					rotateImage(true);
					cnt++;
				}
				long time = System.currentTimeMillis() - startTime;
				statusLine.setText("Speed test: " + cnt + " rotations in "
						+ time + " ms");
				angle = lastAngle;
			}
		});

		// some status text
		statusLine = new JLabel(" ");

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(methodList, c);
		controls.add(decAngleButton, c);
		controls.add(angleSlider, c);
		controls.add(incAngleButton, c);
		controls.add(speedTestButton, c);

		// arrange images
		JPanel images = new JPanel();
		images.add(srcView);
		images.add(dstView);

		// add to main panel
		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		// add border to main panel
		setBorder(BorderFactory.createEmptyBorder(border, border, border,
				border));

		// perform the initial rotation
		rotateImage(false);
	}

	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Rotation - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent contentPane = new Rotate();
		contentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(contentPane);

		// display the window
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2,
				(screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
		// schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/**
	 * Open file dialog used to select a new image.
	 * 
	 * @return The selected file object or null on cancel.
	 */
	private File openFile() {
		// file open dialog
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		return null;
	}

	/**
	 * Initialize the destination view giving it the correct size.
	 */
	private void initDstView() {
		// calculate destination size large enough to embed rotated image at any
		// angle

		/****
		 * TODO: calculate correct size of destination view based on size of
		 * source view
		 ****/
		int width = srcView.getImgWidth(); // replace by your own calculation
		int height = srcView.getImgHeight(); // replace by your own calculation
		int diagonal = getDiagonale(width, height);

		/***************************************************************************************/

		// create an empty destination image
		if (dstView == null)
			dstView = new ImageView(diagonal, diagonal);
		else
			dstView.resetToSize(diagonal, diagonal);

		// limit viewing dimensions
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		frame.pack();
	}

	private int getDiagonale(int width, int height) {
		return (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
	}

	/**
	 * Rotate source image and show result in destination view.
	 * 
	 * @param silent
	 *            - set true when running the speed test (suppresses the image
	 *            view).
	 */
	protected void rotateImage(boolean silent) {

		if (!silent) {
			// present some useful information
			statusLine.setText("Angle = " + angle + " degrees.");
		}

		// get dimensions and pixels references of images
		int srcPixels[] = srcView.getPixels();
		int srcWidth = srcView.getImgWidth();
		int srcHeight = srcView.getImgHeight();
		int dstPixels[] = dstView.getPixels();
		int dstWidth = dstView.getImgWidth();
		int dstHeight = dstView.getImgHeight();
		int diagonale = getDiagonale(srcWidth, srcHeight);
		double cos = 0;
		double sin = 0;
		double inversTranslationX = srcWidth/2.0;
		double inversTranslationY = srcHeight/2.0;


		long startTime = System.currentTimeMillis();

		switch (methodList.getSelectedIndex()) {
		case 0: // Nearest Neigbour
			cos = Math.cos(Math.toRadians(angle));
			sin = Math.sin(Math.toRadians(angle));
			rotateNearestNeigbour(srcPixels, srcWidth, srcHeight, dstPixels,
					dstWidth, dstHeight, diagonale, cos, sin, inversTranslationX, inversTranslationY);
			break;
		case 1: // Bilinear Interpolation
			cos = Math.cos(Math.toRadians(angle));
			sin = Math.sin(Math.toRadians(angle));
			rotateBilinear(srcPixels, srcWidth, srcHeight, dstPixels,
					dstWidth, dstHeight, diagonale, cos, sin, inversTranslationX, inversTranslationY);
			break;
		}

		if (!silent) {
			// show processing time
			long time = System.currentTimeMillis() - startTime;
			statusLine.setText("Angle = " + angle
					+ " degrees. Processing time = " + time + " ms.");
			// show the rotated image
			dstView.applyChanges();
		}
	}

	/**
	 * Image rotation algorithm using nearest neighbour image rendering
	 * 
	 * @param srcPixels
	 *            - source image pixel array of loaded image (ARGB values)
	 * @param srcWidth
	 *            - source image width
	 * @param srcHeight
	 *            - source image height
	 * @param dstPixels
	 *            - destination image pixel array to be filled (ARGB values)
	 * @param dstWidth
	 *            - destination image width
	 * @param dstHeight
	 *            - destination image height
	 */
	void rotateNearestNeigbour(int srcPixels[], int srcWidth, int srcHeight,
			int dstPixels[], int dstWidth, int dstHeight, int diagonale,double cos,double sin,double inversTranslationX,double inversTranslationY) {
		double translationX = 0;
		double translationY = 0;
		int valueX = 0;
		int valueY = 0;
		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				
				translationX = x - diagonale / 2.0;
				translationY = y - diagonale / 2.0;
				valueX = (int) Math.round(cos * translationX + sin * translationY + inversTranslationX);
				valueY = (int) Math.round(-sin * translationX + cos * translationY + inversTranslationY);
				
				if (valueX < 0 || valueX >= srcWidth || valueY < 0 || valueY >= srcHeight){
					dstPixels[y*dstWidth+x] = 0xffffffff;
				}
				else{
					dstPixels[y*dstWidth+x] = srcPixels[valueY * srcWidth +valueX];
				}
			}
		}

	}

	/**
	 * Image rotation algorithm using bilinear interpolation
	 * 
	 * @param srcPixels
	 *            - source image pixel array of loaded image (ARGB values)
	 * @param srcWidth
	 *            - source image width
	 * @param srcHeight
	 *            - source image height
	 * @param dstPixels
	 *            - destination image pixel array to be filled (ARGB values)
	 * @param dstWidth
	 *            - destination image width
	 * @param dstHeight
	 *            - destination image height
	 */
	void rotateBilinear(int srcPixels[], int srcWidth, int srcHeight,
			int dstPixels[], int dstWidth, int dstHeight, int diagonale,double cos,double sin,double inversTranslationX,double inversTranslationY) {
		
		double translationX = 0;
		double translationY = 0;
		int valueX = 0;
		int valueY = 0;
		for (int y = 0; y < dstHeight; y++) {
			for (int x = 0; x < dstWidth; x++) {
				
				translationX = x - diagonale / 2.0;
				translationY = y - diagonale / 2.0;
				valueX = (int) Math.round(cos * translationX + sin * translationY + inversTranslationX);
				System.out.println(valueX);
				valueY = (int) Math.round(-sin * translationX + cos * translationY + inversTranslationY);
				
				//wei�er Rahmen
				if (valueX < 0 || valueX >= srcWidth || valueY < 0 || valueY >= srcHeight){
					dstPixels[y*dstWidth+x] = 0xffffffff;
				}
				//Bildbehandlung
				else{
					int argb;
					int pos = (int) ((valueY) * srcWidth + (valueX));	
					if (pos < 0){
						 argb = 0xffffffff;
					}else if (pos >= (srcWidth * srcHeight)){
						argb = 0xffffffff;
					}else{
						argb = srcPixels[pos]; // Lesen der R,G,B-Werte
					}
					int r1 = (argb >> 16) & 0xff;
					int g1 = (argb >> 8) & 0xff;
					int b1 = argb & 0xff;

					pos = (int) ((valueY) * srcWidth + (valueX + 1));	
					if (pos < 0){
						 argb = 0xffffffff;
					}else if (pos >= (srcWidth * srcHeight)){
						argb = 0xffffffff;
					}else{
						argb = srcPixels[pos]; // Lesen der R,G,B-Werte
					}
					int r2 = (argb >> 16) & 0xff;
					int g2 = (argb >> 8) & 0xff;
					int b2 = argb & 0xff;

					pos = (int) ((valueY + 1) * srcWidth + (valueX));	
					if (pos < 0){
						 argb = 0xffffffff;
					}else if (pos >= (srcWidth * srcHeight)){
						argb = 0xffffffff;
					}else{
						argb = srcPixels[pos]; // Lesen der R,G,B-Werte
					}
					int r3 = (argb >> 16) & 0xff;
					int g3 = (argb >> 8) & 0xff;
					int b3 = argb & 0xff;

					pos = (int) ((valueY + 1) * srcWidth + (valueX + 1));	
					if (pos < 0){
						 argb = 0xffffffff;
					}else if (pos >= (srcWidth * srcHeight)){
						argb = 0xffffffff;
					}else{
						argb = srcPixels[pos]; // Lesen der R,G,B-Werte
					}
					int r4 = (argb >> 16) & 0xff;
					int g4 = (argb >> 8) & 0xff;
					int b4 = argb & 0xff;
					
//					System.out.println("h: " +  (cos * translationX + sin * translationY + inversTranslationX * x));
//					System.out.println("v: " + (-sin * translationX + cos * translationY + inversTranslationY * y));
					
					double h = ((cos * translationX + sin * translationY + inversTranslationX * x) % 1);
					double v = ((-sin * translationX + cos * translationY + inversTranslationY * y) % 1);

					// Berchnung der Bildpunkte an nicht ganzzahligen Pos., entsprechend der Gewichtung
					int r = (int) (r1 * (1 - h) * (1 - v) + r2 * h * (1 - v) + r3 * (1 - h) * v + r4 * h * v);
					int g = (int) (g1 * (1 - h) * (1 - v) + g2 * h * (1 - v) + g3 * (1 - h) * v + g4 * h * v);
					int b = (int) (b1 * (1 - h) * (1 - v) + b2 * h * (1 - v) + b3 * (1 - h) * v + b4 * h * v);
					

					int pos_n = y * dstWidth + x;


					dstPixels[pos_n] = (0xFF << 24) | (r << 16) | (g << 8) | b;
				}
			}
		}
	}


				

				
//	

}
