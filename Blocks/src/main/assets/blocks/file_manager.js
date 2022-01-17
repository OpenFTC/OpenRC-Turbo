/**
 * @license
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview File manager code
 * @author lizlooney@google.com (Liz Looney)
 */

// The following variable must be declared in the html.
// FILE_MANAGER_NAME
// NAME_CLASH
// PLAY_FILE_FUNCTION (optional)

var files = [];
var checkedFiles = [];
var sortByName = false;
var sortByDateModified = true;
var sortAscending = false;
var NEW_NAME_DIALOG_MODE_RENAME_FILE = 1;
var NEW_NAME_DIALOG_MODE_COPY_FILE = 2;
var newNameDialogMode = 0;

var onresize = function(e) {
  // Compute the height of filesTableScroll.
  var filesTableScroll = document.getElementById('filesTableScroll');
  var element = filesTableScroll;
  var y = 0;
  do {
    y += element.offsetTop;
    element = element.offsetParent;
  } while (element);
  filesTableScroll.style.height = (window.innerHeight - y) + 'px';
};

function initialize() {
  fetchJavaScriptForFileManager(function(js, errorMessage) {
    if (js) {
      var newScript = document.createElement('script');
      newScript.setAttribute('type', 'text/javascript');
      newScript.innerHTML = js;
      document.getElementsByTagName('head').item(0).appendChild(newScript);
    } else  {
      alert(errorMessage);
    }
  });

  initializeFiles();
}

function initializeFiles() {
  files = [];
  fetchFiles(function(json, errorMessage) {
    if (json) {
      files = JSON.parse(json);
    } else {
      alert(errorMessage);
    }
    sortFilesAndFillTable();
  });
}

function toggleSortByName() {
  if (sortByName) {
    sortAscending = !sortAscending;
  } else {
    sortByName = true;
    // When sorting by name, sorting ascending makes more sense than descending.
    sortAscending = true;
    sortByDateModified = false;
  }
  sortFilesAndFillTable();
}

function toggleSortByDateModified() {
  if (sortByDateModified) {
    sortAscending = !sortAscending;
  } else {
    sortByDateModified = true;
    // When sorting by date, sorting descending makes more sense than ascending.
    sortAscending = false;
    sortByName = false;
  }
  sortFilesAndFillTable();
}

function sortFilesAndFillTable() {
  if (sortByName) {
    files.sort(function(file1, file2) {
      return file1.name.localeCompare(file2.name);
    });
  } else if (sortByDateModified) {
    files.sort(function(file1, file2) {
      return file1.dateModifiedMillis - file2.dateModifiedMillis;
    });
  }
  if (!sortAscending) {
    files.reverse();
  }

  var table = document.getElementById('filesTable');
  // Remove all rows except the first one, which contains the column headers.
  for (var i = table.rows.length - 1; i >= 1; i--) {
    table.deleteRow(i);
  }
  for (var i = 0; i < files.length; i++) {
    var tr = table.insertRow(-1);
    tr.className = 'file_tr';

    var tdTrash = tr.insertCell(-1);
    tdTrash.innerHTML = '<input type="checkbox" id="checkbox_' + i + '" onclick="fileCheckChanged(' + i + ')">';

    var tdName = tr.insertCell(-1);
    if (PLAY_FILE_FUNCTION) {
      tdName.innerHTML = '<div class="file_name" onclick="' + PLAY_FILE_FUNCTION + '(' + i + ')">' +
          files[i].escapedName + '</div>';
    } else {
      tdName.innerHTML = '<div class="file_name">' +
          files[i].escapedName + '</div>';
    }

    var tdDateModified = tr.insertCell(-1);
    tdDateModified.innerHTML = formatTimestamp(files[i].dateModifiedMillis);
  }

  var upTriangle = '&#x25B2;';
  var downTriangle = '&#x25BC;';
  var nameSortIndicator = document.getElementById('sortByNameIndicator');
  if (sortByName) {
    nameSortIndicator.innerHTML = (sortAscending ? upTriangle : downTriangle);
  } else {
    nameSortIndicator.innerHTML = '';
  }
  var dateSortIndicator = document.getElementById('sortByDateModifiedIndicator');
  if (sortByDateModified) {
    dateSortIndicator.innerHTML = (sortAscending ? upTriangle : downTriangle);
  } else {
    dateSortIndicator.innerHTML = '';
  }

  checkedFiles = [];
  updateButtons();
}

