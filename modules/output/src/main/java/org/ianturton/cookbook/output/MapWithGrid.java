package org.ianturton.cookbook.output;

import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Grids;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.geotools.swing.data.JFileDataStoreChooser;

/**
 * draw a map with graticules
 *
 * @author ian.turton
 *
 */
public class MapWithGrid {

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
		new MapWithGrid(file);
	}

	public MapWithGrid(File file) throws IOException {

		FileDataStore store = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource featureSource = store.getFeatureSource();

		// Create a map content and add our shapefile to it
		mapContent = new MapContent();
		mapContent.setTitle("GeoTools Mapping");
		Style style = SLD.createSimpleStyle(featureSource.getSchema());
		Layer layer = new FeatureLayer(featureSource, style);

		double squareWidth = 20.0;

		// max distance between vertices
		double vertexSpacing = squareWidth / 20;

		ReferencedEnvelope gridBounds = layer.getBounds();
		// grow to cover the whole map (and a bit).
		double left = gridBounds.getMinX();
		double bottom = gridBounds.getMinY();
		System.out.println(left + "," + bottom);
		System.out.println("deltaX = " + (left % squareWidth));
		if (left % squareWidth != 0) {
			if (left > 0.0) { // east
				left -= Math.abs(left % squareWidth);
			} else { // west
				left += Math.abs(left % squareWidth);
			}
		}
		System.out.println("deltaY = " + (bottom % squareWidth));
		if (bottom % squareWidth != 0) {
			if (bottom > 0.0) {
				bottom -= Math.abs(bottom % squareWidth);
			} else {
				bottom += Math.abs(bottom % squareWidth);
			}
		}
		System.out.println(left + "," + bottom);
		gridBounds.expandToInclude(left, bottom);
		double right = gridBounds.getMaxX();
		double top = gridBounds.getMaxY();
		System.out.println("deltaX = " + (right % squareWidth));
		System.out.println("deltaY = " + (top % squareWidth));
		System.out.println(right + "," + top);
		if (right % squareWidth != 0) {
			if (right > 0.0) { // east
				right += Math.abs(right % squareWidth) + squareWidth;
			} else { // west
				right -= Math.abs(right % squareWidth) - squareWidth;
			}
		}

		if (top % squareWidth != 0) {
			if (top > 0.0) { // North
				top += Math.abs(top % squareWidth) + squareWidth;
			} else { // South
				top -= Math.abs(top % squareWidth) - squareWidth;
			}
		}
		System.out.println(right + "," + top);
		gridBounds.expandToInclude(right, top);
		SimpleFeatureSource grid = Grids.createSquareGrid(gridBounds, squareWidth,
				vertexSpacing);
		Layer gridLayer = new FeatureLayer(grid.getFeatures(), style);
		mapContent.addLayer(layer);
		mapContent.addLayer(gridLayer);
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
		JMapPane mapPane = frame.getMapPane();
		ImageOutputStream outputImageFile = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(outputFile);
			outputImageFile = ImageIO.createImageOutputStream(fileOutputStream);
			RenderedImage bufferedImage = mapPane.getBaseImage();
			ImageIO.write(bufferedImage, outputType, outputImageFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (outputImageFile != null) {
					outputImageFile.flush();
					outputImageFile.close();
					fileOutputStream.flush();
					fileOutputStream.close();
				}
			} catch (IOException e) {// don't care now
			}
		}
	}

	private class SaveAction extends AbstractAction {
		/**
		 * Private SaveAction
		 */
		private static final long serialVersionUID = 3071568727121984649L;

		public SaveAction(String text) {
			super(text);
		}

		public void actionPerformed(ActionEvent arg0) {
			String[] writers = ImageIO.getWriterFormatNames();

			String format = (String) JOptionPane.showInputDialog(frame,
					"Choose output format:", "Customized Dialog",
					JOptionPane.PLAIN_MESSAGE, null, writers, "png");
			drawMapToImage(new File("ian." + format), format);

		}

	}
}
