$( function() {

	$( ".images" ).sortable().disableSelection();

	$( ".image" ).hover(
		function() { this.style.zIndex = '2'; }, // mouse enter
		function() { this.style.zIndex = '1'; }  // mouse leave
	);

	$( ".image" ).click(function(){
		requestMetadata($( this ).find(">").attr( "src" ));
	});

	$( ".image" ).dblclick( showLargeImagePanel );
	
	$( ".largeImgPanel" ).dblclick( function() {
		$('.largeImgPanel').css( 'visibility', 'hidden' );
	});
});

function requestMetadata( file ) {

	console.log("ajax request "+ file);
	$.ajax({
		url: "getTags/"+ file,
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
					$.map(value[0], function(helvette, faen){
						//console.log( faen + ":" + helvette );
						ul.append( $('<li>').text( faen + ":" + helvette ));
					});
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
