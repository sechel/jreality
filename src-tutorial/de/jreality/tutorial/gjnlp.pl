#!/usr/bin/perl

#This perl script generates .jnlp files for all classes defined in the subdirectories
#of the directory $srcdir (neither checks whether they have a main method nor 
#decends further into subsubdirectories). Make sure that $srcdir is up to date.
#The .jnlpfiles or written to $outdir .
#
#Make sure that all files referenced in the template jnlp
#file ($template) are up to date and signed with the same key. The purpose of the
#generated .jnlp files is to allow web start launch of the examples from the developer 
#turtorial in the jreality wiki. 

$jrealitybase="/net/www3/pub/jreality/";
$codebase="http://www3.math.tu-berlin.de/jreality/tutorial/jnlp/";
$outdir=$jrealitybase."tutorial/jnlp/";
$srcdir = $jrealitybase."tutorial/jr/src/de/jreality/tutorial/";
$template=$srcdir."Template.jnlp";

print $srcdir,"\n";
opendir(SRCDIR,$srcdir) || die "error: can't open $srcdir\n";

while ($dir = readdir(SRCDIR)) {
    if (-d $srcdir.$dir) {
        print $dir,"\n";
        
        opendir(DIR, $srcdir.$dir);
        @files = readdir(DIR);
        closedir(DIR);
        
        foreach $file (@files)  {
            $end = "\.java";
            if ($file =~ /.*$end$/) {
                $file =~ s/$end//;
                open(IN, "<$template") || die "error: can't read from $template";
                $outfile = $outdir.$file.".jnlp";
                print "\t",$outfile,"\n";
                open(OUT, ">$outfile") || die "error: can't write to $outfile";
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

closedir(SRCDIR);

