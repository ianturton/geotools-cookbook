package org.ianturton.cookbook.output;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.geotools.swing.data.JFileDataStoreChooser;

public class SaveMapAsImage {

	private JMapFrame frame;
	private MapContent mapContent;

	public static void main(String[] args) throws IOException {
		File file = null;
		if (args.length == 0) {
			// display a data store file chooser dialog for shapefiles
			file = JFileDataStoreChooser.showOpenFile("shp", null);
			if (file == null) {
				return;
			}
		} else {
			file = new File(args[0]);
			if (!file.exists()) {
				System.err.println(file + " doesn't exist");
				return;
			}
		}
		new SaveMapAsImage(file);
	}

	public SaveMapAsImage(File file) throws IOException {

		FileDataStore store = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource featureSource = store.getFeatureSource();

		// Create a map content and add our shapefile to it
		mapContent = new MapContent();
		mapContent.setTitle("GeoTools Mapping");

		Style style = SLD.createSimpleStyle(featureSource.getSchema());
		Layer layer = new FeatureLayer(featureSource, style);
		mapContent.addLayer(layer);
		frame = new JMapFrame(mapContent);
		frame.enableStatusBar(true);
		frame.enableToolBar(true);
		JToolBar toolBar = frame.getToolBar();
		toolBar.addSeparator();
		SaveAction save = new SaveAction("Save");
		toolBar.add(save);
		frame.initComponents();

		frame.setSize(1000, 500);
		frame.setVisible(true);

	}



	public void drawMapToImage(File outputFile, String outputType) {

		// Initialise a renderer
		JMapPane mapPane = frame.getMapPane();
		GTRenderer renderer = mapPane.getRenderer();

		Rectangle rectangle = new Rectangle(frame.getWidth(), frame.getHeight());

		BufferedImage bufferedImage;
		bufferedImage = new BufferedImage(rectangle.width, rectangle.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = bufferedImage.createGraphics();
		
		// Set white background

		graphics2D.setBackground(Color.white);
		graphics2D.fillRect(0, 0, rectangle.width, rectangle.height);

		renderer.paint(graphics2D, rectangle, mapPane.getDisplayArea());
		
		ImageOutputStream outputImageFile = null;
		try {	
			outputImageFile = ImageIO.createImageOutputStream(new FileOutputStream(outputFile));
			ImageIO.write(bufferedImage, outputType, outputImageFile);
		} catch (IOException ex) {

		} finally {

			graphics2D.dispose();
			try {
				outputImageFile.close();
			} catch (IOException e) {
				// don't care now
			}
		}
	}
	JDialog dialog = new JDialog(frame);
	private class SaveAction extends AbstractAction {
		/**
		 * Private SaveAction 
		 */
		private static final long serialVersionUID = 3071568727121984649L;

		public SaveAction(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent arg0) {
			/*dialog.setModal(true);
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);*/
			drawMapToImage(new File("ian.png"),"png");

		}

	}
}
