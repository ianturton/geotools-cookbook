package org.ianturton.cookbook.projections;

import java.awt.Color;
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
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Tissot {

	private static final String DEFAULT_GEOMETRY_ATTRIBUTE_NAME = "the_geom";
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
		new Tissot(file);
	}

	public Tissot(File file) throws IOException {

		FileDataStore store = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource featureSource = store.getFeatureSource();

		// Create a map content and add our shapefile to it
		mapContent = new MapContent();
		mapContent.setTitle("GeoTools Mapping");
		Style style = SLD.createSimpleStyle(featureSource.getSchema());
		Layer layer = new FeatureLayer(featureSource, style);
		mapContent.addLayer(layer);
		ReferencedEnvelope gridBounds = layer.getBounds();
		Layer gridLayer = createGridLayer(style, gridBounds);
		mapContent.addLayer(gridLayer);
		Style pstyle = SLD.createPointStyle("circle", Color.red, Color.red, 1.0f,
				5.0f);
		Layer tissotLayer = createTissotLayer(pstyle, gridBounds);
		mapContent.addLayer(tissotLayer);
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

	private FeatureType createFeatureType(String name,
			CoordinateReferenceSystem crs) {
		SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
		String finalName;
		if (name == null || name.isEmpty()) {
			finalName = "Tissot";
		} else {
			finalName = name;
		}
		tb.setName(finalName);
		tb.add(DEFAULT_GEOMETRY_ATTRIBUTE_NAME, Point.class, crs);
		tb.add("id", String.class);
		return tb.buildFeatureType();
	}

	/**
	 * A method to create Tissot Indicatrices based on Whuber's answer to
	 * http://gis
	 * .stackexchange.com/questions/5068/how-to-create-an-accurate-tissot
	 * -indicatrix
	 *
	 * @param style
	 *          - the style to draw the circles with
	 * @param gridBounds
	 *          - the bounds of the map (may be increased in the method)
	 * @return a layer of Tissot Indicatrices (scaled for visibility).
	 */
	private Layer createTissotLayer(Style style, ReferencedEnvelope gridBounds) {
		FeatureType type = createFeatureType(null,
				gridBounds.getCoordinateReferenceSystem());
		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(
				(SimpleFeatureType) type);
		double step = 20.0;
		GeometryFactory geomFac = new GeometryFactory();
		double width = gridBounds.getWidth();
		double height = gridBounds.getHeight();
		int id = 0;
		final ListFeatureCollection fc = new ListFeatureCollection(
				(SimpleFeatureType) type);
		double y = gridBounds.getMinY();

		for (int iy = 0; iy < (height / step); iy++) {
			double x = gridBounds.getMinX();
			for (int ix = 0; ix < (width / step); ix++) {
				Point p = geomFac.createPoint(new Coordinate(x, y));

				sfb.set("the_geom", p);
				SimpleFeature f = sfb.buildFeature("tissot2" + id);
				fc.add(f);
				x += step;
			}
			y += step;
		}

		Layer layer = new FeatureLayer(fc, style);
		return layer;
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

	private Layer createGridLayer(Style style, ReferencedEnvelope gridBounds)
			throws IOException {
		double squareWidth = 20.0;
		double extent = gridBounds.maxExtent();
		double ll = Math.log10(extent);
		if (ll > 0) {
			// there are ll 10's across the map
			while (ll-- > 4) {
				squareWidth *= 10;
			}
		}

		// max distance between vertices
		double vertexSpacing = squareWidth / 20;
		// grow to cover the whole map (and a bit).
		double left = gridBounds.getMinX();
		double bottom = gridBounds.getMinY();

		if (left % squareWidth != 0) {
			if (left > 0.0) { // east
				left -= Math.abs(left % squareWidth);
			} else { // west
				left += Math.abs(left % squareWidth);
			}
		}

		if (bottom % squareWidth != 0) {
			if (bottom > 0.0) {
				bottom -= Math.abs(bottom % squareWidth);
			} else {
				bottom += Math.abs(bottom % squareWidth);
			}
		}

		gridBounds.expandToInclude(left, bottom);
		double right = gridBounds.getMaxX();
		double top = gridBounds.getMaxY();
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

		gridBounds.expandToInclude(right, top);
		SimpleFeatureSource grid = Grids.createSquareGrid(gridBounds, squareWidth,
				vertexSpacing);
		Layer gridLayer = new FeatureLayer(grid.getFeatures(), style);
		return gridLayer;
	}
}