function formatTimestamp(timestampMillis) {
  var date = new Date(timestampMillis);
  var monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'];
  var formatted = monthNames[date.getMonth()] + '&nbsp;' + date.getDate() +
      ',&nbsp;' + date.getFullYear() + ',&nbsp;';
  if (date.getHours() == 0) {
    formatted += '12';
  } else if (date.getHours() > 12) {
    formatted += (date.getHours() - 12);
  } else {
    formatted += date.getHours();
  }
  formatted += ':';
  if (date.getMinutes() < 10) {
    formatted += '0';
  }
  formatted += date.getMinutes() + ':'
  if (date.getSeconds() < 10) {
    formatted += '0';
  }
  formatted += date.getSeconds() + '&nbsp;';
  if (date.getHours() < 12) {
    formatted += 'AM';
  } else {
    formatted += 'PM';
  }
  return formatted;
}

function cancelNewNameDialog() {
  document.getElementById('newNameDialog').style.display = 'none';
}

function okNewNameDialog() {
  // Validate name for legal characters and for existing file names.
  var newName = document.getElementById('newName').value;
  // isValidName is generated dynamically in FileManager.fetchJavaScript():
  if (!isValidName(newName)) {
    document.getElementById('newNameError').innerHTML =
        'The name must only contains alphanumeric<br>characters and !#$%&\'()+,-.;=@[]^_{}~.';
    return;
  }
  var nameClash = false;
  for (var i = 0; i < files.length; i++) {
    if (newName == files[i].name) {
      nameClash = true;
      break;
    }
  }
  if (nameClash) {
    document.getElementById('newNameError').innerHTML = NAME_CLASH;
    return;
  }

  switch (newNameDialogMode) {
    case NEW_NAME_DIALOG_MODE_RENAME_FILE:
      renameFileOk(files[checkedFiles[0]].name, newName);
      break;
    case NEW_NAME_DIALOG_MODE_COPY_FILE:
      copyFileOk(files[checkedFiles[0]].name, newName);
      break;
  }
}

function uploadFilesButtonClicked() {
  // Show modal dialog asking for file.
  document.getElementById('uploadFilesFileInput').value = '';
  document.getElementById('uploadFilesError').innerHTML = '';
  document.getElementById('uploadFilesOk').disabled = true;
  document.getElementById('uploadFilesFileInput').onchange = function() {
    var files = document.getElementById('uploadFilesFileInput').files;
    document.getElementById('uploadFilesOk').disabled = (files.length == 0);
  };
  document.getElementById('uploadFilesDialog').style.display = 'block';
}

function cancelUploadFilesDialog() {
  // Close the dialog.
  document.getElementById('uploadFilesDialog').style.display = 'none';
}

var uploadCountDown = 0;
var uploadSuccess = true;

function okUploadFilesDialog() {
  var files = document.getElementById('uploadFilesFileInput').files;
  uploadCountDown = files.length;
  uploadSuccess = true;
  uploadNextFile();
}

function uploadNextFile() {
  if (uploadCountDown > 0) {
    uploadCountDown--;
    document.getElementById('uploadFilesOk').disabled = true;
    setWaitCursor(true);
    setTimeout(uploadFile, 100);
  } else {
    setWaitCursor(false);
    if (uploadSuccess) {
      // Close the dialog.
      document.getElementById('uploadFilesDialog').style.display = 'none';
    }
  }
}

var uploadFile = function() {
  var files = document.getElementById('uploadFilesFileInput').files;
  var uploadedFile = files[uploadCountDown];
  var name = makeNameForUploadedFile(uploadedFile.name);

  var reader = new FileReader();
  reader.onload = function(event) {
    var prefix = 'base64,';
    var iPrefix = event.target.result.indexOf(prefix);
    if (iPrefix != -1) {
      var base64Content = event.target.result.substring(iPrefix + prefix.length);
      saveFile(name, base64Content, function(success, errorMessage) {
        if (success) {
          initializeFiles();
        } else {
          uploadSuccess = false;
          document.getElementById('uploadFilesError').innerHTML += uploadedFile.name + ': ' + errorMessage + '<br>';
        }
        uploadNextFile();
      });
    } else {
      console.log('Error: Could not find "' + prefix + '" in event.target.result');
      uploadSuccess = false;
      document.getElementById('uploadFilesError').innerHTML += uploadedFile.name + ': Failed to upload file.<br>';
      uploadNextFile();
    }
  };
  reader.readAsDataURL(uploadedFile);
}

function setWaitCursor(wait) {
  if (wait) {
    document.body.classList.add('waitCursor');
  } else {
    document.body.classList.remove('waitCursor');
  }
}

function makeNameForUploadedFile(uploadedFileName) {
  var lastDot = uploadedFileName.lastIndexOf('.');
  var preferredName = (lastDot == -1)
      ? uploadedFileName
      : uploadedFileName.substr(0, lastDot);
  var extension = (lastDot == -1)
      ? ''
      : uploadedFileName.substr(lastDot);
  var name = preferredName + extension; // No suffix.
  var suffix = 0;
  while (true) {
    var nameClash = false;
    for (var i = 0; i < files.length; i++) {
      if (name == files[i].name) {
        nameClash = true;
        break;
      }
    }
    if (!nameClash) {
      return name;
    }
    suffix++;
    name = preferredName + '_' + suffix + extension;
  }
  return name;
}

