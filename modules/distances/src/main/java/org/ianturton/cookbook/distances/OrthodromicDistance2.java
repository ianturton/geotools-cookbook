package org.ianturton.cookbook.distances;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class OrthodromicDistance2 {
	/**
	 * take two pairs of lat/long and return bearing and distance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
		if (args.length != 4) {
			System.err.println("Need 4 numbers lat_1 lon_1 lat_2 lon_2");
			return;
		}
		GeometryFactory geomFactory = new GeometryFactory();
		Point[] points = new Point[2];
		for (int i = 0, k = 0; i < 2; i++, k += 2) {
			double x = Double.valueOf(args[k]);
			double y = Double.valueOf(args[k + 1]);
			if (CRS.getAxisOrder(crs).equals(AxisOrder.NORTH_EAST)) {
				System.out.println("working with a lat/lon crs");
				points[i] = geomFactory.createPoint(new Coordinate(x, y));
			} else {
				System.out.println("working with a lon/lat crs");
				points[i] = geomFactory.createPoint(new Coordinate(y, x));
			}

		}


		double distance = 0.0;

		GeodeticCalculator calc = new GeodeticCalculator(crs);
		calc.setStartingGeographicPoint(points[0].getX(), points[0].getY());
		calc.setDestinationGeographicPoint(points[1].getX(), points[1].getY());
		distance = calc.getOrthodromicDistance();
		double bearing = calc.getAzimuth();
		
		Measure<Double, Length> dist = Measure.valueOf(distance, SI.METER);
		System.out.println(dist.doubleValue(SI.KILOMETER) + " Km");
		System.out.println(dist.doubleValue(NonSI.MILE) + " miles");
		System.out.println("Bearing " + bearing + " degrees");
	}
}
