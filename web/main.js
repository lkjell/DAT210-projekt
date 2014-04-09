// $( function() {
// $('body').append('<div id="largeImgPanel" onclick="hideMe(this);"><img id="largeImg" style="height: 100%; margin: 0; padding: 0;" /></div>');
// });

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

	/*$(document).keydown(function(e){
    	if (e.keyCode == 37) { 
       		alert( "left pressed " );
       		return false;
    	}
    	if (e.keyCode == 39) { 
       		alert( "right pressed " );
       		return false;
    	}
	});*/
	
	$( ".largeImgPanel" ).click(
		function() {
		document.getElementById('largeImgPanel').style.visibility = 'hidden';
		});


	
});

//alert("test") kontrol + q for comment

			// function hideLargeImagePanel() {
            // document.getElementById('largeImgPanel').style.visibility = 'hidden';
            // }

            function showLargeImagePanel() {
            document.getElementById('largeImgPanel').style.visibility = 'visible';
			
            }
            function unselectAll() {
                if(document.selection) document.selection.empty();
                if(window.getSelection) window.getSelection().removeAllRanges();
            }

imgs=Array(1,2,3,4,5);
var i=0;
document.onkeydown = checkKey;
function checkKey(e) {
    e = e || window.event;
    if (e.keyCode == '37') {
        document.getElementById(".image").src=imgs[--i];
    }
    else if (e.keyCode == '39') {
        document.getElementById(".image").src=imgs[++i];
    }
}

//src ==> getElementById