/**
 * 
 */

var $checkableTree = null;
var componentMap = new Map();
// init
jQuery.ajax({
    url: "/projects", // 请求的URL
    dataType: 'json',
    async: false,
    timeout: 50000,
    cache: false,
    success: function (response) {
        $checkableTree = $('#treeview-checkable').treeview({
            data: response,
            showIcon: false,
            levels: 1,
            showCheckbox: true
        });
        var compArr = $checkableTree.treeview('getUnchecked').filter(
    			function (node) { return node.projectName == null; });
        compArr.forEach(
        		function(node){componentMap.set(node.componentId, node.nodeId)});
    },
    error: function (XMLHttpRequest, textStatus, errorThrown) {
        alert(XMLHttpRequest.responseJSON.message);
        window.location="/login";
    }
});

// Check / Uncheck all
$('#btn-check-all').on('click', function (e) {
    $checkableTree.treeview('checkAll', {
        silent: true
    });
});

$('#btn-uncheck-all').on('click', function (e) {
    $checkableTree.treeview('uncheckAll', {
        silent: true
    });
});

// find all the checked component nodes for export
var checkedNodes = function () {
    return $('#treeview-checkable').treeview('getChecked').filter(
			function (node) { return node.projectName == null; });
};

//Process step 1, verify main ticket
var verifyMain = function () {
    var mainTicketId = $("#input-main-ticket")[0].value;
    if (mainTicketId == null || mainTicketId.trim().length < 1) {
        throw "Please input main ticket!";
    }
    var valid = null;
    jQuery.ajax({
        type: 'GET',
        url: "/getissue", // 请求的URL
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        data: { "issue_id": mainTicketId.trim() },
        async: false,
        timeout: 50000,
        cache: false,
        success: function (response) {
            mainTicket.issueKey = response.issueKey;
            mainTicket.projectKey = response.projectKey;
            mainTicket.componentId = response.componentId;
            mainTicket.description = response.description;
            mainTicket.fixVersionId = response.fixVersionId;
            mainTicket.fixVersionName = response.fixVersionName;
            mainTicket.customfield_10002Value = response.customfield_10002Value;
            mainTicket.summary = response.summary;
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        	valid = XMLHttpRequest;
        }
    });
    return valid;
};

// get version Id for project
var versionId4Project = function (nodeId, versionName) {
	var parentNode = $("#treeview-checkable").treeview("getParent", nodeId);
	if(parentNode.fixVersionId){
		return parentNode.fixVersionId;
	}
	if(parentNode.hasVersion != undefined &&
			parentNode.hasVersion == false){
		return null;
	}
	if(!parentNode.fixVersionId){
		jQuery.ajax({
	        type: 'GET',
	        url: "/getversionid", // 请求的URL
	        data: { "projectkey": parentNode.text,
	        	"versionname": mainTicket.fixVersionName},
	        contentType: "application/json; charset=utf-8",
	        async: false,
	        timeout: 50000,
	        cache: false,
	        success: function (response) {
	        	if(response==null || response.trim().length<1){
	        		parentNode.hasVersion = false;
	        		processModalContent(
	        				"Cannot find version for project: " + parentNode.text, true);
	        	}
	        		
	        	else{
	        		parentNode.fixVersionId = response;
	        		parentNode.hasVersion = true;
	        	}
	        }
	    });
	}
	return parentNode.fixVersionId;
}

// Node value to JSON for create
var checkedNodes2Json = function () {
    var validNode = checkedNodes();
    if (validNode == null || validNode.length == 0) {
        throw "No component choose!";
    }
    var compArr = [];
    validNode.forEach(
        function (node) {
        	var ver = versionId4Project(node.nodeId, mainTicket.fixVersionName);
        	if(ver==null || ver.trim().length<1){
        		return;
        	}
            var nObj = {};
            nObj.projectKey = $checkableTree.treeview("getParent", node.nodeId).text;
            nObj.componentId = node.componentId;
            nObj.componentName = node.text;
            
            //fixVersionId
            if (node.fixVersionId == null || node.fixVersionId.trim().length < 1)
            	nObj.fixVersionId = ver;
            else
            	nObj.fixVersionId = node.fixVersionId;
            
            //customfield_10002Value
            if (node.customfield_10002Value == null || node.customfield_10002Value.trim().length < 1)
                nObj.customfield_10002Value = "3-Medium";
            else
            	nObj.customfield_10002Value = node.customfield_10002Value;
            
            //issuetype always 167
            nObj.issuetype = "167";
            
            //summary
            if (node.summary == null || node.summary.trim().length < 1)
            	nObj.summary = mainTicket.summary;
            else
            	nObj.summary = node.summary;
            
            //description
            if (node.description == null || node.description.trim().length < 1)
            	nObj.description = "Please refer to main ticket.";
            else 
            	nObj.description = node.description;
            compArr.push(nObj);
        }
    )
    return compArr;
};

