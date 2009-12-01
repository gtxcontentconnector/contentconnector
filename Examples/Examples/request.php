<?

//Open the stream and read the first 10MB

$handle = fopen ("http://localhost:81/ContentRepository/ccr?filter=object.obj_type==10008%20AND%20object.obj_id==2&attributes=name&attributes=mother_obj_type&type=php", "r");
$contents = fread ($handle, 10000000);
fclose ($handle);

//Convert the stream to a PHP-Array
$cr = unserialize($contents);

//Print the array
print_r($cr);

?>