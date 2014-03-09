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
		success: function( data, status, xhr ) {
			accordion = $('<div>');
			//recursive(accordion, data);

			$.map(data[0], function(value, key) {
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

			$( ".right" ).append( accordion );
		},
		error: function( xhr, status, error ) {
			alert( status +"\n"+ error );
		}
	});

	// function recursive(dom, root) {
	// 	$.map(root, function(value, key) {
	// 		h3 = $( '<h3>', key );
	// 		for ( i=0; i < value.length; i++ ) {
	// 			if (i == 0 && value[0] != null) {
	// 				$.map(root, function(value, key) {
						
	// 				});
	// 			}
	// 			if value instanceof Object recursive(dom, value);
	// 		}
			
	// 		sub = $('<div>', text );
	// 		//text += key + ": "
	// 		accordion.append( h3, sub )
	// 	});
	// }
}

function showLargeImagePanel() {
	var imgSource = $( this ).find('img').attr('src');
	console.log(imgSource);
	$('#largeImg').attr( 'src', imgSource );
	$('.largeImgPanel').css( 'visibility', 'visible');
	if(document.selection) document.selection.empty();
	if(window.getSelection) window.getSelection().removeAllRanges();
}
