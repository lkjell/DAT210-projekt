
// Global variables
imageById = new Array();

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
	var rest = this.slice((to || from) + 1 || this.length);
	this.length = from < 0 ? this.length + from : from;
	return this.push.apply(this, rest);
}

$( function() { // When document is ready

	$( '.images' ).sortable().disableSelection();

	$( '.image' ).each( function( index, element ) {
		new Image( $( element ).attr( 'id' ) );
	});

	$( '.image' ).hover(
		function() { this.style.zIndex = '2'; }, // mouse enter
		function() { this.style.zIndex = '1'; }  // mouse leave
	).click( function() { alert("sending request"); requestMetadata( $( this ).attr( 'id' )); }
	).dblclick( showLargeImagePanel );

	$( '.largeImgPanel' ).dblclick( function() {
		$( '.largeImgPanel' ).css( 'visibility', 'hidden' );
	});
});

function Image( id, metadata ) {

	// private members
	var _xpkeywords;

	function setMetadata( metadata ) {
		_xpkeywords = metadata.exif.XPKeywords;
		this.path   = metadata.file.path;
		this.width  = metadata.image.width;
		this.height = metadata.image.height;
	}

	// public members
	this.id = id;
	if( metadata != undefined ) setMetadata( metadata );

	this.fetchMetadata = function() {
		get( "meta{"+ this.id +"}", function( data, status, xhr ) {
			alert( data );
		});
	}

	this.getKeywords = function() { return _xpkeywords;	}

	this.addKeywords = function( keywords ) {
		for( var i in arguments ) {
			if( !( typeof arguments[i] === 'string' ) || arguments[i] in _xpkeywords )
				return; // TODO: remove instead
		}
		post( "meta{"+ this.id +"}", {XPKeywords: arguments},
			function( data, status, xhr ) {
				alert( data );
			}
		);
	}

	this.removeKeywords = function( keywords ) {
		for( var i in arguments ) { if( !( typeof arguments[i] === 'string' )) return; }
	}

	imageById[id] = this;
}

function get( url, success, error ) {
	$.ajax({
		url: url,
		type: 'GET',
		dataType: 'json',
		success: success,
		error: error
	});
}

function post( url, plainObject, success, error ) {
	$.ajax({
		url: url,
		type: 'POST',
		dataType: 'json',
		data: plainObject,
		success: success,
		error: error
	});
}

function search() {}

function sortBy() {}

function requestMetadata( file_id ) {

	console.log("ajax request "+ file_id);
	$.ajax({
		url: "getTags?img_id="+ file_id,
		dataType: 'json',
		success: function( data, status, xhr ) {
			dom = $( '<div>' );
			recursive( dom, data );
			dom.accordion({
				heightStyle: "content"
			});
			/*$.map(data[0], function(value, key) {
				h3 = $( '<h3>' ).text( key );
				string = "";
				for ( var i=0; i < value.length; i++ ) {
					string += value;
				}
				sub = $('<div>').text( string );
				console.log( $('<div>').append(sub).html() );
				//text += key + ": "
				accordion.append( h3, sub );
			});

			$( ".right" ).append( accordion );*/
		},
		error: function( xhr, status, error ) {
			alert( status +"\n"+ error );
		}
	});

	function recursive(dom, data) {

		$.map(data, function( value, key ) {
					console.log( "key; " + key );
			h3 = $( '<h3>' ).text( key );
			sub = $('<div>');
			for ( var i = 0; i < value.length; i++ ) {
				if( i === 0 ){
					if ( value[i] === null ) { continue;	}
					ul = $('<ul>')
					for(i=0;i<value.length;i++){
						//console.log( faen + ":" + helvette );
						ul.append( $('<li>').text( value[i] ));
						console.log(ul.html())
					};
					//console.log( $('<div>').append(ul).html() );
					sub.html( ul );
					console.log( $('<div>').append(sub).html() );
					break;
				}
				recursive(dom, value[i]);
			}
			
			//console.log( $('<div>').append(sub).html() );
			//text += key + ": "
			dom.append( h3, sub );
		});
		console.log("fuck you");
		$( ".right" ).html( dom );
	}
}

function showLargeImagePanel() {
	var imgSource = $( this ).find('img').attr('src');
	console.log(imgSource);
	$('#largeImg').attr( 'src', imgSource );
	$('.largeImgPanel').css( 'visibility', 'visible');
	if(document.selection) document.selection.empty();
	if(window.getSelection) window.getSelection().removeAllRanges();
}



// Copyright 2006-2007 javascript-array.com

var timeout	= 500;
var closetimer	= 0;
var ddmenuitem	= 0;

// open hidden layer
function mopen( id )
{	
	// cancel close timer
	mcancelclosetime();

	// close old layer
	if(ddmenuitem) ddmenuitem.style.visibility = 'hidden';

	// get new layer and show it
	ddmenuitem = document.getElementById(id);
	ddmenuitem.style.visibility = 'visible';

}
// close showed layer
function mclose()
{
	if(ddmenuitem) ddmenuitem.style.visibility = 'hidden';
}

// go close timer
function mclosetime()
{
	closetimer = window.setTimeout(mclose, timeout);
}

// cancel close timer
function mcancelclosetime()
{
	if(closetimer)
	{
		window.clearTimeout(closetimer);
		closetimer = null;
	}
}
