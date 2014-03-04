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
			text = "";
			try{ text += "Filename: "+ data.filename }catch(e){}
			try{ text += "<br/>Dimensions: "+ data.width +" x "+ data.height }catch(e){}
			try{ text += "<br/>Keywords: "+ data.exif.keywords }catch(e){}
			$( ".right" ).html( text );
		},
		error: function( xhr, status, error ) {
			alert( status +"\n"+ error );
		}
	});
}

function showLargeImagePanel() {
	var imgSource = $( this ).find('img').attr('src');
	console.log(imgSource);
	$('#largeImg').attr( 'src', imgSource );
	$('.largeImgPanel').css( 'visibility', 'visible');
	if(document.selection) document.selection.empty();
	if(window.getSelection) window.getSelection().removeAllRanges();
}
