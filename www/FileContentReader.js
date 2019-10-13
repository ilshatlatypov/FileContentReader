var exec = require('cordova/exec');

module.exports.readContent = function (contentUri, success, error) {
    exec(success, error, 'FileContentReader', 'readContent', [contentUri]);
};

module.exports.getFileDetails = function (contentUri, success, error) {
    exec(success, error, 'FileContentReader', 'getFileDetails', [contentUri]);
};
