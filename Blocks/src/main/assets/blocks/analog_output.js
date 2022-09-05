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
 * @fileoverview FTC robot blocks related to analog output.
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are defined in vars.js:
// createFieldDropdown
// createNonEditableField
// functionColor

// createAnalogOutputDropdown is no longer generated dynamically in
// HardwareUtil.fetchJavaScriptForHardware() because AnalogOutput was removed in PR 2531.
function createAnalogOutputDropdown() {
  var CHOICES = [
  ];
  return createFieldDropdown(CHOICES);
}

// Functions

Blockly.Blocks['analogOutput_setAnalogOutputVoltage'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createAnalogOutputDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setAnalogOutputVoltage'));
    this.appendValueInput('VOLTAGE') // no type, for compatibility
        .appendField('voltage')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Sets the channel output voltage.');
  }
};

Blockly.JavaScript['analogOutput_setAnalogOutputVoltage'] = function(block) {
  return '// AnalogOutput is no longer supported\n';
};

Blockly.FtcJava['analogOutput_setAnalogOutputVoltage'] = function(block) {
  return '// AnalogOutput is no longer supported\n';
};

Blockly.Blocks['analogOutput_setAnalogOutputVoltage_Number'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createAnalogOutputDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setAnalogOutputVoltage'));
    this.appendValueInput('VOLTAGE').setCheck('Number')
        .appendField('voltage')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Sets the channel output voltage.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'VOLTAGE':
          return 'int';
      }
      return '';
    };
  }
};

Blockly.JavaScript['analogOutput_setAnalogOutputVoltage_Number'] =
    Blockly.JavaScript['analogOutput_setAnalogOutputVoltage'];

Blockly.FtcJava['analogOutput_setAnalogOutputVoltage_Number'] =
    Blockly.FtcJava['analogOutput_setAnalogOutputVoltage'];

Blockly.Blocks['analogOutput_setAnalogOutputFrequency'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createAnalogOutputDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setAnalogOutputFrequency'));
    this.appendValueInput('FREQUENCY') // no type, for compatibility
        .appendField('frequency')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Sets the channel output frequency.');
  }
};

Blockly.JavaScript['analogOutput_setAnalogOutputFrequency'] = function(block) {
  return '// AnalogOutput is no longer supported\n';
};

Blockly.FtcJava['analogOutput_setAnalogOutputFrequency'] = function(block) {
  return '// AnalogOutput is no longer supported\n';
};

Blockly.Blocks['analogOutput_setAnalogOutputFrequency_Number'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createAnalogOutputDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setAnalogOutputFrequency'));
    this.appendValueInput('FREQUENCY').setCheck('Number')
        .appendField('frequency')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Sets the channel output frequency.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'FREQUENCY':
          return 'int';
      }
      return '';
    };
  }
};

Blockly.JavaScript['analogOutput_setAnalogOutputFrequency_Number'] =
    Blockly.JavaScript['analogOutput_setAnalogOutputFrequency'];

Blockly.FtcJava['analogOutput_setAnalogOutputFrequency_Number'] =
    Blockly.FtcJava['analogOutput_setAnalogOutputFrequency'];

Blockly.Blocks['analogOutput_setAnalogOutputMode'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createAnalogOutputDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setAnalogOutputMode'));
    this.appendValueInput('MODE') // no type, for compatibility
        .appendField('mode')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Sets the channel operating mode.');
  }
};

Blockly.JavaScript['analogOutput_setAnalogOutputMode'] = function(block) {
  return '// AnalogOutput is no longer supported\n';
};

Blockly.FtcJava['analogOutput_setAnalogOutputMode'] = function(block) {
  return '// AnalogOutput is no longer supported\n';
};

Blockly.Blocks['analogOutput_setAnalogOutputMode_Number'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createAnalogOutputDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setAnalogOutputMode'));
    this.appendValueInput('MODE').setCheck('Number')
        .appendField('mode')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Sets the channel operating mode.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'MODE':
          return 'byte';
      }
      return '';
    };
  }
};

Blockly.JavaScript['analogOutput_setAnalogOutputMode_Number'] =
    Blockly.JavaScript['analogOutput_setAnalogOutputMode'];

Blockly.FtcJava['analogOutput_setAnalogOutputMode_Number'] =
    Blockly.FtcJava['analogOutput_setAnalogOutputMode'];
