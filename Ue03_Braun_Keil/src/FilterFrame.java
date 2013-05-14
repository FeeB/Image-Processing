import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class FilterFrame extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "<Your Name>"; // type in your name
														// here
	private static final String initialFilename = "rhino_part.png";
	private static final int border = 10;
	private static final int maxWidth = 900;
	private static final int maxHeight = 900;
	private static JFrame frame;

	private ImageView srcView = null; // source image view
	private ImageView dstView = null; // rotated image view

	private JLabel statusLine; // to print some status text
	private int thresholdValue = 128;

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

		// convert to binary
		makeBinary(srcView);
		srcView.applyChanges();

		// create empty destination image of same size
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					makeBinary(srcView);
					srcView.applyChanges();
					dstView = new ImageView(srcView.getImgWidth(), srcView
							.getImgHeight());
					dstView.setMaxSize(new Dimension(maxWidth, maxHeight));
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
				makeBinary(srcView);
				srcView.applyChanges();
			}
		});
		
		// slider for filter
				final JSlider filterSlider = new JSlider(-500, 500, 0);
				filterSlider.setBorder(BorderFactory
						.createTitledBorder("Filter"));
				filterSlider.setMajorTickSpacing(100);
				filterSlider.setMinorTickSpacing(0);
				filterSlider.setPaintTicks(true);
				filterSlider.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						
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
		int pixels[] = imgView.getPixels();

		for (int i = 0; i < pixels.length; i++) {
			int r = giveBackValuePixel(argb_read(pixels[i], 16));
			int g = giveBackValuePixel(argb_read(pixels[i], 8));
			int b = giveBackValuePixel(argb_read(pixels[i], 0));

			pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;

		}
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
}