//Process step 2, create tasks
var createTasks = function () {
    var j = JSON.stringify(checkedNodes2Json());
    var taskList = [];
    jQuery.ajax({
        type: 'POST',
        url: "/createtasks", // 请求的URL
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        data: j,
        async: false,
        timeout: 50000,
        cache: false,
        success: function (response) {
        	$('#myModalContent')[0].innerHTML = "";
            taskList = response;
            taskList.forEach(
                function (node) {
                    if(node.startsWith("ERROR") && node.endsWith("ERROR")){
                    	processModalContent(
    	        				node.substring(6, node.length-6) + " XPI ticket creation failed.", true);
                    } else {
                    	processModalContent(
    	        				"New ticket " + node, false);
                    }
                }
            );
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            throw "XPI ticket creation failed";
        }
    });
    return taskList;
};

// Process step 3, create issue link
var createIssueLinks = function (mainTicketId, taskList) {
	var failedIssueLinkList = [];
    jQuery.ajax({
        type: 'GET',
        url: "/createissuelink", // 请求的URL
        contentType: "application/json; charset=utf-8",
        data: {"inward": mainTicketId,
        	"outwards": taskList},
        async: false,
        timeout: 50000,
        cache: false,
        success: function (response) {
        	failedIssueLinkList = response;
        	failedIssueLinkList.forEach(
                function (node) {
                	processModalContent(
	        				node + " xpi link creation failed", true);
                }
            );
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            throw "xpi link creation failed";
        }
    });
};

//Process
var mainTicket = {};
$('#btn-process').on('click', function (e) {
	report = [];
	$('#myModalProgress').show();
	$('#myModalClose').hide();
	$('#myModalLabel').text("Processing");
	$('#myModalContent')[0].innerHTML = "The progress may take several minutes. Please be patient.";
	$('#myModalDownload').hide();
	$("#myModal").modal('show');
    setTimeout(function(){
	    try {
	    	var mainTicket = verifyMain()
	        if(mainTicket!=null){
	        	var meg = mainTicket.responseJSON.message.toLowerCase().trim();
	        	if(meg == "no fixversions")
	        		throw "Lack of fix versions in main ticket!";
	        	else if(meg == "session timeout")
	        		throw "Session timeout, please re-login!";
	        	else 
	        		throw "Main ticket error";
	        }
	        var taskList = createTasks();
	
	        if (!$.isEmptyObject(taskList)){ 
	            	createIssueLinks(mainTicket.issueKey,taskList.join(','));
	        }
	        else
	        	throw "None ticket created!";
	        
	        $('#myModalProgress').hide();
	        if(report.length < 1)
	        	$('#myModalContent')[0].innerHTML = "";
	        else
	            $('#myModalDownload').show();
	        $('#myModalClose').show();
	        $('#myModalLabel').text("Complete");
	        
	    } catch (err) {
	    	$('#myModalProgress').hide();
	        $('#myModalClose').show();
	        $('#myModalLabel').text("Error");
	        if(report.length < 1)
	        	$('#myModalContent')[0].innerHTML = "";
	        else
	            $('#myModalDownload').show();
	        var errP = "<p style='color:red'>" + err + "</p>";
	        $('#myModalContent')[0].innerHTML += errP;
	    }
    },1000);
});

var report = [];
var processModalContent = function (log, errorFlag) {
	report.push(log);
	if(errorFlag)
		$('#myModalContent')[0].innerHTML += "<p style='color:red'>" + log + "</p>";
	else
		$('#myModalContent')[0].innerHTML += "<p>" + log + "</p>";
}

//import csv
$("#btn-import").on('click', function (e) {
    var fileName = $('#file_input').val();
    if (fileName === '') {
        alert('Please choose CSV file');
        return false;
    }
    var fileType = (fileName.substring(fileName
            .lastIndexOf(".") + 1, fileName.length))
            .toLowerCase();
    if (fileType !== 'csv') {
        alert('Please add CSV file');
        return false;
    }

    var fileImport = $("#file_input")[0].files[0];

    var reader = new FileReader();

    Papa.parse(fileImport, {
        complete: function(results) {
            var csvData = results.data;
            var key = csvData[0];
            for(var i=1; i<csvData.length; i++){
            	var nObj = csvData[i];
            	var nodeId = componentMap.get(nObj[2]);
            	$checkableTree.treeview('checkNode', nodeId);
            	var node = $checkableTree.treeview('getNode', nodeId);
            	var pNode = $checkableTree.treeview('getParent', nodeId);
            	if(!pNode.state.expanded){
            		$checkableTree.treeview('expandNode', [ pNode.nodeId, { silent: true, ignoreChildren: true } ]);
            	}
            	node.projectKey = nObj[0];
            	node.componentName = nObj[1];
            	node.fixVersionId = nObj[3];
            	node.customfield_10002Value = nObj[4];
            	node.summary = nObj[6];
            	node.description = nObj[7];
            }  
            alert("Import file " + $('#file_input')[0].files[0].name + " succeed!");
            $('#file_input').val(null);
        }
    });
    
});

