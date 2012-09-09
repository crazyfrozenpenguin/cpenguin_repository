/* Author:

*/
var restRoot = 'rest/properties';
var jsonRoot = restRoot + '/json';
var downloadRoot = restRoot + '/download';
var orderUpdateRoot = restRoot + '/order';
var deleteRoot = restRoot + '/delete';
var createRoot = restRoot + '/create';

function CMViewModel() {
	
    // Data
    var self = this;
    self.dataSets = null;
    self.availableSets = ko.observableArray();
    self.setId = ko.observable();
    self.chosenFiles = ko.observableArray();
    self.filename = ko.observable();
    self.obsFilename = ko.observableArray();
    self.canAddProperty = ko.observable(false);
    self.dragging = ko.observable(undefined);
    self.onViewport = 1;
    self.newSet = ko.observable();
    self.newSetFile = ko.observable();
	
    // Behaviours
    self.showPropertiesFiles = function(data, event) {
        location.hash = data.set;
        if ($('.setsSelected')) {
        	$('.setsSelected').removeAttr('class');
        }
        $(event.srcElement).attr('class','setsSelected');
    };
    
    self.showFileProperties = function(data) {
        location.hash = self.setId() + '/' + data.filename;
        self.showViewportTwo();
    };
    
    self.createNewSetWindow = function() {
        $('.popupErrorMessage').text('');
        $("#createNewSet").dialog("open");
    };
    
    self.addNewSet = function() {
      if (/[^a-zA-Z0-9]/.test(self.newSet())) {
          $('.popupErrorMessage').text('Please remove illegal characters');
      } else {
          $.post(createRoot + '/' + self.newSet(), null, function() {
              $.get(jsonRoot, null, self.availableSets, 'json');
          });
          $('#createNewSet').dialog('close');
      }
    };
    
    self.createNewSetFileWindow = function() {
        $('.popupErrorMessage').text('');
        $('#createNewSetFile').dialog('open');
    };
    
    self.addNewSetFile = function(event) {
        if (/[^a-zA-Z0-9]/.test(self.newSetFile())) {
            $('.popupErrorMessage').text('Please remove illegal characters');
        } else {
            $.post(createRoot + '/' + self.setId() + '/' + self.newSetFile() + '.properties', null, function() {
                $.get(jsonRoot + '/' + self.setId(), null, self.updateChosenFiles, 'json');
            });
            $('#createNewSetFile').dialog('close');
        } 
    };
      
    // Helpers
    self.updateChosenFiles = function (data) {
    	self.chosenFiles(null);
    	if (data) self.chosenFiles(data.files);
    };

    self.updateChosenProperties = function(data) {
        self.obsFilename(null);
        if (data) self.obsFilename(data.properties);
    };
    
    self.move = function(from, to) {
        if (from && to) {
            var fromIndex = self.chosenFiles.indexOf(from);
            var toIndex = self.chosenFiles.indexOf(to);
            var files = self.chosenFiles();
            var copy = [];
            var order = '';
            var ci = 0;
            for (var i=0; i < files.length; i++) {
                if (i != fromIndex) {
                    order += '/' + files[i].filename.replace('.properties', '');
                    copy[ci++] = files[i];
                    if (i == toIndex) {
                        order += '/' + files[fromIndex].filename.replace('.properties', '');
                        copy[ci++] = files[fromIndex];
                    }
                } 
            }
            self.chosenFiles(null);
            self.chosenFiles(copy);
            // not the best way... it should post and request a new read on success...
            $.post(orderUpdateRoot + '/' + self.setId() + order);
        }
    };
    
    self.showViewportOne = function () {
        if (self.onViewport == 1) return; 
        $('.viewport').animate({ left: "+=980px" });
        self.filename(null);
        self.onViewport = 1;
    };
    
    self.showViewportTwo = function () {
        if (self.onViewport == 2) return;
        $('.viewport').animate({ left: "-=980px" });
        self.onViewport = 2;
    };
    
    self.showSetPath = function () {
        return self.setId() != 'ROOT';
    };
    
    self.showFilenamePath = function () {
        return self.filename() != null;
    };
    
    // Operations
    self.removeSet = function(entry) { 
        $.post(deleteRoot + '/' + self.setId(), function (data) {
            self.sammy.runRoute('get', '#ROOT');
        });
    };
    
    self.removePropertyFile = function(entry) { 
    	var fn = entry.filename.replace('.properties', '');
    	$.post(deleteRoot + '/' + self.setId() + '/' + fn, function (data) {
    		self.sammy.runRoute('get', '#' + self.setId());
    	});
    };
    
    self.addProperty = function() {
    	var entry = {key:'', value:''};
        self.obsFilename.push(entry);
    };
    
    self.removeProperty = function(entry) { 
    	self.obsFilename.remove(entry);
    };
    
    self.apply = function() {
    	var str = JSON.stringify(self.obsFilename());
    	var fn = self.filename().replace('.properties', '');
    	$.post(jsonRoot + '/' + self.setId() + '/' + fn, str);
    };
    
    self.downloadProperties = function(entry) {
    	if ('filename' in entry) {
    		var fn = entry.filename().replace('.properties', '');
    		window.open(downloadRoot + '/' + self.setId() + '/' + fn);
    	} else {
    		window.open(downloadRoot + '/' + self.setId());
    	}
    };
    
    self.downloadSetProperties = function(entry) {
        window.open(downloadRoot + '/' + self.setId());
    };
    
    // Client-side routes
    self.sammy = $.sammy(function() {
        
        this.get('#:set', function() {
        	self.canAddProperty(false);
        	self.setId(this.params.set);
        	self.showViewportOne();

            self.obsFilename.removeAll();
            if (self.setId() == 'ROOT') {
                self.updateChosenFiles(null);
                $.get(jsonRoot, null, self.availableSets, 'json');
            } else if (self.availableSets().length == 0) {
                $.get(jsonRoot, null, function(data) {
                    self.availableSets(data);
                    self.filename(null);
                    if ($('.setsSelected')) $('.setsSelected').removeAttr('class');
                    $('#set-' + self.setId()).attr('class', 'setsSelected');
                    $.get(jsonRoot + '/' + self.setId(), null, self.updateChosenFiles, 'json');
                }, 'json');
            } else {
                if ($('.setsSelected')) $('.setsSelected').removeAttr('class');
                $('#set-' + self.setId()).attr('class', 'setsSelected');
                self.filename(null);
            	$.get(jsonRoot + '/' + self.setId(), null, self.updateChosenFiles, 'json');
            }
        });
        
        this.get('#:set/:filename', function() {
        	self.canAddProperty(true);
        	self.setId(this.params.set);
            self.filename(this.params.filename);
            
            if (self.availableSets().length == 0) {
                $.get(jsonRoot, null, function(data) {
                    self.availableSets(data);
                    $.get(jsonRoot + '/' + self.setId(), null, function(data) {
                        self.updateChosenFiles(data);
                    	var fn = self.filename().replace('.properties', '');
                        $.get(jsonRoot + '/' + self.setId() + '/' + fn, null, self.updateChosenProperties, 'json');
                    }, 'json');
                }, 'json');
            } else if (self.chosenFiles().length == 0) {
                $.get(jsonRoot + '/' + self.setId(), null, function(data) {
                    self.updateChosenFiles(data);
                	var fn = self.filename().replace('.properties', '');
                	self.showViewportTwo();
                    $.get(jsonRoot + '/' + self.setId() + '/' + fn, null, self.updateChosenProperties, 'json');
                }, 'json');
            } else {
            	var fn = self.filename().replace('.properties', '');
            	self.showViewportTwo();
            	$.get(jsonRoot + '/' + self.setId() + '/' + fn, null, self.updateChosenProperties, 'json');
            }
        });
        
        this.get('', function() { 
            this.app.runRoute('get', '#ROOT'); 
        });
        
    }).run();
}

