$( function() {
$('body').append('<div id="largeImgPanel" onclick="hideMe(this);"><img id="largeImg" style="height: 100%; margin: 0; padding: 0;" /></div>');
});

$( function() {
	$( ".image" ).draggable({
		containment:       ".images",
		connectToSortable: ".image",
		scroll:            true,
		revert:            true
	});
	$( ".images" ).sortable().disableSelection();
	$( ".image" ).hover(
		function() { this.style.zIndex = '2'; }, // mouse enter
		function() { this.style.zIndex = '1'; }  // mouse leave
	);
	
	$( ".image" ).click(
		function () {
			var imgSource = $(this).find('img').attr('src');
			document.getElementById('largeImg').src = imgSource; 
			showLargeImagePanel();
			unselectAll();
			//alert(imgSource);
		}
		
	);
	
});

//alert("test") kontrol + q for comment

            function showLargeImagePanel() {
            document.getElementById('largeImgPanel').style.visibility = 'visible';
			
            }
            function unselectAll() {
                if(document.selection) document.selection.empty();
                if(window.getSelection) window.getSelection().removeAllRanges();
            }
            function hideMe(obj) {
                obj.style.visibility = 'hidden';
            }