//csv data
var checkedNodes2Csv = function () {
    var validNode = checkedNodes();
    if (validNode == null || validNode.length == 0) {
        throw "No component choose!";
    }
    var compArr = [];
    validNode.forEach(
        function (node) {
            var nObj = {};
            nObj['project'] = $checkableTree.treeview("getParent", node.nodeId).text;
            nObj['component id'] = node.componentId;
            nObj['component name'] = node.text;
            nObj['description'] = node.description;
            nObj['fix version'] = node.fixVersionId;
            nObj['ticket priority'] = node.customfield_10002Value;
            nObj['issuetype'] = node.issuetype;
            nObj['summary'] = node.summary;
            compArr.push(nObj);
        }
    )
    return compArr;
};

//csv header
var fields = [
    'project',
    'component name',
    'component id',
    'fix version',
    'ticket priority',
    'issuetype',
    'summary',
    'description'
];

//export csv
var funDownload = function (content, filename) {
    var eleLink = document.createElement('a');
    eleLink.download = filename;
    eleLink.style.display = 'none';
    var blob = new Blob([content]);
    eleLink.href = URL.createObjectURL(blob);
    document.body.appendChild(eleLink);
    eleLink.click();
    document.body.removeChild(eleLink);
};

$("#btn-export").on('click', function (e) {
    var csv = json2csv({ data: checkedNodes2Csv(), fields: fields });
    funDownload(csv, 'test.csv');
});

$("#myModalDownload").on('click', function (e) {
    funDownload(report.join("\r\n"), 'report.txt');
});

//on check node
$checkableTree.on('nodeChecked', function (event, node) { 
    var selectNodes = getChildNodeIdArr(node); 
    if (selectNodes) {
        $('#treeview-checkable').treeview('checkNode', [selectNodes, { silent: true }]);
    }
    var parentNode = $("#treeview-checkable").treeview("getNode", node.parentId);
    setParentNodeCheck(node);
});

//on uncheck node
$checkableTree.on('nodeUnchecked', function (event, node) { 
    var selectNodes = setChildNodeUncheck(node); 
    var childNodes = getChildNodeIdArr(node);
    if (selectNodes && selectNodes.length == 0) {
        console.log("反选");
        $('#treeview-checkable').treeview('uncheckNode', [childNodes, { silent: true }]);
    }
    var parentNode = $("#treeview-checkable").treeview("getNode", node.parentId);
    var selectNodes = getChildNodeIdArr(node);
    setParentNodeCheck(node);
});

function getChildNodeIdArr(node) {
    var ts = [];
    if (node.nodes) {
        for (x in node.nodes) {
            ts.push(node.nodes[x].nodeId);
            if (node.nodes[x].nodes) {
                var getNodeDieDai = getChildNodeIdArr(node.nodes[x]);
                for (j in getNodeDieDai) {
                    ts.push(getNodeDieDai[j]);
                }
            }
        }
    } else {
        ts.push(node.nodeId);
    }
    return ts;
}

function setParentNodeCheck(node) {
    var parentNode = $("#treeview-checkable").treeview("getNode", node.parentId);
    if (parentNode.nodes) {
        var checkedCount = 0;
        for (x in parentNode.nodes) {
            if (parentNode.nodes[x].state.checked) {
                checkedCount++;
            } else {
                break;
            }
        }
        if (checkedCount == parentNode.nodes.length) {
            $("#treeview-checkable").treeview("checkNode", parentNode.nodeId);
            setParentNodeCheck(parentNode);
        } else {
            $('#treeview-checkable').treeview('uncheckNode', parentNode.nodeId);
            setParentNodeCheck(parentNode);
        }
    }
}

function setChildNodeUncheck(node) {
    if (node.nodes) {
        var ts = []; 
        for (x in node.nodes) {
            if (!node.nodes[x].state.checked) {
                ts.push(node.nodes[x].nodeId);
            }
            if (node.nodes[x].nodes) {
                var getNodeDieDai = node.nodes[x];
                console.log(getNodeDieDai);
                for (j in getNodeDieDai) {
                    if (!getNodeDieDai.nodes[x].state.checked) {
                        ts.push(getNodeDieDai[j]);
                    }
                }
            }
        }
    }
    return ts;
}