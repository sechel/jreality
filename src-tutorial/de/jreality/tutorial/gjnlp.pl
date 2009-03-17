#!/usr/bin/perl
$codebase="http://www3.math.tu-berlin.de/jreality/download/webstart/";
while ($dir = <*>) {
    if (-d $dir) {
        print $dir,"\n";
        opendir(DIR, $dir);
        @files = readdir(DIR);
        foreach $file (@files)  {
            $end = ".java";
            if ($file =~ /.*$end\b/) {
                $file =~ s/$end//;
                open(IN, "Template.jnlp");
                $outfile = $file.".jnlp";
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
