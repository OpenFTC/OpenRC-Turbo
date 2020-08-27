/**
 * @license
 * Copyright 2016 Google LLC
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
 * @fileoverview FTC robot blocks related to DbgLog.
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are generated dynamically in HardwareUtil.fetchJavaScriptForHardware():
// dbgLogIdentifierForJavaScript
// The following are defined in vars.js:
// functionColor

// Functions

Blockly.Blocks['dbgLog_msg'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('DbgLog'))
        .appendField('.')
        .appendField(createNonEditableField('msg'));
    this.appendValueInput('MESSAGE').setCheck('String')
        .appendField('message')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Log a debug message.');
  }
};

Blockly.JavaScript['dbgLog_msg'] = function(block) {
  var message = Blockly.JavaScript.valueToCode(
      block, 'MESSAGE', Blockly.JavaScript.ORDER_COMMA);
  return dbgLogIdentifierForJavaScript + '.msg(' + message + ');\n';
};

Blockly.FtcJava['dbgLog_msg'] = function(block) {
  var message = Blockly.FtcJava.valueToCode(
      block, 'MESSAGE', Blockly.FtcJava.ORDER_COMMA);
  Blockly.FtcJava.generateImport_('RobotLog');
  return 'RobotLog.ii("DbgLog", ' + message + ');\n';
};

Blockly.Blocks['dbgLog_error'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('DbgLog'))
        .appendField('.')
        .appendField(createNonEditableField('error'));
    this.appendValueInput('MESSAGE').setCheck('String')
        .appendField('message')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Log a debug message.');
  }
};

Blockly.JavaScript['dbgLog_error'] = function(block) {
  var message = Blockly.JavaScript.valueToCode(
      block, 'MESSAGE', Blockly.JavaScript.ORDER_COMMA);
  return dbgLogIdentifierForJavaScript + '.error(' + message + ');\n';
};

Blockly.FtcJava['dbgLog_error'] = function(block) {
  var message = Blockly.FtcJava.valueToCode(
      block, 'MESSAGE', Blockly.FtcJava.ORDER_COMMA);
  Blockly.FtcJava.generateImport_('RobotLog');
  return 'RobotLog.ee("DbgLog", ' + message + ');\n';
};
