package org.ianturton.cookbook.styling;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.ianturton.cookbook.output.SaveMapAsImage;
import org.opengis.feature.simple.SimpleFeatureType;

public class TwoAttributes {
	private File infile;
	private File outFile;
	SaveMapAsImage saver = new SaveMapAsImage();
	private JMapFrame frame;

	public TwoAttributes(String[] args) throws IOException {
		File file = new File(args[0]);
		FileDataStore store = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource featureSource = store.getFeatureSource();
		SimpleFeatureType schema = featureSource.getSchema();
		System.out.println(schema);
		// Create a map content and add our shapefile to it
		MapContent mapContent = new MapContent();
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

	public static void main(String[] args) throws IOException {
		TwoAttributes me = new TwoAttributes(args);
	}

	private class SaveAction extends AbstractAction {
		/**
		 * Private SaveAction
		 */
		private static final long serialVersionUID = 3071568727121984649L;

		public SaveAction(String text) {
			super(text);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String[] writers = ImageIO.getWriterFormatNames();

			String format = (String) JOptionPane.showInputDialog(frame,
					"Choose output format:", "Customized Dialog",
					JOptionPane.PLAIN_MESSAGE, null, writers, "png");
			saver.drawMapToImage(new File("ian." + format), format,
					frame.getMapPane());

		}

	}
}
