import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class FilterFrame extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "<Fee Braun & Stefan Keil>"; // type in
																		// your
																		// name
	// here
	private static final String initialFilename = "rhino_part_bw.png";
	private static final int border = 10;
	private static final int maxWidth = 900;
	private static final int maxHeight = 900;
	private static JFrame frame;

	private ImageView srcView = null; // source image view
	private ImageView dstView = null; // final image view
	private ImageView bwView = null; // BW Image
	private ImageView tmpView = null; // nach Filter1

	private JLabel statusLine; // to print some status text
	private int thresholdValue = 128;
	private float radius=0;
	private float secondRadius=0;

	private int[] origPixels = null;

	public FilterFrame() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
		origPixels = srcView.getPixels().clone();
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		bwView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		tmpView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
	

	//	 convert to gray;binary
		makeBinary(makeGray(srcView));
		dilation(bwView, tmpView, radius);
		dilation(tmpView, dstView, secondRadius);

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					origPixels = srcView.getPixels().clone();
					dstView.setSize(srcView.getImgWidth(),
							srcView.getImgHeight());
					bwView.setSize(srcView.getImgWidth(),
							srcView.getImgHeight());
					tmpView.setSize(srcView.getImgWidth(),
							srcView.getImgHeight());
					makeBinary(makeGray(srcView));
					dilation(bwView, tmpView, radius);
					dilation(tmpView, dstView, secondRadius);
				}
			}
		});

		// slider for threshold
		final JSlider thresholdSlider = new JSlider(0, 255, 128);
		thresholdSlider.setBorder(BorderFactory
				.createTitledBorder("Schwellenwert"));
		thresholdSlider.setMajorTickSpacing(10);
		thresholdSlider.setMinorTickSpacing(0);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				thresholdValue = thresholdSlider.getValue();
				srcView.setPixels(origPixels);
				makeBinary(makeGray(srcView));
				dilation(bwView, tmpView, radius);
				dilation(tmpView, dstView, secondRadius);
			}
		});

		// slider for filter1
		final JSlider filterSlider = new JSlider(-50, 50, 0);
		filterSlider.setBorder(BorderFactory.createTitledBorder("Filter 1"));
		filterSlider.setMajorTickSpacing(100);
		filterSlider.setMinorTickSpacing(0);
		filterSlider.setPaintTicks(true);
		filterSlider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				radius = filterSlider.getValue() / 10.0f;
				if (filterSlider.getValue() >= 0) {
					dilation(bwView, tmpView, radius);
				} else {
					// invert(I)+invert(H)
					invertBinaryImage(bwView);
					dilation(bwView, tmpView, -radius);
					
					invertBinaryImage(bwView);
					invertBinaryImage(tmpView);
				}
				dilation(tmpView, dstView, secondRadius);
			}

		});

		// slider for filter2
		final JSlider filterSliderSecond = new JSlider(-50, 50, 0);
		filterSliderSecond.setBorder(BorderFactory
				.createTitledBorder("Filter 2"));
		filterSliderSecond.setMajorTickSpacing(100);
		filterSliderSecond.setMinorTickSpacing(0);
		filterSliderSecond.setPaintTicks(true);
		filterSliderSecond.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				secondRadius = filterSliderSecond.getValue() / 10.0f;
				if (filterSliderSecond.getValue() >= 0) {
					dilation(bwView, tmpView, secondRadius);
				
				} else {
					invertBinaryImage(bwView);
					dilation(bwView, tmpView, -secondRadius);
					
					invertBinaryImage(bwView);
					invertBinaryImage(tmpView);
					dilation(tmpView, dstView, radius);
				}
				dilation(tmpView, dstView, radius);
					
			}

		});

		// some status text
		statusLine = new JLabel(" ");

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(thresholdSlider, c);
		controls.add(filterSlider, c);
		controls.add(filterSliderSecond, c);

		// arrange images
		JPanel images = new JPanel();
		images.add(dstView);

		// add to main panel
		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		// add border to main panel
		setBorder(BorderFactory.createEmptyBorder(border, border, border,
				border));
	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Rotation - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent contentPane = new FilterFrame();
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

	public static void main(String[] args) {
		// schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

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

	private void makeBinary(ImageView imgView) {
		int srcPixels[] = imgView.getPixels();
		int[] bwArray = bwView.getPixels();

		for (int i = 0; i < srcPixels.length; i++) {
			int r = giveBackValuePixel(argb_read(srcPixels[i], 16));
			int g = giveBackValuePixel(argb_read(srcPixels[i], 8));
			int b = giveBackValuePixel(argb_read(srcPixels[i], 0));

			// r = b = g = 0;

			bwArray[i] = 0xff000000 | (r << 16) | (g << 8) | b;

		}
		bwView.applyChanges();
		return;
	}

	private int giveBackValuePixel(int pixel) {
		if (pixel > thresholdValue) {
			return 255;
		} else {
			return 0;
		}
	}

	private int argb_read(int pixel, int shift_value) {
		int x = (pixel >> shift_value) & 0xff;
		return x;
	}

	private ImageView makeGray(ImageView imgView) {
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
		imgView.applyChanges();
		return imgView;
	}

	private void dilation(ImageView srcView, ImageView dstView, float radius) {
		// todo : dstView = wei§
		int[] dstArray = dstView.getPixels();
		int[] srcArray = srcView.getPixels();
		for (int i = 0; i < dstArray.length; i++) {
			dstArray[i] = 0xffffffff;
		}

		int w = srcView.getImgWidth();
		int h = srcView.getImgHeight();

		for (int y = 0; y < srcView.getImgHeight(); y++) {
			for (int x = 0; x < srcView.getImgWidth(); x++) {
				// if (y >= 0 && y < srcView.getImgHeight() && x >= 0 && x <
				// srcView.getImgWidth())
				// {
				if (srcArray[y * dstView.getImgWidth() + x] == 0xff000000) {
					makeNeighbourhoodBlack(dstView, radius, y, x);
				}
				// }
			}
		}
		dstView.applyChanges();
	}

	private void makeNeighbourhoodBlack(ImageView dstView, float radius,
			int mittelpunktY, int mittelpunktX) {
		int[] dstArray = dstView.getPixels();
		int d = Math.round(radius);
		// System.out.println(d);
		for (int y = mittelpunktY - d; y <= mittelpunktY + d; y++) {
			for (int x = mittelpunktX - d; x <= mittelpunktX + d; x++) {
				if (x >= 0 && y >= 0 && x < dstView.getImgWidth()
						&& y < dstView.getImgHeight()) {
					int distance = (int) (Math.sqrt(Math.pow(mittelpunktX - x,
							2) + Math.pow(mittelpunktY - y, 2)));
					if (distance <= radius) {
						dstArray[y * dstView.getImgWidth() + x] = 0xff000000;
					}
				}
			}
		}
	}

	private void invertBinaryImage(ImageView srcView) {
		int[] pixels = srcView.getPixels();
		for (int i = 0; i < pixels.length; i++) {
			int r = invert(argb_read(pixels[i], 16));
			int b = invert(argb_read(pixels[i], 8));
			int g = invert(argb_read(pixels[i], 0));

			pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
		}
		srcView.applyChanges();
	}

	private int invert(int pixel) {
		return pixel == 255 ? 0 : 255;
	}
}
