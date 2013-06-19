// BV Ue01 SS2013
//

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

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
	private int[] praedikation;

	private JLabel statusLine = new JLabel("   "); // to print some status text
	private JLabel label = new JLabel("   ");
	private JLabel label1 = new JLabel("   ");
	private JLabel label2 = new JLabel("   ");
	private JLabel label3 = new JLabel("   ");

	private JComboBox praedikationType;
	public DPCM() {
		super(new BorderLayout(borderWidth, borderWidth));

		setBorder(BorderFactory.createEmptyBorder(borderWidth, borderWidth,
				borderWidth, borderWidth));

		// load the default image
		File input = new File("test1.jpg");

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
		
		preadikation(1);
		
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

		// selector for the praedikation type
		String[] praedikationNames = { "A (horizontal)", "B (vertikal)", "C (diagonal)", "A+B-C", "(A+B)/2", "adaptiv" };

		praedikationType = new JComboBox(praedikationNames);
		praedikationType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (praedikationType.getSelectedIndex() == 0){
					preadikation(1);
				} else if (praedikationType.getSelectedIndex() == 1){
					preadikation(2);
				} else if (praedikationType.getSelectedIndex() == 2){
					preadikation(3);
				} else if (praedikationType.getSelectedIndex() == 3){
					preadikation(4);
				} else if (praedikationType.getSelectedIndex() == 4){
					preadikation(5);
				} else if (praedikationType.getSelectedIndex() == 5){
					preadikation(6);
				}
			}
		});

		controls.add(load, c);
		controls.add(praedikationType, c);
		
		// text display
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        
        String string = " ";
        
        label = new JLabel(string);
        label.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 50));
        label1 = new JLabel(string);
        label1.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));
        label2 = new JLabel(string);
        label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));
        label3 = new JLabel(string);
        label3.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 50));

        controlPanel.add(label);
        controlPanel.add(label1);
        controlPanel.add(label2);
        controlPanel.add(label3);
        
		
		updateText();

		// images panel
		JPanel images = new JPanel(new GridLayout(1, 3));
		
		images.add(origView);
		images.add(praedError);
		images.add(recView);

		// status panel
		JPanel status = new JPanel(new GridBagLayout());

		status.add(statusLine, c);

		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
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
			makeGray(origView);
			origView.setMaxSize(new Dimension(maxWidth, maxHeight));
			// create empty destination image of same size
			praedError.resetToSize(origView.getImgWidth(), origView.getImgHeight());
			recView.resetToSize(origView.getImgWidth(), origView.getImgHeight());
			preadikation(1);
			frame.pack();
		}

	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Ue05_Braun_Keil");
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
		imgView.applyChanges();
	}
	
	private int argb_read(int pixel, int shift_value) {
		int x = (pixel >> shift_value) & 0xff;
		return x;
	}
	
	private void preadikation(int type){
		praedikation = new int[origView.getPixels().length];
		int src[] = origView.getPixels();
		for (int y = 0; y < origView.getImgHeight(); y++){
			for (int x = 0; x < origView.getImgWidth(); x++){
				if (type == 1 && x != 0){
					praedikation[y * praedError.getImgWidth() + x] = src[y * origView.getImgWidth() + x -1];
				}else if(type == 2 && y != 0){
					praedikation[y * praedError.getImgWidth() + x] = src[(y-1) * origView.getImgWidth() + x];
				}else if(type == 3 && y != 0 && x != 0){
					praedikation[y * praedError.getImgWidth() + x] = src[(y-1) * origView.getImgWidth() + (x -1)];
				}else if(type == 4 && y != 0 && x != 0){
					praedikation[y * praedError.getImgWidth() + x] = src[y * origView.getImgWidth() + x -1] + src[(y-1) * origView.getImgWidth() + x] - src[(y-1) * origView.getImgWidth() + x -1];
				}else if(type == 5 && y != 0 && x != 0){
					int a = src[y * origView.getImgWidth() + x -1];
					int b = src[(y-1) * origView.getImgWidth() + x];
					praedikation[y * praedError.getImgWidth() + x] = (a+b)/2;
				}else if (type == 6 && y != 0 && x != 0){
					int aMinusC = 0;
					int bMinusC = 0;
					if (src[y * origView.getImgWidth() + x -1]-src[(y-1) * origView.getImgWidth() + x -1] < 0){
						aMinusC = (src[y * origView.getImgWidth() + x -1]-src[(y-1) * origView.getImgWidth() + x -1])*(-1);
					}else{
						aMinusC = (src[y * origView.getImgWidth() + x -1]-src[(y-1) * origView.getImgWidth() + x -1]);
					}
					if (src[(y-1) * origView.getImgWidth() + x]-src[(y-1) * origView.getImgWidth() + x -1] < 0){
						bMinusC = (src[(y-1) * origView.getImgWidth() + x]-src[(y-1) * origView.getImgWidth() + x -1])*(-1);
					}else{
						bMinusC = (src[(y-1) * origView.getImgWidth() + x]-src[(y-1) * origView.getImgWidth() + x -1]);
					}
					
					if (aMinusC < bMinusC){
						praedikation[y * praedError.getImgWidth() + x] = src[(y-1) * origView.getImgWidth() + x];
					}else{
						praedikation[y * praedError.getImgWidth() + x] = src[y * origView.getImgWidth() + x -1];
					}
				}else{
					praedikation[y * praedError.getImgWidth() + x] = 0xFFFFFF;
				}
				
			}
		}
		createPraedikationImage();
	}
	
	public void createPraedikationImage(){
		int dst[] = new int[origView.getPixels().length];
		int src[] = origView.getPixels();
		for (int y = 0; y < praedError.getImgHeight(); y++){
			for (int x = 0; x < praedError.getImgWidth(); x++){
				int error = src[y * praedError.getImgWidth() + x] - praedikation[y * praedError.getImgWidth() + x];
				error = error + 0xff808080;
				dst[y * praedError.getImgWidth() + x] = error;
			}
			praedError.setPixels(dst);
		}
		createReconstructedImage();
	}
	
	public void createReconstructedImage(){
		int dst[] = new int[origView.getPixels().length];
		int error[] = praedError.getPixels();
		for (int y = 0; y < recView.getImgHeight(); y++){
			for (int x = 0; x < recView.getImgWidth(); x++){
				dst[y * recView.getImgWidth() + x] = (error[y * praedError.getImgWidth() + x] - 0xff808080) + praedikation[y * praedError.getImgWidth() + x];
			}
		}
		recView.setPixels(dst);
		updateText();
	}
	
	public double calculateEntropy(ImageView imgView){
		int[] pixels = imgView.getPixels();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < pixels.length; i++){
			if(!map.containsKey(pixels[i])){
				map.put(pixels[i], 1);
			}else{
				map.put(pixels[i], map.get(pixels[i])+1);
			}
		}
		
		double entropy = 0.0;
		for (int i : map.keySet()) {
			double frequency = map.get(i);
			double size = pixels.length;
			double result = frequency/size;
			entropy += result * (Math.log(result) / Math.log(2));
		}
		double r = -(Math.round(entropy*1000));
		return r /1000;
	}
	
	public double calculateMSE(ImageView imgView){
		int[] recPixels = imgView.getPixels();
		int [] orgPixels= origView.getPixels();
		double mse =0.0;
		for(int i=0;i<recPixels.length;i++){
				mse+= Math.pow((orgPixels[i]-recPixels[i]),2);
		}
		mse= mse/recPixels.length;
		return mse;
	}
	
	private void updateText() {
		
		String entropie1 = "Entropie: " + calculateEntropy(origView);
		String entropie2 = "Entropie: " + calculateEntropy(praedError);
		String entropie3 = "Entropie: " + calculateEntropy(recView);
		String mse = "MSE: " + calculateMSE(recView);
		
		label.setText(entropie1);
		label1.setText(entropie2);
		label2.setText(entropie3);
		label3.setText(mse);
	}
}
