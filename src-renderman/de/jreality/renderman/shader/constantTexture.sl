surface
constantTexture ( string texturename = "")
{
        color Ct;
        float tr;
        if (texturename != "")    {
	        tr = float texture(texturename[3], "fill",1);
            Ct = color texture (texturename);
            Ci = Ct * Cs;
            Oi = tr*Os;
        }
        else {
            Ci = Cs;
            Oi = Os;
        }
}

