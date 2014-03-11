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
	
	$( ".image" ).click(function () {
			var imgSource = $(this).find('img').attr('src');
			document.getElementById('largeImg').src = imgSource; 
			showLargeImagePanel();
			$('#button').css('hidden');
			skjulKnappene();
			unselectAll();
			$(".image").keypress();
	});
	
});

function skjulKnappene() {
	$('#button').css('hidden');
}

function visKnappene() {
	$('#button').css('visibility');
}

function showLargeImagePanel() {
	document.getElementById('largeImgPanel').style.visibility = 'visible';
	$('#button').css('hidden');
}

function unselectAll() {
    if(document.selection) document.selection.empty();
    if(window.getSelection) window.getSelection().removeAllRanges();
}

function hideMe(obj) {
    obj.style.visibility = 'hidden';
}


//Disabler scrollbar
//var ar=new Array(33,34,35,36,37,38,39,40);

//$(document).keydown(function(e) {
     //var key = e.which;
      //console.log(key);
      //if(key==35 || key == 36 || key == 37 || key == 39)
      //if($.inArray(key,ar) > -1) {
          //e.preventDefault();
          //return false;
      //}
      //return true;
//});