function renameFileButtonClicked() {
  // Show modal dialog asking for file name.
  document.getElementById('newName').value = files[checkedFiles[0]].name;
  document.getElementById('newNameError').innerHTML = '';
  newNameDialogMode = NEW_NAME_DIALOG_MODE_RENAME_FILE;
  document.getElementById('newNameDialogTitle').innerHTML = 'Rename Selected File';
  document.getElementById('newNameDialog').style.display = 'block';
  document.getElementById('newName').focus();
}

function renameFileOk(oldName, newName) {
  renameFile(oldName, newName, function(success, errorMessage) {
    if (success) {
      // Close the dialog.
      document.getElementById('newNameDialog').style.display = 'none';
      initializeFiles();
    } else {
      document.getElementById('newNameError').innerHTML = errorMessage;
    }
  });
}

function copyFileButtonClicked() {
  // Show modal dialog asking for file name.
  document.getElementById('newName').value = files[checkedFiles[0]].name;
  document.getElementById('newNameError').innerHTML = '';
  newNameDialogMode = NEW_NAME_DIALOG_MODE_COPY_FILE;
  document.getElementById('newNameDialogTitle').innerHTML = 'Copy Selected File';
  document.getElementById('newNameDialog').style.display = 'block';
  document.getElementById('newName').focus();
}

function copyFileOk(oldName, newName) {
  copyFile(oldName, newName, function(success, errorMessage) {
    if (success) {
      // Close the dialog.
      document.getElementById('newNameDialog').style.display = 'none';
      initializeFiles();
    } else {
      document.getElementById('newNameError').innerHTML = errorMessage;
    }
  });
}

function downloadFilesButtonClicked() {
  for (var i = 0; i < files.length; i++) {
    var checkbox = document.getElementById('checkbox_' + i);
    if (checkbox.checked) {
      downloadFile(files[i].name);
    }
  }
}

function downloadFile(name) {
  fetchFileContent(name, function(base64Content, errorMessage) {
    if (base64Content) {
      var a = document.getElementById('download_link');
      a.href = 'data:text/plain;base64,' + base64Content;
      a.download = name;
      a.click();
    } else {
      alert(errorMessage);
    }
  });
}

var starDelimitedDeleteNames = '';

function deleteFilesButtonClicked() {
  starDelimitedDeleteNames = '';
  var table = document.getElementById('deleteFilesTable');
  // Remove all rows.
  for (var i = table.rows.length - 1; i >= 0; i--) {
    table.deleteRow(i);
  }
  // Gather the checked files.
  var delimiter = '';
  for (var i = 0; i < files.length; i++) {
    var checkbox = document.getElementById('checkbox_' + i);
    if (checkbox.checked) {
      starDelimitedDeleteNames += delimiter + files[i].name;
      delimiter = '*';
      // Insert name into the table.
      table.insertRow(-1).insertCell(-1).innerHTML = files[i].escapedName;
    }
  }
  if (table.rows.length > 0) {
    // Show modal dialog confirming that the user wants to delete the files.
    document.getElementById('deleteFilesDialog').style.display = 'block';
  }
}

function noDeleteFilesDialog() {
  // Close the dialog.
  document.getElementById('deleteFilesDialog').style.display = 'none';
}

function yesDeleteFilesDialog() {
  // Close the dialog.
  document.getElementById('deleteFilesDialog').style.display = 'none';
  deleteFiles(starDelimitedDeleteNames, function(success, errorMessage) {
    starDelimitedDeleteNames = '';
    if (success) {
      initializeFiles();
    } else {
      alert(errorMessage);
    }
  });
}

function fileCheckAllChanged(i) {
  var checkboxAll = document.getElementById('checkbox_all');
  if (checkedFiles.length == 0) {
    // Check all.
    checkedFiles = [];
    for (var i = 0; i < files.length; i++) {
      var checkbox = document.getElementById('checkbox_' + i);
      checkbox.checked = true;
      checkedFiles.push(i);
    }
    checkboxAll.checked = true;
  } else {
    // Check none.
    checkedFiles = [];
    for (var i = 0; i < files.length; i++) {
      var checkbox = document.getElementById('checkbox_' + i);
      checkbox.checked = false;
    }
    checkboxAll.checked = false;
  }
  updateButtons();
}

function fileCheckChanged(i) {
  var checkbox = document.getElementById('checkbox_' + i);
  if (checkbox.checked) {
    if (checkedFiles.indexOf(i) == -1) {
      checkedFiles.push(i);
    }
  } else {
    var index = checkedFiles.indexOf(i);
    checkedFiles.splice(index, 1);
  }
  updateButtons();
}

