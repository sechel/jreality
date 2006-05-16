Graphics3D[
 {
	{
	 SurfaceColor[RGBColor[0.5,.5,1]],
	 Cuboid[{0,0,0},{0.2,0.2,0.2}]
	},
	{
	  RGBColor[1, 0, 0], 
	 Point[{1,1,0}],
	  RGBColor[1, 1, 0],
	 Point[{1,0,1}],
	  RGBColor[0, 1, 0], 
	 Point[{0,1,1}],
	  RGBColor[0, 1, 1], 
	 AbsolutePointSize[3],
	 Point[{1,1,1}],
	  RGBColor[0, 1, 0], 	 
	  
	 Line[{{0,0,0},{3,0,0}}],
	  RGBColor[0, 0, 0], 
	 Line[{{0,0,0},{0,3,0}}],
	  RGBColor[1, 0, 0], 
	 Line[{{0,0,0},{0,0,3}}],
	 
	 
	 
	 
	  AbsoluteThickness[3],
    
	 Line[{{2,0,2},{2,2,0},{0,2,2}}],
	 
	  RGBColor[1, 0, 0],
     Point[{2,2,2}],
	 
	 Line[{{2,2,0},{1.3,1.3,1.3},{2,0,2}}],
	  RGBColor[1, 0, 0],
	 Line[{{2,2,2},{3,3,3}}],
	 Line[{{1.3,1.3,1.3},{0,2,2}}]
	
	},
	{Text["Hallo",{4,4,4}]
	},
	{	
	  SurfaceColor[RGBColor[1, 0, 0]], 
	 Polygon[{{0,0,1},{0,1,0},{1,0,0}}],
	  SurfaceColor[RGBColor[1, 1, 0]], 
	 Polygon[{{0,0,1.1},{0,1.1,0},{0,1.8,0},{0,0,1.8}}],
	  SurfaceColor[RGBColor[0, 1, 0]], 
	 Polygon[{{0,1.1,0},{1.1,0,0},{1.8,0,0},{0,1.8,0}}],
 	  SurfaceColor[RGBColor[0, 1, 1]], 
	 Polygon[{{0,0,1.1},{1.1,0,0},{1.8,0,0},{0,0,1.8}}]
	}
  }
  
  
  
  ,{
  PlotRange -> Automatic,
  DisplayFunction -> (Display[$Display, #1] & ), 
  ColorOutput -> Automatic, 
  Axes -> True, 
  
  PlotLabel -> None, 
  AxesLabel -> None, 
  Ticks -> Automatic, 
  Prolog -> {}, 
  Epilog -> {}, 
  AxesStyle -> Automatic, 
  Background -> Automatic, 
  DefaultColor -> RGBColor[1,0,0], 
  DefaultFont -> {"Courier", 10.}, 
  AspectRatio -> Automatic, 
  ViewPoint -> {1.3, -2.4, 2.}, 
  Boxed -> True, 
  
  BoxRatios -> {1, 1, 0.4}, 
  Plot3Matrix -> Automatic, 
  Lighting -> True, 
  AmbientLight -> GrayLevel[0], 
  LightSources -> {{{1., 0., 1.},   RGBColor[1, 0, 0]}, {{1., 1., 1.},     RGBColor[0, 1, 0]}, {{0., 1., 1.}, RGBColor[0, 0, 1]}}, 
  ViewCenter -> Automatic, 
  PlotRegion -> Automatic, 
  ImageSize -> Automatic, 
  TextStyle -> {}, 
  FormatType -> StandardForm, 
  ViewVertical -> {0., 0., 1.}, 
  FaceGrids -> None, 
  Shading -> True, 
  AxesEdge -> Automatic, 
  BoxStyle -> Automatic, 
  SphericalRegion -> False}]

