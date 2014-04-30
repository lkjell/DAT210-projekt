
// Global variables
var imageById = new Array();
var xpkeywords = new Array();

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
	var rest = this.slice((to || from) + 1 || this.length);
	this.length = from < 0 ? this.length + from : from;
	return this.push.apply(this, rest);
}

// Quentin http://stackoverflow.com/questions/979975
var QueryString = function () {
	// This function is anonymous, is executed immediately and 
	// the return value is assigned to QueryString!
	var query_string = {};
	var query = window.location.search.substring(1);
	var vars = query.split("&");
	for (var i=0;i<vars.length;i++) {
		var pair = vars[i].split("=");
			// If first entry with this name
		if (typeof query_string[pair[0]] === "undefined") {
			query_string[pair[0]] = pair[1];
			// If second entry with this name
		} else if (typeof query_string[pair[0]] === "string") {
			var arr = [ query_string[pair[0]], pair[1] ];
			query_string[pair[0]] = arr;
			// If third or later entry with this name
		} else {
			query_string[pair[0]].push(pair[1]);
		}
	}
	return query_string;
} ();

$( function() { // When document is ready

	console.log( "starting" );

	$( '#searchtext' ).change( function( event ) {
		var txt = $( this ).val();
		console.log( txt );
		search( txt, function( data, status, xhr ) { //success
			buildGrid( data );
		}, function( xhr, status, error ) { // error
			console.log( "id-list fetch at textbox update failed" );
		});
	});

	$( '.images' ).sortable().disableSelection();

	$( '.largeImgPanel' ).dblclick( function() {
		$( '.largeImgPanel' ).css( 'visibility', 'hidden' );
	});


	search( QueryString.filter, function( data, status, xhr ) { //success
		$( '#searchtext' ).val( QueryString.filter );
		buildGrid( data );
	});

	$( ".image" ).dblclick( showLargeImagePanel );
	$( ".pagecontainer" ).click(function() { $('.largeImgPanel').css('visibility', 'hidden'); });
});

/* Constructor for Keyword objects
 * Keyword is a mutable String object
 */
function Keyword( value ) {

	if( value == undefined )
		console.error( "Keyword instanciated without arguments" );
	this._val = value;
	function _set( value ) { this._val = value; console.log( "_set" ); }
	this.set = function( value ) {
		var oldvalue = this._val;
		var url = "meta/?old="+ oldvalue +"&new="+ value;
		console.log( "POST: "+ url );
		$.post( url, function() {
			console.log( "keyword "+ oldvalue +" changed to "+ value );
			_set( value );
		});
	}
}
Keyword.prototype.toString = function() { return this._val; }
Keyword.prototype.valueOf = function() { return this._val; }
Keyword.prototype.equals = function( arg ) {
	return this._val == arg.toString();
}

