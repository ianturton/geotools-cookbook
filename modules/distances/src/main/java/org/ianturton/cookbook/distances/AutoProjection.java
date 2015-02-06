package org.ianturton.cookbook.distances;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;

public class AutoProjection {
	static GeometryFactory geometryFactory = JTSFactoryFinder
			.getGeometryFactory(null);
	static WKTReader2 reader = new WKTReader2(geometryFactory);

	public static void main(String[] args) throws ParseException,
	MismatchedDimensionException, NoSuchAuthorityCodeException,
	FactoryException, TransformException {
		Point p1 = getRandomPoint();
		for (int i = 1; i < 50; i++) {

			double dx = i * Math.cos(Math.PI / 4);
			double dy = i * Math.sin(Math.PI / 4);
			Coordinate coordinate = new Coordinate(p1.getCoordinate().x + dx,
					p1.getCoordinate().y + dy);
			Point p2 = geometryFactory.createPoint(coordinate);
			// System.out.println(p2);
			System.out.print(Math.round(p1.distance(p2)) + " degrees \t");
			AutoProjection me = new AutoProjection();
			double distance = me.calculateDistance(p1, p2);
			double oDistance = me.calculateOrthoDistance(p1, p2);
			Measure<Double, Length> dist = Measure.valueOf(distance, SI.METER);
			Measure<Double, Length> oDist = Measure.valueOf(oDistance, SI.METER);
			double diff = Math.abs(distance - oDistance);
			Measure<Double, Length> oDiff = Measure.valueOf(diff, SI.METER);
			System.out.println("Proj: " + dist.doubleValue(SI.KILOMETER) + " Km"
					+ "\tOrtho: " + oDist.doubleValue(SI.KILOMETER) + " Km" + "\tDiff: "
					+ oDiff.doubleValue(SI.KILOMETER) + " Km");
		}
	}

	private double calculateDistance(Geometry g1, Geometry g2)
			throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		Point c1 = g1.getCentroid();
		Point c2 = g2.getCentroid();

		Coordinate[] coordinates = new Coordinate[2];
		coordinates[0] = c1.getCoordinate();
		coordinates[1] = c2.getCoordinate();
		LineString line = geometryFactory.createLineString(coordinates);
		Point c = line.getCentroid();
		double x = c.getCoordinate().x;
		double y = c.getCoordinate().y;
		/*
		 * double x = (c1.getCoordinate().x - c2.getCoordinate().x) +
		 * c1.getCoordinate().x; while (x > 180.0) { x -= 360.0; } while (x <
		 * -180.0) { x += 360.0; }
		 *
		 * double y = (c1.getCoordinate().y - c2.getCoordinate().y) +
		 * c1.getCoordinate().y; while (y > 90.0) { y -= 180.0; } while (x < -90.0)
		 * { y += 180.0; }
		 */
		String code = "AUTO:42001," + y + "," + x;
		// System.out.println(code);
		CoordinateReferenceSystem auto = CRS.decode(code);
		// System.out.println(auto);
		MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84,
				auto);
		Geometry g3 = JTS.transform(g1, transform);
		Geometry g4 = JTS.transform(g2, transform);
		return g3.distance(g4);
	}

	private double calculateOrthoDistance(Geometry g1, Geometry g2) {
		double distance = 0.0;
		Point c1 = g1.getCentroid();
		Point c2 = g2.getCentroid();
		GeodeticCalculator calc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
		calc.setStartingGeographicPoint(c1.getX(), c1.getY());
		calc.setDestinationGeographicPoint(c2.getX(), c2.getY());
		distance = calc.getOrthodromicDistance();

		return distance;
	}

	private static Point getRandomPoint() {
		double lat = -90.0 + Math.random() * 180.0;
		double lon = -180.0 + Math.random() * 360.0;
		Point p = geometryFactory.createPoint(new Coordinate(lon, lat));
		// System.out.println(p);
		return p;
	}
}