function updateButtons() {
  var renameFileButton = document.getElementById('renameFileButton');
  renameFileButton.disabled = (checkedFiles.length != 1);
  var copyFileButton = document.getElementById('copyFileButton');
  copyFileButton.disabled = (checkedFiles.length != 1);
  var downloadFilesButton = document.getElementById('downloadFilesButton');
  downloadFilesButton.disabled = (checkedFiles.length == 0);
  var deleteFilesButton = document.getElementById('deleteFilesButton');
  deleteFilesButton.disabled = (checkedFiles.length == 0);
}

/**
 * Fetches the JavaScript code related to the file manager and calls the callback.
 */
function fetchJavaScriptForFileManager(callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    fetchJavaScriptForFileManagerViaHttp(callback);
  }
}

/**
 * Fetches the list of files (as json) and calls the callback.
 */
function fetchFiles(callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    fetchFilesViaHttp(callback);
  }
}

/**
 * Fetches the content of an existing file and calls the callback
 */
function fetchFileContent(name, callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    fetchFileContentViaHttp(name, callback);
  }
}

function saveFile(name, base64Content, callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    saveFileViaHttp(name, base64Content, callback);
  }
}

function renameFile(oldName, newName, callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    renameFileViaHttp(oldName, newName, callback);
  }
}

function copyFile(oldName, newName, callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    copyFileViaHttp(oldName, newName, callback);
  }
}

function deleteFiles(starDelimitedNames, callback) {
  if (window.location.protocol === 'http:' || window.location.protocol === 'https:') {
    // html/js is in a browser, loaded as an http:// URL.
    deleteFilesViaHttp(starDelimitedNames, callback);
  }
}

//..........................................................................
// Code used when html/js is in a browser, loaded as an http:// URL.

// The following are generated dynamically in ProgrammingModeServer.fetchJavaScriptForServer():
// URI_FILE_MANAGER_JS
// URI_LIST_FILES
// URI_FETCH_FILE
// URI_FETCH_FILE_TYPE
// URI_SAVE_FILE
// URI_RENAME_FILE
// URI_COPY_FILE
// URI_DELETE_FILES
// PARAM_FM_NAME
// PARAM_NAME
// PARAM_NEW_NAME

function fetchJavaScriptForFileManagerViaHttp(callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME);
  xhr.open('POST', URI_FILE_MANAGER_JS, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var js = xhr.responseText;
        callback(js, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(null, 'Fetch JavaScript for FileManager failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function fetchFilesViaHttp(callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME);
  xhr.open('POST', URI_LIST_FILES, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var json = xhr.responseText;
        callback(json, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(null, 'Fetch files failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function fetchFileContentViaHttp(name, callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME) +
      '&' + PARAM_NAME + '=' + encodeURIComponent(name);
  xhr.open('POST', URI_FETCH_FILE, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var base64Content = xhr.responseText;
        callback(base64Content, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(null, 'Fetch file failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function fetchFileMimeTypeViaHttp(name, callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME) +
      '&' + PARAM_NAME + '=' + encodeURIComponent(name);
  xhr.open('POST', URI_FETCH_FILE_TYPE, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var mimeType = xhr.responseText;
        callback(mimeType, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(null, 'Fetch file mime type failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function saveFileViaHttp(name, base64Content, callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME) +
      '&' + PARAM_NAME + '=' + encodeURIComponent(name) +
      '&' + PARAM_CONTENT + '=' + encodeURIComponent(base64Content);
  xhr.open('POST', URI_SAVE_FILE, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        callback(true, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(false, 'Save file failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function renameFileViaHttp(oldName, newName, callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME) +
      '&' + PARAM_NAME + '=' + encodeURIComponent(oldName) +
      '&' + PARAM_NEW_NAME + '=' + encodeURIComponent(newName);
  xhr.open('POST', URI_RENAME_FILE, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        callback(true, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(false, 'Rename file failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function copyFileViaHttp(oldName, newName, callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME) +
      '&' + PARAM_NAME + '=' + encodeURIComponent(oldName) +
      '&' + PARAM_NEW_NAME + '=' + encodeURIComponent(newName);
  xhr.open('POST', URI_COPY_FILE, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        callback(true, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(false, 'Copy file failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}

function deleteFilesViaHttp(starDelimitedNames, callback) {
  var xhr = new XMLHttpRequest();
  var params = PARAM_FM_NAME + '=' + encodeURIComponent(FILE_MANAGER_NAME) +
      '&' + PARAM_NAME + '=' + encodeURIComponent(starDelimitedNames);
  xhr.open('POST', URI_DELETE_FILES, true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        callback(true, '');
      } else {
        // TODO(lizlooney): Use specific error messages for various xhr.status values.
        callback(false, 'Delete files failed. Error code ' + xhr.status + '. ' + xhr.statusText);
      }
    }
  };
  xhr.send(params);
}
