package org.ianturton.cookbook.filters;

import java.io.File;
import java.io.IOException;

import javax.measure.unit.Unit;
import javax.media.jai.Histogram;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.coverageio.gdal.aig.AIGReader;
import org.geotools.data.DataSourceException;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;

/**
 * find all the cells in a grid coverage that are a set value. based on
 * http://stackoverflow
 * .com/questions/27761590/geotools-total-area-where-gridcoverage-has-value-x
 *
 * @author ian.turton
 *
 */
public class QueryGrid {

	public static void main(String[] args) {
		// load a file

		// File raster = new File("../../data/TQ19.asc");
		File raster = new File("../../data/nzdem/nzdem500/hdr.adf");

		AbstractGridFormat format = GridFormatFinder.findFormat(raster);
		AbstractGridCoverage2DReader reader = null;
		try {
			reader = format.getReader(raster);
		} catch (Exception e) {
			System.err.println("Failed to find a reader for " + raster);
			e.printStackTrace();
			// return;
		}

		try {
			reader = new AIGReader(raster);
		} catch (DataSourceException e) {
			e.printStackTrace();
		}

		GridCoverage2D cov;
		try {
			cov = reader.read(null);
		} catch (IOException giveUp) {
			throw new RuntimeException(giveUp);
		}

		QueryGrid queryGrid = new QueryGrid();
		queryGrid.selectCells(cov, 135);
		queryGrid.selectCells(cov, 0);
		queryGrid.selectCells(cov, 134);

	}

	private double selectCells(GridCoverage2D cov, int value) {
		GridGeometry2D geom = cov.getGridGeometry();
		// cov.show();
		final OperationJAI op = new OperationJAI("Histogram");
		ParameterValueGroup params = op.getParameters();
		GridCoverage2D coverage;
		params.parameter("Source").setValue(cov);
		coverage = (GridCoverage2D) op.doOperation(params, null);
		javax.media.jai.Histogram hist = (Histogram) coverage
				.getProperty("histogram");

		int total = hist.getSubTotal(0, value, value);
		double area = calcAreaOfCell(geom);
		Unit<?> unit = cov.getCoordinateReferenceSystem().getCoordinateSystem()
				.getAxis(0).getUnit();
		System.out.println("which gives " + (area * total) + " " + unit
				+ "^2 area with value " + value);
		return area * total;
	}

	private double calcAreaOfCell(GridGeometry2D geom) {
		GridEnvelope gridRange = geom.getGridRange();
		int width = gridRange.getHigh(0) - gridRange.getLow(0) + 1; // allow for the
		int height = gridRange.getHigh(1) - gridRange.getLow(1) + 1;// 0th row/col
		Envelope envelope = geom.getEnvelope();
		double dWidth = envelope.getMaximum(0) - envelope.getMinimum(0);
		double dHeight = envelope.getMaximum(1) - envelope.getMinimum(1);
		double cellWidth = dWidth / width;
		double cellHeight = dHeight / height;

		return cellWidth * cellHeight;
	}
}
