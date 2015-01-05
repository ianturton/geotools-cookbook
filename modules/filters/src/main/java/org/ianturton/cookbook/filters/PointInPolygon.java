package org.ianturton.cookbook.filters;

import java.io.File;
import java.io.IOException;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class PointInPolygon {
	FilterFactory2 filterFactory;
	private SimpleFeatureCollection features;
	private ReferencedEnvelope env;

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
		PointInPolygon tester = new PointInPolygon();
		FileDataStore store = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource featureSource = store.getFeatureSource();
		tester.setFeatures(featureSource.getFeatures());
		GeometryFactory fac = new GeometryFactory();
		for (int i = 0; i < 1000; i++) {

			double lat = (Math.random() * 180.0) - 90.0;
			double lon = (Math.random() * 360.0) - 180.0;
			Point p = fac.createPoint(new Coordinate(lat, lon));
			boolean flag = tester.isInShape(p);
			if (flag) {
				System.out.println(p + " is in States ");
			}
		}
	}

	public PointInPolygon() {

		filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools
				.getDefaultHints());
	}

	private boolean isInShape(Point p) {
		if (!env.contains(p.getCoordinate())) {
			return false;
		}
		Expression propertyName = filterFactory.property(features.getSchema()
				.getGeometryDescriptor().getName());
		Filter filter = filterFactory.contains(propertyName,
				filterFactory.literal(p));
		SimpleFeatureCollection sub = features.subCollection(filter);
		if (sub.size() > 0) {
			return true;
		}
		return false;
	}

	private void setFeatures(SimpleFeatureCollection features) {
		this.features = features;
		env = features.getBounds();
	}

}