var cmViewModel = new CMViewModel();
var currentlyDragging = cmViewModel.dragging;

ko.bindingHandlers.drag = {
    init: function (element, valueAccessor, allBindingsAccessor, viewModel) {
        var $element = $(element),
            dragOptions = {
                revert: 'invalid',
                appendTo : 'body',
                helper: 'clone',
                start : function (e, ui) { 
                    logger('started dragging');
                    currentlyDragging(viewModel);
                },
                stop : function (e, ui) { 
                    logger('stopped dragging');
                }
            };
        
        $element.draggable(dragOptions);
    }
};

ko.bindingHandlers.drop = {
    init: function (element, valueAccessor, allBindingsAccessor, viewModel) {
        var $element = $(element),
            dropOptions = { 
                drop: function (e, ui) {
                    setTimeout(function () { 
                        logger('dropped ' + currentlyDragging().filename + ' on ' + viewModel.filename);
                        cmViewModel.move(currentlyDragging(), viewModel);
                    }, 0);
                    return true;
                }
            };
        $element.droppable(dropOptions);
    }
};

var debug=true;
var logger = function (log) {
    if (debug !== undefined && debug) {
        $('<div></div>').appendTo('#log').text(new Date().toGMTString() + ' : ' + log);
    }
};

$(function () {
    $('#createNewSet').dialog({
        autoOpen: false,
        resizable: false,
        height: 160,
        modal: true
    });

    $('#createNewSetFile').dialog({
        autoOpen: false,
        resizable: false,
        height: 160,
        modal: true
    });
    
    ko.applyBindings(cmViewModel);
});

