var hiModule = angular.module('rbnr', ['angularFileUpload']);

hiModule.directive('rbnr', ['$http', '$upload', function($http, $upload){
	return {
		restrict: 'A',
		replace: true,
		template: '<div>' +
					'<div class="description">' +
						'<div style="text-align: center">' +
							'<h2>Headstone Cleaner</h2>' +
						'</div>' +
						'<div ng-show="showDescription" align="left">' +
							'<h3>Demo</h3>' +
							'This is a demo of my racing bib recognition software. To use, upload an image or choose from images that are already on the server.' +
							'<h3>Output</h3>The output of the process is simply the OCR\'d numbers.' +
							'<h3>Contact</h3>For further information or questions please contact me at <a href="' + 'mail' + 'to:' + '{{username}}' + '@' + '{{hostname}}' + '">' + '{{linktext}}' + '</a>.' +
							'<br /><br /><b></b>' +
							'<br /><br />' + 
							
						'</div>' +
						'<a href="#" ng-click="toggleDescription(true)" ng-show="!showDescription">(Show description)</a>' +
						'<a href="#" ng-click="toggleDescription(false)" ng-show="showDescription">(Hide description)</a>' +
					'</div>' +
					'<div id="headstoneCleaner" class="demo">' +
						'<div id="results">' +
							'<span class="warn" ng-show="processing"> Processing Image... </span><br /><br />' +
							'<div class="originalImage">' +
								'Chosen Image: <br />' +
								'<img id="original" class="originalImage" /></a>' +
							'</div>' +
							'<div ng-if="result.processingTime">Processing time: <span ng-bind="result.processingTime" /> seconds</div><br /><br />' +
							'<div ng-if="result.ocrResult && hasResult">Rbn\'s found: <br />' +
								'<span style="display:inline-flex" ng-repeat="(key, value) in result.ocrResult">' +
									'Image {{key}}:' +
									'<ul style="display:inline-block;">' +
										'<li style="text-align:left" ng-repeat="transcription in value">{{transcription}}</li>' +
									'</ul>' +
								'</span>' +
							'<br /><br /></div>' +

//							'<div ng-if="result.ABBYYOcr && hasResult">ABBYY Transcription: <span ng-bind="result.ABBYYOcr" /><br /><br /></div>' +
						'</div>' +
						'<div>' +
							'<input type="radio" ng-model="inputType" value="upload">upload image</input>' +
							'<input type="radio" ng-model="inputType" value="available">select a pre-uploaded image</input>' +
						'</div>' +
						'<hr />' +
						'<div id="chooseImageDiv" ng-show="inputType===\'available\'">' +
							'<select ng-model="result.inputImage" size="10" ng-options="availableImage as availableImage for availableImage in availableImages" ng-change="selectInputImage()">' +
							'</select>' +
						'</div>' +
						'<div id="uploadImageDiv" ng-show="inputType===\'upload\'">' +
							'<input type="file" ng-file-select="onFileSelect($files)" accept="image/jpeg,image/png"/>' +
						'</div>' +
						'<hr />' +
						'<div>' +
//							'<input type="checkbox" id="doOcrCheckbox" name="doOcr" ng-model="doOcr" />Do OCR<br />' +
//							'<input type="checkbox" id="doABBYYOcrCheckbox" name="doABBYYOcr" ng-model="doABBYYOcr" />Do ABBYY OCR<br />' +
							'<input type="button" id="submitButton" ng-click="submitPhoto()" ng-disabled="processing" value="submit" />' +
						'</div>' +
					'</div>' +
				'</div>'
,
		link: function(scope, element, attrs){
			var inputImageDir = "data/input-images";
			scope.result = {};
			scope.input = {};
			scope.hasResult = false;
			scope.inputType = "available";
			scope.showDescription = true;
			scope.processing = false;
			scope.doOcr = false;
			scope.submitPhoto = function()
			{
				scope.showDescription = false;
				scope.processing = true;
				$http.post('/rbnr/rest/binarize', scope.result.inputImage).
			    success(function(result, status, headers, config) {
			    	scope.result = result;

//			    	scope.result.inputImage = result.originalPath.substr(result.originalPath.lastIndexOf('/') + 1);
			    	scope.result.processingTime = Math.round((result.duration * 0.001) * 1000) / 1000;
			    	scope.processing = false;
					scope.hasResult = true;
					document.images.original.src = result.originalPath;
			    }).
			    error(function(data, status, headers, config) {
			    	if (status === 429)
			    	{
			    		alert("You have exceeded the number of images that you can run this demo, please try again tomorrow, or contact me for further options");
			    	}
			    	else
			    	{
				    	alert("There was an error making the request");
			    	}
			    	scope.processing = false;
					scope.hasResult = false;
			    });
			};
			
			scope.onFileSelect = function($files){
				var file = $files[0];
		    	scope.result.processingTime = null;
		    	scope.hasResult = false;
		    	
				scope.upload = $upload.upload({
					url: "/rbnr/rest/upload",
					file: file
				}).success(function(data, status, headers, config){
			    	document.images.original.src = data;
			    	scope.result.inputImage = data.substr(data.lastIndexOf('/') + 1);
				}).error(function(data, status, headers, config){
			    	if (status === 429)
			    	{
			    		alert("You have exceeded the number of images that you can run this demo, please try again tomorrow, or contact me for further options");
			    	}
			    	else
			    	{
			    		alert("There was an error uploading the image");
			    	}
			    	scope.processing = false;
					scope.hasResult = false;
				});
			};
			
			scope.getAvailableImages = function(){
				$http.get('/rbnr/rest/inputImages').
				success(function(data, status, headers, config) {
					scope.availableImages = data;
				}).
				error(function(data, status, headers, config) {
		    		alert("There was an error getting the available images");
				});
			};
			
			scope.selectInputImage = function(){
				document.images.original.src = inputImageDir + "/" + scope.result.inputImage;
				scope.hasResult = false;
				scope.result.processingTime = null;
			};
			
			scope.toggleDescription = function(show){
				scope.showDescription = show;
			};
			
			scope.changeSelectedImage = function(optionValue){
				scope.inputType = 'available';
				scope.hasResult = false;
				scope.result.inputImage = optionValue;
				document.images.original.src = inputImageDir + "/" + scope.result.inputImage;
				scope.submitPhoto();
			};
			
			scope.username = "cam";
			scope.hostname = "cameronchristiansen";
			scope.linktext = scope.username + "@" + scope.hostname;
			
			scope.getAvailableImages();
		}
	};
}]);