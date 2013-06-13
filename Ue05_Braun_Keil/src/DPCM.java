// BV Ue01 SS2013
//

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class DPCM extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int borderWidth = 5;
	private static final int maxWidth = 400;
	private static final int maxHeight = maxWidth;
	private static JFrame frame;

	private ImageView origView; // source image view
	private ImageView praedError; // filtered image view
	private ImageView recView; // filtered image view

	private int[] origPixels = null;

	private JLabel statusLine = new JLabel("   "); // to print some status text

	private JComboBox noiseType;
	public DPCM() {
		super(new BorderLayout(borderWidth, borderWidth));

		setBorder(BorderFactory.createEmptyBorder(borderWidth, borderWidth,
				borderWidth, borderWidth));

		// load the default image
		File input = new File("lena_klein.png");

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		origView = new ImageView(input);
		origView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// convert to grayscale
		makeGray(origView);

		// keep a copy of the grayscaled original image pixels
		origPixels = origView.getPixels().clone();

		// create empty destination image of same size
		praedError = new ImageView(origView.getImgWidth(), origView.getImgHeight());
		praedError.setMaxSize(new Dimension(maxWidth, maxHeight));

		// create empty destination image of same size
		recView = new ImageView(origView.getImgWidth(), origView.getImgHeight());
		recView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
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
				makeGray(origView);
				// keep a copy of the grayscaled original image pixels
				origPixels = origView.getPixels().clone();
			}
		});

		// selector for the noise method
		String[] noiseNames = { "A (horizontal)", "B (vertikal)", "C (diagonal)", "A+B-C", "(A+B)/2", "adaptiv" };

		noiseType = new JComboBox(noiseNames);
		noiseType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});

		controls.add(load, c);
		controls.add(noiseType, c);

		// images panel
		JPanel images = new JPanel(new GridLayout(1, 4));
		images.add(origView);
		images.add(praedError);
		images.add(recView);

		// status panel
		JPanel status = new JPanel(new GridBagLayout());

		status.add(statusLine, c);

		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
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
			origView.loadImage(file);
			origView.setMaxSize(new Dimension(maxWidth, maxHeight));
			// create empty destination image of same size
			praedError.resetToSize(origView.getImgWidth(), origView.getImgHeight());
			frame.pack();
		}

	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Ue01_Nachname_Vorname");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new DPCM();
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
	
	private int argb_read(int pixel, int shift_value) {
		int x = (pixel >> shift_value) & 0xff;
		return x;
	}
}
