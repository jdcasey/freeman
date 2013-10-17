// 
// Copyright (C) 2013 John Casey.
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
// 
function init(){
	$(document).ajaxError(function(e, xhr, settings, exception) {
		
		if ( exception ){
			alert("Command listing failed: " + e + "\n\n" + exception);
		} else {
			alert("Command listing failed: " + e);
		}
		
	});
	
	$('#select-command-pane').show();
	$('#command-pane').hide();
	
	$.ajax({
		type: 'get',
		url: '/commands',
	})
	.done(function(data,textstatus){
		
		var commands = JSON.parse(data)['items'];
		var commandList = $('#command-listing ul');
		$(commandList).html('');
		
		var commandItemTemplate = Handlebars.compile($("#command-item-template").html());
		$(commands).each(function(idx,command){
			var item = commandItemTemplate({'label': command});
			$(commandList).append(item);
		});
		
	});
}

$(document).on('click', '.command-link', function(){
	var command = $(this).attr('command');
	var commandUrl = '/commands/' + command;
	
	alert( 'GET: ' + commandUrl );
	
	$.ajax({
		type: 'get',
		url: commandUrl
	})
	.done(function(data,textstatus){
		alert(data);
		var form = $('#command-form-holder form').clone();
		
		$(form).attr('id', 'command-form').attr('action', commandUrl).attr('method', 'POST');
		$(form).select('.command-form-content').html(data);
		$('#command-pane').html($(form));
		$('#command-pane').show();
		$('#select-command-pane').hide();
		
		alert( $('#command-content-pane').html() );
	});
});

$(document).on('submit', '.command-form', function(){
	alert( "block submission on main form");
	return false;
});

$(document).on('click', '.command-cancel', function(){
	$('#command-pane').hide();
	$('#select-command-pane').show();
	
	$('#command-pane').html('');
});

$(document).on('click', '.command-execute', function(){
	var form = $('#command-form').clone();
	
	$(form).removeAttr('id').attr('id', 'iframe-form');
	$('#submit-frame').append($(form));
	
	alert( "executing iframe form");
	$(form).submit();
});
