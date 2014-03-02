$( function() {
	//$( ".image" ).draggable({
	//	containment:       ".images",
	//	connectToSortable: ".image",
	//	scroll:            true,
	//	revert:            true
	//});
	$( ".images" ).sortable().disableSelection();
	$( ".image" ).hover(
		function() { this.style.zIndex = '2'; }, // mouse enter
		function() { this.style.zIndex = '1'; }  // mouse leave
	);
	$( ".image" ).click(function(){
		requestMetadata($( this ).find(">").attr( "src" ));
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