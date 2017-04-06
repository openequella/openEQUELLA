#
# Capture the output of your Java program when running with the following option:
#
#    -verbose:class
#
# Run this using AWK in the following manner:
#
#    awk -f gather_loaded_classes.awk file_with_java_output
#
# Save the output and use with the "minimal-jar" ant macro to create minimal jars.
#

/^\[Loaded org\.(aopalliance|springframework)\./ {
    gsub(/\./, "/", $2)
    print $2 ".class"
}