package org.ianturton.cookbook.output;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ExportGeometries {

	public static void main(String[] args) {
		GeometryFactory fac = new GeometryFactory();
		Point p = fac.createPoint(new Coordinate(10.1, 22.2));
		System.out.println(getWKT(p));
		System.out.println(getCoordinates(p));
		Coordinate[] coordinates = { new Coordinate(1, 1), new Coordinate(2, 3),
				new Coordinate(4.4, 5.2) };
		LineString line = fac.createLineString(coordinates);
		System.out.println(getWKT(line));
		System.out.println(getCoordinates(line));
		Coordinate[] pcoordinates = { new Coordinate(1, 1), new Coordinate(2, 3),
				new Coordinate(4.4, 5.2), new Coordinate(1, 1) };
		Polygon poly = fac.createPolygon(pcoordinates);
		System.out.println(getWKT(poly));
		System.out.println(getCoordinates(poly));
		Coordinate[] pcoordinates2 = { new Coordinate(10, 10),
				new Coordinate(20, 30), new Coordinate(40.4, 50.2),
				new Coordinate(10, 10) };
		Polygon poly2 = fac.createPolygon(pcoordinates2);
		MultiPolygon multiPoly = fac
				.createMultiPolygon(new Polygon[] { poly, poly2 });
		System.out.println(getWKT(multiPoly));
		System.out.println(getCoordinates(multiPoly));
	}

	static String getWKT(Geometry geom) {
		return geom.toText();
	}

	static String getCoordinates(Geometry geom) {
		StringBuilder builder = new StringBuilder();
		if (geom.getNumGeometries() > 1) {
			for (int i = 0; i < geom.getNumGeometries(); i++) {
				builder.append(getCoordinates(geom.getGeometryN(i)));
				builder.append("\n");
			}
		} else {

			Coordinate[] coords = geom.getCoordinates();
			boolean first = true;
			for (Coordinate coord : coords) {
				if (!first) {
					builder.append(',');
				}
				first = false;
				builder.append(getCoord(coord));

			}
		}
		return builder.toString();
	}

	private static String getCoord(Coordinate coord) {
		StringBuilder builder = new StringBuilder();
		builder.append(coord.x).append(",").append(coord.y);
		return builder.toString();
	}
}
