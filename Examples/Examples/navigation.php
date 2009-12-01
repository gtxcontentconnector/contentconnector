
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title>RESTNavigation - PHP Example</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
</head>
<body>
<?


$REST_NAV_PATH="http://localhost:42880/CCServlets/nav";
$REST_BIN_PATH="http://localhost:42880/CCServlets/bin";

function process_element($children)
{
	echo "<ul>";
	while($child = current($children))
	{
		if(is_array($child))
		{
			if(count($child["attributes"])>0)
			{
				if($child["attributes"]["name"]!="")
				{
					
					echo "<li>";
					echo "<a href=\"".$_SERVER['PHP_SELF']."?contentid=".$child["contentid"]."&amp;action=getcontent\">";
					print($child["attributes"]["name"]);
					echo "</a></li>";
					
				}
			}
			if(count($child["children"])>0)
			{
				process_element($child["children"]);
				
			}
		}
		next($children);
	}
	echo "</ul>";
	
}


?>
<table border="0" width="100%">
<tr><td>
<?
     
$contents = implode ('', file($REST_NAV_PATH."?contentid=10002.4&childfilter=object.obj_type==10002&attributes=name&type=php"));

//Convert the stream to a PHP-Array
$cr = unserialize($contents);
if(!$cr)
	echo "could net be deserialized...";
if(count($cr)==0)
	echo $contents;

process_element($cr);


?>
</td><td>
<?

if($_REQUEST['action']=="getcontent")
{
	$request_url=$REST_NAV_PATH."?contentid=".$_REQUEST['contentid']."&childfilter=object.obj_type!=10002&attributes=name&attributes=mimetype&type=php";

	$contents = implode ('', file($request_url));
	$cr = unserialize($contents);
	if(!$cr)
		echo "could net be deserialized...";
	if(count($cr)==0)
		echo $contents;

	$children = $cr[$_REQUEST['contentid']]["children"];
	while($child = current($children))
	{
		if(is_array($child)&& count($child["attributes"])>0)
		{
			if($child["attributes"]["name"]!="")
			{
				echo "[".$child["obj_type"]."]<a href=\"".$_SERVER['PHP_SELF']."?contentid=".$child["contentid"]."&amp;contenttype=".$child["attributes"]["mimetype"]."&amp;filename=".$child["attributes"]["name"]."&amp;action=show\">";
				print ($child["attributes"]["name"]);
				echo "</a><br>";
			}
		}
		next($children);
	}
}
 ?>
</td><td>
<?
if($_REQUEST['action']=="show")
{
	
	$arr = explode("/",$_REQUEST['contenttype']);
	if($arr[0]=="image")
	{
		echo "<img src=\"".$REST_BIN_PATH."?contentid=".$_REQUEST['contentid']."\">";
	}
	else
	{
		echo "<iframe src=\"".$REST_BIN_PATH."?contentid=".$_REQUEST['contentid']."&contentdisposition=".$_REQUEST['filename']."\" name=\"content\" width=\"800\" height=\"600\"><p>Sorry, Iframe did not work.</p></iframe>";
	}
}
?>
</td></tr></table></body></html>