// Constructor for Image objects
// all metadata are stored in these objects
function Image( id, metadata ) {

	// private members
	var _xpkeywords = new Array();
	var that = this;

	function setMetadata( metadata ) {
		that.path   = metadata.path;
		that.width  = metadata.width;
		that.height = metadata.height;
		_xpkeywords = new Array();
		setKeywords( metadata.keywords );
	}

	function setKeywords( kws ) {
		var newkws = new Array();
		for( var i in kws ) {
			var kw = kws[i];
			// if kw is not a string
			if( !( typeof kw === 'string' )) continue;
			kw = kw.trim();
			// if kw is already in the local list
			if( that.hasKeyword( kw ) != -1 ) continue;
			// indexOf does not work comparing Keyword to String
			var index = -1;
			for( var j in xpkeywords ) {
				if( xpkeywords[j] == kw ) index = j;
			}
			// if kw is not in the global list
			if( index === -1 ) {
				index = xpkeywords.length;
				xpkeywords.push( new Keyword( kw ));
			}
			_xpkeywords.push( xpkeywords[index] );
			newkws.push( kw );
			console.log( "image"+ that.id +" added keyword: "+ kw );
		}
		return newkws;
	}

	// public members
	this.id = id;
	this.hasdata = false;
	if( metadata != undefined ) setMetadata( metadata );

	this.hasKeyword = function( keyword ) {
		for( var i in _xpkeywords ) {
			if( _xpkeywords[i] == keyword ) return i;
		}
		return -1;
	}

	this.fetchMetadata = function( after ) {
		console.log( "fetching metadata for img "+ this.id );
		get( "meta?img_id="+ this.id, function( data, status, xhr ) {
			setMetadata( data );
			if ( typeof after == 'function' ) after( data, status, xhr );
		});
	}

	this.getKeywords = function() { return _xpkeywords; }

	this.addKeywords = function( keywords ) {
		var newkws = setKeywords( arguments );
		var url = "meta/:"+ this.id +"/?add="+ newkws.toString();
		console.log( "POST: "+ url );
		$.post( url );
	}

	this.removeKeywords = function( keywords ) {
		var list = new Array();
		for( var i in arguments ) {
			var kw = arguments[i];
			// if kw is not a string
			if( !( typeof kw === 'string' ) ) continue;
			kw = kw.trim();
			// if kw is not in the local list
			if( that.hasKeyword( kw ) == -1 ) continue;
			list[i] = kw;
			var kws = this.getKeywords();
			for( var j in kws ) {
				if( kws[j] == kw ) kws.splice( j, 1 );
			}
			console.log( "image"+ that.id +" removed keyword: "+ kw );
		}
		var url = "meta/:"+ this.id +"/?remove="+ list.toString();
		console.log( "POST: "+ url );
		$.post( url );
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
	$.post({
		url: url,
		type: 'POST',
		dataType: 'json',
		data: plainObject,
		success: success,
		error: error
	});
}

function search( string, success, error ) {
	string = ( string == undefined ) ? "search" : "search?string="+ string;
	$.ajax({
		url: string,
		type: 'GET',
		dataType: 'json',
		success: success,
		error: error
	});
}

function sortBy() {}

function buildGrid( ids ) {
	console.log( ids.toString() );
	var images = $( '.images' ).empty();
	for( var i=0; i<ids.length; i++ ) {
		if( imageById[ids[i]] == undefined ) new Image( ids[i] );
		images.append(
			$( '<li>' ).attr( 'class', "image" ).attr( 'id', ids[i] ).append(
			$( '<img>' ).attr( 'src', "img/?img_id="+ ids[i] ))
		);
	}
	$( '.image' ).hover(
		function() { this.style.zIndex = '2'; }, // mouse enter
		function() { this.style.zIndex = '1'; }  // mouse leave
	).click( function() {
		updateSidebar( $( this ).attr( 'id' ));
	}).dblclick( showLargeImagePanel );
}

function updateSidebar( img_id ) {
	var image = imageById[img_id];
	if ( !image.hasdata ) image.fetchMetadata( writeIt );
	else writeIt();
	function writeIt() {

		var container = $( '<div>' );
		var kwsliste = $('<p>');
		var kws = image.getKeywords();
		// returns a function to run once an input is changed
		function onKeywordChange( keyword ) {
			return function() {
				var newkw = $( this ).val();
				if( newkw == "" ) {
					image.removeKeywords( keyword.toString() );
					return;
				}
				console.log( "edited input from "
					+ keyword.toString() +" to "+ newkw )
				keyword.set( newkw );
			}
		}
		for( var i = 0; i < kws.length; i++ ){
			var kw = kws[i]
			kwsliste.append( $('<input>').val( kw.toString() )
				.attr( 'id', i ).attr('class', 'lol89')
				.change( onKeywordChange( kw ) )
			);
		}

		var kwsfelt = $( '<div>' );
		kwsfelt.append( kwsliste );
		kwsfelt.append( $('<input>').attr( 'placeholder', "new keyword" )
			.change( function() {
				image.addKeywords( $( this ).val() );
			} )
		);

		console.log(image.getKeywords().toString());
		container.append(
			$( '<p>' ).text( "filepath: "+ image.path ),
			$( '<p>' ).text( "dimensions: "+ image.width +" x "+ image.height ),
			$( '<p>' ).text( "keywords: "), kwsfelt
		);
		$( ".right" ).html( container );
	}
}

function updateKeyword( oldKw, newKw ) {
	post( "meta/?old="+ oldKw +"&new="+ newKw, function() {
		alert( oldKw +" changed to "+ newKw );
	});
}

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
