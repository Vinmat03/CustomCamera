var images = 
[{additionalInfo:"false", code:"90001", description:"http://1.bp.blogspot.com/-nl2v6bMGaho/UWRmBj_2HfI/AAAAAAAABTA/FyzIbevz9to/s1600/P1160946.JPG"},
{additionalInfo:"false", code:"90002", description:"http://2.bp.blogspot.com/-AxUkDyYd3Bw/UU9oy57RRGI/AAAAAAAABQQ/-geit6e7xaQ/s1600/P1150252bis.jpg"},
{additionalInfo:"false", code:"90003", description:"http://3.bp.blogspot.com/-BzXIfF-FwGA/UUdy2x9A9VI/AAAAAAAABN4/LW87xQ63C1c/s1600/P1150969.JPG"},
{additionalInfo:"false", code:"90004", description:"http://1.bp.blogspot.com/-G_1iQU43ajw/UUNp8GFMgOI/AAAAAAAABEE/fg2PiYP4Ebo/s1600/P1110852.JPG"},
{additionalInfo:"false", code:"90005", description:"http://4.bp.blogspot.com/-BFoZuyhpKDk/UWhmM2qyemI/AAAAAAAABTY/VtzXGLr3GRM/s1600/P1170064.JPG"},
{additionalInfo:"false", code:"90006", description:"http://2.bp.blogspot.com/-RDbKCUh9IYg/UWOp09NiChI/AAAAAAAABSo/90pA5DjwAAs/s1600/P1160905bis.jpg"},
{additionalInfo:"false", code:"90007", description:"http://4.bp.blogspot.com/-bJUYkxQmMlE/UUbYLYtlrkI/AAAAAAAABMA/pC_Cfo6KEUs/s1600/P1130577.JPG"},
{additionalInfo:"false", code:"90008", description:"http://3.bp.blogspot.com/-NoURBoBVGPc/UUuDhVk_jHI/AAAAAAAABPY/PqQy95HfP8g/s1600/P1150152.JPG"},
{additionalInfo:"false", code:"90009", description:"http://1.bp.blogspot.com/-vctPnmU-HiI/UUjnFNr0uQI/AAAAAAAABOQ/VmUztmiv-7g/s1600/P1140851bis.jpg"},
{additionalInfo:"false", code:"90010", description:"http://2.bp.blogspot.com/-IGLYzvGKWbs/UUdzkU-DozI/AAAAAAAABOA/rKnzNfWvhCo/s1600/P1160235.JPG"},
{additionalInfo:"false", code:"90011", description:"http://3.bp.blogspot.com/-eBrxpAouK0U/UUcjNiRjx3I/AAAAAAAABNQ/120MQrDeO2s/s1600/P1150213.JPG"},
{additionalInfo:"false", code:"90012", description:"http://4.bp.blogspot.com/-pLf6pLv-cY4/UUcHEnzrXLI/AAAAAAAABMQ/QzcoOx-P4Tw/s1600/P1150309.JPG"},
];

var images_empty = [];

var CameraModule = require('ti.customcamera');

var win = Ti.UI.createWindow({
    backgroundColor : 'white'
});

var button1 = Titanium.UI.createButton({
	   title: 'Snap Picture with images array',
	   top:'25%'
	});

var button2 = Titanium.UI.createButton({
	   title: 'Snap Picture without images array',
	   bottom:'25%'
	});

win.add(button1);
win.add(button2);




button1.addEventListener('click', function() {
    CameraModule.showCameraView(images);
});
button2.addEventListener('click', function() {
    CameraModule.showCameraView(images_empty);
});


// Register a listener function for event callback for save event.
CameraModule.addEventListener('uploadImages',function(e){
    alert("Welcome back : "+JSON.stringify(e.images));
});


// Register a listener function for event callback for image delete event.
CameraModule.addEventListener('deleteImage', function(e) {
   var idx = e.index;
   alert("Delete Image : "+JSON.stringify(e));
   Ti.API.info("Delete Image : "+JSON.stringify(e));
});

// Register a listener function for  event callback for set default event.
CameraModule.addEventListener('setDefaultImage', function(e) {
	   var idx = e.index;
	   alert("setDefaultImage : "+JSON.stringify(e));
	   Ti.API.info("setDefaultImage : "+JSON.stringify(e));
	});

win.open();
