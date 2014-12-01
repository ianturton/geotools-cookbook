package org.ianturton.cookbook.output;

import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
import org.geotools.map.MapViewport;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * draw a map with graticules
 *
 * @author ian.turton
 *
 */
public class MapWithGrid implements MapBoundsListener {

	private JMapFrame frame;
	private MapContent mapContent;
	private FeatureLayer layer;
	private Layer gridLayer;
	private Style style;

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
		style = SLD.createSimpleStyle(featureSource.getSchema());
		layer = new FeatureLayer(featureSource, style);

		ReferencedEnvelope gridBounds = layer.getBounds();
		gridLayer = createGridLayer(style, gridBounds);
		mapContent.addLayer(layer);
		mapContent.addLayer(gridLayer);
		mapContent.addMapBoundsListener(this);
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

	public void mapBoundsChanged(MapBoundsEvent event) {
		// this fires on a CRS change as well as a PAN/ZOOM
		if (event.getEventType().contains(MapBoundsEvent.Type.CRS)) {
			CoordinateReferenceSystem crs = event.getNewCoordinateReferenceSystem();
			Extent bounds = crs.getDomainOfValidity();

			if (bounds == null) {
				return;
			}
			@SuppressWarnings("unchecked")
			ArrayList<? extends GeographicBoundingBox> ex = new ArrayList<GeographicBoundingBox>(
					(Collection<? extends GeographicBoundingBox>) bounds
							.getGeographicElements());
			GeographicBoundingBox box = ex.get(0);
			ReferencedEnvelope env = new ReferencedEnvelope(
					DefaultGeographicCRS.WGS84);
			env.expandToInclude(box.getWestBoundLongitude(),
					box.getSouthBoundLatitude());
			env.expandToInclude(box.getEastBoundLongitude(),
					box.getNorthBoundLatitude());
			System.out.println(env);

			MapViewport viewport = frame.getMapPane().getMapContent().getViewport();

			viewport.setBounds(env);
			// mapContent.removeLayer(gridLayer);
			/*
			 * try { gridLayer = createGridLayer(style, res); } catch (IOException e)
			 * { // TODO Auto-generated catch block e.printStackTrace(); return; }
			 * mapContent.addLayer(gridLayer);
			 */

		}

	}
}
