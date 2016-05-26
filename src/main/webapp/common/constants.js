//Namespaced Constants
var ZephyrApp;
// MyAppName Namespace
(function(ZephyrApp) {
	// MyAppName.Constants Namespace
	(function(Constants) {
		// Private
		function createConstant(name, val) {
			Object.defineProperty(ZephyrApp.Constants, name, {
				value : val,
				writable : false
			});
		}

		// Public

		// initialize messageResource.js
		messageResource.init({
			// path to directory containing config.properties
			filePath : 'resources'
		});

		// load config.properties file
		messageResource
				.load(
						'config',
						function() {
							// load file callback

							// get value corresponding to a key from
							// config.properties

							Constants.GETUSECASESURL = createConstant(
									"GETUSECASESURL",
									 messageResource.get('hostname', 'config') + '/zephyrtool/rest/getusecases');
							Constants.GETFEATURESURL = createConstant(
									"GETFEATURESURL",
									messageResource.get('hostname', 'config') + '/zephyrtool/rest/getfeatures');
							
						});

		ZephyrApp.Constants = Constants;
	})(ZephyrApp.Constants || (ZephyrApp.Constants = {}));
})(ZephyrApp || (ZephyrApp = {}));