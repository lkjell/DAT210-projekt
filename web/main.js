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
});