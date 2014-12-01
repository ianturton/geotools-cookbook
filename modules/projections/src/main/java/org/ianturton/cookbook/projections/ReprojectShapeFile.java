package org.ianturton.cookbook.projections;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

public class ReprojectShapeFile {

	public static void main(String[] args) throws MalformedURLException,
			IOException, NoSuchAuthorityCodeException, FactoryException {
		if (args.length < 3) {
			System.err.println("Usage: ReprojectShapeFile in.shp out.shp epsg:code");
			System.exit(3);
		}
		File in = new File(args[0]);
		File out = new File(args[1]);
		CoordinateReferenceSystem crs = CRS.decode(args[2]);

		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		FileDataStore inStore = fac.createDataStore(in.toURI().toURL());
		FileDataStore outStore = fac.createDataStore(out.toURI().toURL());

		SimpleFeatureCollection inFeatures = inStore.getFeatureSource()
				.getFeatures();

		ReprojectShapeFile reprojector = new ReprojectShapeFile();

		SimpleFeatureCollection outFeatures = reprojector
				.reproject(inFeatures, crs);
		outStore.createSchema(outFeatures.getSchema());
		Transaction transaction = new DefaultTransaction("create");
		String outName = outStore.getNames().get(0).getLocalPart();
		SimpleFeatureSource featureSource = outStore.getFeatureSource(outName);
		FeatureStore featureStore = (FeatureStore) featureSource;
		featureStore.setTransaction(transaction);
		featureStore.addFeatures(outFeatures);
		transaction.commit();
		outStore.dispose();
	}

	public SimpleFeatureCollection reproject(SimpleFeatureCollection features,
			CoordinateReferenceSystem target) throws FactoryException {
		SimpleFeatureCollection projFeatures = null;

		CoordinateReferenceSystem source = features.getSchema()
				.getCoordinateReferenceSystem();

		SimpleFeatureType outSchema = rewriteSchema(features.getSchema(), target);

		boolean lenient = true; // allow for some error due to different datums
		MathTransform transform = CRS.findMathTransform(source, target, lenient);

		SimpleFeatureIterator iterator = features.features();
		List<SimpleFeature> feats = new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(outSchema);
		try {
			while (iterator.hasNext()) {
				// copy the contents of each feature and transform the geometry
				SimpleFeature feature = iterator.next();
				SimpleFeature copy = builder.buildFeature(null, feature.getAttributes()
						.toArray());
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				Geometry geometry2 = JTS.transform(geometry, transform);

				copy.setDefaultGeometry(geometry2);
				feats.add(copy);
			}

		} catch (Exception problem) {
			problem.printStackTrace();
			return null;
		} finally {
			iterator.close();

		}
		System.err.println("got " + feats.size() + " output");
		projFeatures = new ListFeatureCollection(outSchema, feats);
		return projFeatures;

	}

	/**
	 * copy the schema provided and change the geometry descriptor to reflect the
	 * new CRS.
	 *
	 * @param schema
	 * @return
	 */
	private SimpleFeatureType rewriteSchema(SimpleFeatureType schema,
			CoordinateReferenceSystem crs) {
		GeometryDescriptor geom = schema.getGeometryDescriptor();
		List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
		GeometryType geomType = null;
		List<AttributeDescriptor> attribs = new ArrayList<AttributeDescriptor>();
		for (AttributeDescriptor attrib : attributes) {
			AttributeType type = attrib.getType();
			if (type instanceof GeometryType) {
				geomType = (GeometryType) type;

			} else {
				attribs.add(attrib);
			}
		}

		GeometryTypeImpl gt = new GeometryTypeImpl(new NameImpl("the_geom"),
				geomType.getBinding(), crs, geomType.isIdentified(),
				geomType.isAbstract(), geomType.getRestrictions(), geomType.getSuper(),
				geomType.getDescription());

		GeometryDescriptor geomDesc = new GeometryDescriptorImpl(gt, new NameImpl(
				"the_geom"), geom.getMinOccurs(), geom.getMaxOccurs(),
				geom.isNillable(), geom.getDefaultValue());

		attribs.add(0, geomDesc);

		SimpleFeatureType shpType = new SimpleFeatureTypeImpl(schema.getName(),
				attribs, geomDesc, schema.isAbstract(), schema.getRestrictions(),
				schema.getSuper(), schema.getDescription());
		return shpType;
	}
}
