surface
constantTexture ( string texturename = "")
{
        Oi = Os;
        color Ct;
        if (texturename != "")    {
            Ct = color texture (texturename);
            Ci = Ct * Cs;
        }
        else Ci = Cs;
}

