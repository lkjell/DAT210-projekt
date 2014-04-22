$( function() {

	$( ".images" ).sortable().disableSelection();

	$( ".image" ).hover(
		function() { this.style.zIndex = '2'; }, // mouse enter
		function() { this.style.zIndex = '1'; }  // mouse leave
	);

	$( ".image" ).click(function(){
		requestMetadata($( this ).attr( "id" ));
	});

	$( ".image" ).dblclick( showLargeImagePanel );
	$( ".pagecontainer" ).click(function() { $('.largeImgPanel').css('visibility', 'hidden'); });
	
});

function requestMetadata( file_id ) {

	console.log("ajax request "+ file_id);
	$.ajax({
		url: "getTags?img_id="+ file_id,
		dataType: 'json',
		success: function( data, status, xhr ) {
			dom = $('<div>');
			recursive(dom, data);
			dom.accordion({
				heightStyle: "content"
			});
//			$.map(data[0], function(value, key) {
//				h3 = $( '<h3>' ).text( key );
//				string = "";
//				for ( var i=0; i < value.length; i++ ) {
//					string += value;
//				}
//				sub = $('<div>').text( string );
//				console.log( $('<div>').append(sub).html() );
//				//text += key + ": "
//				accordion.append( h3, sub );
//			});
//
//			$( ".right" ).append( accordion );
		},
		error: function( xhr, status, error ) {
			alert( status +"\n"+ error );
		}
	});

	function recursive(dom, data) {

		$.map(data, function(value, key) {
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
function mopen(id)
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
