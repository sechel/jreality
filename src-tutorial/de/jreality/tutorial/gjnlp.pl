#!/usr/bin/perl
$jrealitybase="/net/www3/pub/jreality/";
$codebase="http://www3.math.tu-berlin.de/jreality/download/webstart/jnlp/";
$outdir=$jrealitybase."download/webstart/jnlp/";
#$dirname = $jrealitybase."download/src-tutorial/de/jreality/tutorial/";
#print $dirname,"\n";
#opendir( ALLDIR, $dirname) || die "Error in opening $dirname\n";
#while (($dir = readdir(ALLDIR))) {
while ( $dir = <*>) {
    if (-d $dir) {
        print $dir,"\n";
        opendir(DIR, $dirname.$dir);
        @files = readdir(DIR);
        foreach $file (@files)  {
            $end = ".java";
            if ($file =~ /.*$end\b/) {
                $file =~ s/$end//;
                open(IN, "Template.jnlp");
                $outfile = $outdir.$file.".jnlp";
                print "\t",$outfile,"\n";
                open(OUT, ">$outfile");
                while (<IN>)    {
                    s/CODEBASE/$codebase/;
                    s/CLASSNAME/$file/;
                    s/PACKAGE/$dir/;
                    print OUT $_;
                }
                close(IN);
                close(OUT);
            }
        }
    }
}
