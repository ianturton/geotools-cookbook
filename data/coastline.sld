<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>Meridian&#8482; 2</Name>
    <UserStyle>
      <Title>Coastline</Title>
      <Abstract>Meridian&#8482; 2. Ordnance Survey. &#169; Crown copyright and database rights 2013.</Abstract>
      
      <!--  Coastline  -->
      
      <FeatureTypeStyle>
        <Rule>
          <Name>Coastline - 1:23,607 To 1:94,482</Name>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>CODE</ogc:PropertyName>
              <ogc:Literal>6200</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <MinScaleDenominator>23607</MinScaleDenominator>
          <MaxScaleDenominator>94482</MaxScaleDenominator>
          <LineSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#5DC6DE</CssParameter>
              <CssParameter name="stroke-width">0.75</CssParameter>    
            </Stroke>
          </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
      
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>