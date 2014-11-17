package org.ianturton.cookbook.distances;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class OrthodromicDistance {

	DefaultGeographicCRS default_crs = DefaultGeographicCRS.WGS84;

	/**
	 * take two pairs of lat/long and return bearing and distance.
	 *
	 * @param args
	 * @throws FactoryException
	 * @throws NoSuchAuthorityCodeException
	 */
	public static void main(String[] args)
			throws NoSuchAuthorityCodeException, FactoryException {
		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		if (args.length < 4) {
			System.err
			.println("Need 4 numbers lat_1 lon_1 lat_2 lon_2 [epsgcode]");
			return;
		}
		if (args.length > 4) {
			crs = CRS.decode(args[4]);
		}
		GeometryFactory geomFactory = new GeometryFactory();
		Point[] points = new Point[2];
		for (int i = 0, k = 0; i < 2; i++, k += 2) {
			double x = Double.valueOf(args[k]);
			double y = Double.valueOf(args[k + 1]);
			if (CRS.getAxisOrder(crs)
					.equals(AxisOrder.NORTH_EAST)) {
				points[i] = geomFactory.createPoint(new Coordinate(
						x, y));
			} else {
				points[i] = geomFactory.createPoint(new Coordinate(
						y, x));
			}

		}

		OrthodromicDistance d = new OrthodromicDistance();
		d.calculateDistance(crs, points);
	}

	private void calculateDistance(
			CoordinateReferenceSystem crs, Point[] points) {
		if (crs == null) {
			crs = default_crs;
		}
		double distance = 0.0;
		try {
			distance = JTS.orthodromicDistance(
					points[0].getCoordinate(),
					points[1].getCoordinate(), crs);
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Measure<Double, Length> dist = Measure.valueOf(
				distance, SI.METER);
		System.out.println(dist.doubleValue(SI.KILOMETER)
				+ " Km");
		System.out.println(dist.doubleValue(NonSI.MILE)
				+ " miles");
	}
}
