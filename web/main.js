
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
			//$('#button').css('hidden');
			//skjulKnappene();
			//unselectAll();
			//$(".image").keypress();
	});
	
		$( ".largeImgPanel" ).click(
		function() {
		document.getElementById('largeImgPanel').style.visibility = 'hidden';
		});

	
});

//vic
            function showLargeImagePanel() {
            document.getElementById('largeImgPanel').style.visibility = 'visible';
			
            }
            //function unselectAll() {
                if(document.selection) document.selection.empty();
                if(window.getSelection) window.getSelection().removeAllRanges();
            }


//diroy
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

//function unselectAll() {
    if(document.selection) document.selection.empty();
    if(window.getSelection) window.getSelection().removeAllRanges();
}

//function hideMe(obj) {
    obj.style.visibility = 'hidden';
}

<script type="text/javascript">
/* KEYNAV */
document.onkeydown = function(e) {
if (! e) var e = window.event;
var code = e.charCode ? e.charCode : e.keyCode;
if (! e.shiftKey && ! e.ctrlKey && ! e.altKey && ! e.metaKey) {
if (code == Event.KEY_LEFT) {
if ($('previous_page_link')) location.href = $('previous_page_link').href;
} else if (code == Event.KEY_RIGHT) {
if ($('next_page_link')) location.href = $('next_page_link').href;}
}
}); 
</script>

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