(*******************************************************************
This file was generated automatically by the Mathematica front end.
It contains Initialization cells from a Notebook file, which
typically will have the same name as this file except ending in
".nb" instead of ".m".

This file is intended to be loaded into the Mathematica kernel using
the package loading commands Get or Needs.  Doing so is equivalent
to using the Evaluate Initialization Cells menu command in the front
end.

DO NOT EDIT THIS FILE.  This entire file is regenerated
automatically each time the parent Notebook file is saved in the
Mathematica front end.  Any changes you make to this file will be
overwritten.
***********************************************************************)

torus[r_, R_, n_, m_] := Block[{du = 2 Pi / n, dv = 2 Pi/m},Table[
{Cos[u]*(R+r Sin[v]), r*Cos[v],Sin[u] * (R+r Sin[v])}, {u, 0, 2 Pi, du}, {v,0,
        2  Pi, dv}]]
