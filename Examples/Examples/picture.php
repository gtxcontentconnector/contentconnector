<?php
	$contents = implode ('', file("http://soc-dev8:81/ContentRepository/ccr?contentid='".$_REQUEST['contentid']."'&attributes=mimetype&attributes=binarycontent&type=php"));
	$cr = unserialize($contents);
	if(!$cr)
		echo "could net be deserialized...";
	if(count($cr)==0)
		echo $contents;
	
	$mime = $cr[$_REQUEST['contentid']]["attributes"]["mimetype"];
	
	header('Content-type: '+$mime);
	$data = $cr[$_REQUEST['contentid']]["attributes"]["binarycontent"];
	print($data);
?>