package org.ianturton.cookbook.styling;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import org.geotools.styling.Font;
import org.geotools.styling.FontImpl;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;

public class FontManagement {
	public String[] listFonts() {

		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		return fonts;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FontManagement f = new FontManagement();
		String[] fonts = f.listFonts();
		for(String font:fonts){
			System.out.println(font);
		}
		StyleBuilder builder = new StyleBuilder();
		String attributeName="";
		Font font = builder.createFont(fonts[0], 10.0);
		TextSymbolizer textSymb = builder.createTextSymbolizer(Color.black, font , attributeName);
		System.out.println(textSymb.getFont().getFamily());
		
		Rule rule = builder.createRule(textSymb);
		
	}

}
