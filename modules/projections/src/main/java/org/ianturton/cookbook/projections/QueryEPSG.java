package org.ianturton.cookbook.projections;

import java.util.ArrayList;
import java.util.Set;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class QueryEPSG {

	public static void main(String[] args) {
		QueryEPSG q = new QueryEPSG();
		/*
		 * ArrayList<String> list = q.listDb(); // Collections.sort(list); for
		 * (String desc : list) { System.out.println(desc); }
		 */
		/*
		 * ArrayList<CoordinateReferenceSystem> crss = q.listCRS(); for
		 * (CoordinateReferenceSystem crs : crss) { ReferenceIdentifier name =
		 * crs.getName(); if (name != null) { Citation authority =
		 * name.getAuthority(); if (authority != null) {
		 * System.out.print(authority.getTitle() + " "); }
		 * System.out.println(name.getCode() + " (" + name.getCodeSpace() + ")"); }
		 * }
		 */
		ArrayList<CoordinateReferenceSystem> crss = q.selectCRS("GB");
		for (CoordinateReferenceSystem crs : crss) {
			System.out.println(crs.toWKT());
			System.out
					.println("----------------------------------------------------------------------------");
		}
	}

	/**
	 * search the CRS database and return any which include the string provided.
	 *
	 * @param value
	 *          - a string to search for.
	 * @return an ArrayList of CRS that match (may be empty, but not null).
	 */
	private ArrayList<CoordinateReferenceSystem> selectCRS(String value) {
		String lValue = value.toLowerCase();
		ArrayList<CoordinateReferenceSystem> ret = new ArrayList<CoordinateReferenceSystem>();
		ArrayList<CoordinateReferenceSystem> possibles = listCRS();
		CRSAuthorityFactory factory = CRS.getAuthorityFactory(false);

		for (CoordinateReferenceSystem crs : possibles) {
			if (crs.getName().getCode().toLowerCase().contains(lValue)) {
				ret.add(crs);
			} else if (crs.getName().getCodeSpace() != null
					&& crs.getName().getCodeSpace().toLowerCase().contains(lValue)) {
				ret.add(crs);
			} else if (crs.getRemarks() != null
					&& crs.getRemarks().toString().contains(lValue)) {
				ret.add(crs);
			} else if (crs.getCoordinateSystem().getRemarks() != null
					&& crs.getCoordinateSystem().getRemarks().toString().toLowerCase()
							.contains(lValue)) {
				ret.add(crs);
			}

		}
		return ret;
	}

	/**
	 * query the list of authorities and select CRS that are supported.
	 *
	 * @return a list of CRS.
	 */
	private ArrayList<CoordinateReferenceSystem> listCRS() {

		ArrayList<CoordinateReferenceSystem> ret = new ArrayList<CoordinateReferenceSystem>();
		Set<String> authorities = CRS.getSupportedAuthorities(false);
		for (String authority : authorities) {
			System.out.println("Looking up " + authority);
			Set<String> codes = CRS.getSupportedCodes(authority);
			for (String code : codes) {
				try {
					if (!code.contains(":")) {
						code = authority + ":" + code;
					}
					ret.add(CRS.decode(code));
				} catch (NoSuchAuthorityCodeException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				} catch (FactoryException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 * ask the Authority Factory to provide a list of CRS codes and Descriptions.
	 *
	 * @return a list of strings, code\tdescription.
	 */
	private ArrayList<String> listDb() {
		ArrayList<String> ret = new ArrayList<String>();
		CRSAuthorityFactory authFac = CRS.getAuthorityFactory(false);
		try {

			Set<String> list = authFac
					.getAuthorityCodes(CoordinateReferenceSystem.class);

			for (String code : list) {
				ret.add(code + "\t" + authFac.getDescriptionText(code));
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;

	}

}
