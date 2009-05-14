//---------------------------//
//     TREE BROWSER CODE     //
// Copyright Andrew Vos 2006 //
//---------------------------//

var parentNodeCount = 0;
var nodeCount = 0;

function loadURLtoIframe(url)
{
	document.getElementById('myIframe').src = url;
}

function initPreTreeBuild()
{
	this.nodeCount=0;
	this.parentNodeCount=0;
}


function onParentNodeImageClick(node) {
    var divNode = document.getElementById(node.name.replace("parentNodeImage","parentNodeDiv"));
	var imageNode = node;

 	if (divNode.style.display == "none"){
		imageNode.src = "minus.png";
		divNode.style.display = "";
	}
	else {
		imageNode.src = "plus.png";
		divNode.style.display = "none";
	}
}
function onParentNodeTextClick(node) {
    var divNode = document.getElementById(node.name.replace("parentNodeText","parentNodeDiv"));
    var imageNode = document.getElementsByName(node.name.replace("parentNodeText","parentNodeImage"))[0];

	if (divNode.style.display == 'none') {
		imageNode.src = "minus.png";
		divNode.style.display = '';
	}
	else {
		imageNode.src = "plus.png";
		divNode.style.display = 'none';
	}
}

function onNodeTextClick(node,url) {
    var imageNode = document.getElementsByName(node.name.replace("nodeText","nodeImage"))[0];
    setSelectedNode(imageNode);
    loadURLtoIframe(url);
}
function onNodeImageClick(node, url, target){
    var imageNode = node
    setSelectedNode(imageNode);
    loadURLtoIframe(url);
}

function setSelectedNode(imageNode){
    for (index = 0; index < this.nodeCount; index++) {
		document.getElementsByName("nodeImage" + index)[0].src = "page.png";
    }
   	imageNode.src = "pageSelected.png";
}
function expandAll(){
	for (index = 0; index < this.parentNodeCount; index++) {
		document.getElementById("parentNodeDiv" + index).style.display = "";
        document.getElementsByName("parentNodeImage" + index)[0].src = "minus.png";     
	}
}
function collapseAll(){
	for (index = 0; index < this.parentNodeCount; index++) {
		document.getElementById("parentNodeDiv" + index).style.display = "none";
        document.getElementsByName("parentNodeImage" + index)[0].src = "plus.png";     
	}
}

function startParentNode(text){
	var ret="";
	ret+='<table border="0" cellpadding="1" cellspacing="0">';
	ret+='  <tr>';
	ret+='    <td><img src="plus.png" name="parentNodeImage' + parentNodeCount + '" onclick="onParentNodeImageClick(this)" style="cursor:pointer;"/></td>';
	ret+='    <td><a class="parentTreeNode" name="parentNodeText' + parentNodeCount + '" onclick="onParentNodeTextClick(this)" style="cursor:pointer;">'+text+'</a></td>';
	ret+='  </tr>';
	ret+='  <tr>';
	ret+='    <td></td><!-- SPACING -->';
	ret+='	<td><DIV id="parentNodeDiv' + parentNodeCount + '" style="display:none">';	
    this.parentNodeCount = this.parentNodeCount + 1;
    return(ret);
}
function endParentNode(){
	var ret="";
	ret+='</DIV></td>';
	ret+='  </tr>';
	ret+='</table>';
	return(ret);
}
function addNode(text, url, target){
	var ret="";
	ret+='<table border="0" cellpadding="1" cellspacing="0">';
	ret+='  <tr>';
    ret+='    <td><a href="#" onfocus="this.hideFocus=true;" style="outline-style:none;"><img src="page.png" border="0" name="nodeImage' + this.nodeCount + '" onclick="onNodeImageClick(this,\''+url+'\');" /></a></td>';
	ret+='    <td><a name="nodeText' + this.nodeCount + '" onclick="onNodeTextClick(this,\''+url+'\');" href="#" class="normalTreeNode" onfocus="this.hideFocus=true;" style="outline-style:none;">' + text + '</a></td>';
	ret+='  </tr>';
	ret+='</table>';
    this.nodeCount = this.nodeCount + 1;
    return(ret);
}
function addExpandCollapseAll(){
	var ret="";
	ret+='<table width="100%" border="0">';
	ret+='  <tr>';
	ret+='    <td align="right" width="50%"><a onclick="expandAll();" class="expandCollapse" style="cursor:pointer;">Expand All</a></td>';
	ret+='    <td alight="left" width="50%"><a onclick="collapseAll();" class="expandCollapse" style="cursor:pointer;">Collapse All</a></td>';
	ret+='  </tr>';
	ret+='</table>';
	
	return(ret);  
}

function appendTree(tree)
{
	$("#nav").append(tree);
}


