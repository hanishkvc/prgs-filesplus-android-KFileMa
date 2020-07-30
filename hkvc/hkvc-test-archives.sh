

setup_tree() {

	BPATH=$1
	read -p "Clear and ReCreate $BPATH, ..."
	rm -rf $BPATH
	mkdir --parents $BPATH
	for iD in "01" "2" "03" "4" "5"; do
		d=$BPATH/dir$iD
		mkdir $d
		for iF in "1" "2" "03" "4"; do
			touch $d/file_$iD\_$iF
		done
	done	

}

compress_archive() {
	sType=$1
	sCmd=$2

	echo $sType > $THEPATH/yo_$sType
	tar -cvf /tmp/$THENAME\_$sType.tar $THEPATH/
	$sCmd /tmp/$THENAME\_$sType.tar
	rm $THEPATH/yo_$sType
}

THENAME=testdir
THEPATH=/tmp/$THENAME
setup_tree $THEPATH

echo "tar" > $THEPATH/yo_tar
tar -cvf /tmp/$THENAME.tar $THEPATH/
rm $THEPATH/yo_tar

compress_archive targz gzip
compress_archive tarbz bzip2
compress_archive tarxz xz

echo "zip" >  $THEPATH/yo_zip
zip -r /tmp/$THENAME.zip $THEPATH/
rm $THEPATH/yo_zip

echo "7z" > $THEPATH/yo_7z
7z a -r /tmp/$THENAME.7z $THEPATH/*
rm $THEPATH/yo_7z

