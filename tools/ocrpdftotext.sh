#!/bin/bash
#ocrpdftotext
# Simplified implementation of http://ubuntuforums.org/showthread.php?t=880471

# Might consider doing something with getopts here, see http://wiki.bash-hackers.org/howto/getopts_tutorial
DPI=600
TESS_LANG=ita
STARTPAGE=1 # set to pagenumber of the first page of PDF you wish to convert
ENDPAGE=3 # set to pagenumber of the last page of PDF you wish to convert

FILENAME=${@%.pdf}
SCRIPT_NAME=`basename "$0" .sh`
TMP_DIR=${SCRIPT_NAME}-tmp
OUTPUT_FILENAME=${FILENAME}-output@DPI${DPI}

mkdir ${TMP_DIR}
cp ${@} ${TMP_DIR}
cd ${TMP_DIR}


for i in `seq $STARTPAGE $ENDPAGE`; do
    convert -monochrome -depth 8 -density $DPI ${@}\[$(($i - 1 ))\] page.tif
    echo processing page $i
    tesseract page.tif tempoutput #-l ${TESS_LANG}
    cat tempoutput.txt >> ${OUTPUT_FILENAME}.txt
done

mv ${OUTPUT_FILENAME}.txt ..
rm *
cd ..
rmdir ${TMP_DIR}



