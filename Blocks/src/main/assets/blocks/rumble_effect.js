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
 * @fileoverview FTC robot blocks related to RumbleEffect.
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are generated dynamically in HardwareUtil.fetchJavaScriptForHardware():
// rumbleEffectIdentifierForJavaScript
// The following are defined in vars.js:
// createNonEditableField
// functionColor

// Constructors

Blockly.Blocks['rumbleEffect_createBuilder'] = {
  init: function() {
    this.setOutput(true, 'Gamepad.RumbleEffect.Builder');
    this.appendDummyInput()
        .appendField('new')
        .appendField(createNonEditableField('RumbleEffect.Builder'));
    this.setColour(functionColor);
    this.setTooltip('Creates a new RumbleEffect.Builder object.');
  }
};

Blockly.JavaScript['rumbleEffect_createBuilder'] = function(block) {
  var code = rumbleEffectIdentifierForJavaScript + '.createBuilder()';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['rumbleEffect_createBuilder'] = function(block) {
  var code = 'new Gamepad.RumbleEffect.Builder()';
  Blockly.FtcJava.generateImport_('Gamepad');
  return [code, Blockly.FtcJava.ORDER_NEW];
};

// Functions

Blockly.Blocks['rumbleEffect_addStep'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('RumbleEffect.Builder'))
        .appendField('.')
        .appendField(createNonEditableField('addStep'));
    this.appendValueInput('RUMBLE_EFFECT_BUILDER').setCheck('Gamepad.RumbleEffect.Builder')
        .appendField('rumbleEffectBuilder')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('RUMBLE_1').setCheck('Number')
        .appendField('rumble1')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('RUMBLE_2').setCheck('Number')
        .appendField('rumble2')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('MILLISECONDS').setCheck('Number')
        .appendField('duration')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Add a "step" to this rumble effect. A step basically just means to rumble ' +
        'at a certain power level for a certain duration. By creating a chain of steps, you can ' +
        'create unique effects.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'MILLISECONDS':
          return 'int';
        case 'RUMBLE_1':
        case 'RUMBLE_2':
          return 'double';
      }
      return '';
    };
  }
};

Blockly.JavaScript['rumbleEffect_addStep'] = function(block) {
  var rumbleEffectBuilder = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_EFFECT_BUILDER', Blockly.JavaScript.ORDER_COMMA);
  var rumble1 = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_1', Blockly.JavaScript.ORDER_COMMA);
  var rumble2 = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_2', Blockly.JavaScript.ORDER_COMMA);
  var millis = Blockly.JavaScript.valueToCode(
      block, 'MILLISECONDS', Blockly.JavaScript.ORDER_COMMA);
  return rumbleEffectIdentifierForJavaScript + '.addStep(' + rumbleEffectBuilder + ', ' +
      rumble1 + ', ' + rumble2 + ', ' + millis + ');\n';
};

Blockly.FtcJava['rumbleEffect_addStep'] = function(block) {
  var rumbleEffectBuilder = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_EFFECT_BUILDER', Blockly.FtcJava.ORDER_MEMBER);
  var rumble1 = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_1', Blockly.FtcJava.ORDER_COMMA);
  var rumble2 = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_2', Blockly.FtcJava.ORDER_COMMA);
  var millis = Blockly.FtcJava.valueToCode(
      block, 'MILLISECONDS', Blockly.FtcJava.ORDER_COMMA);
  return rumbleEffectBuilder + '.addStep(' + rumble1 + ', ' + rumble2 + ', ' + millis + ');\n';
};

Blockly.Blocks['rumbleEffect_build'] = {
  init: function() {
    this.setOutput(true, 'Gamepad.RumbleEffect');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('RumbleEffect.Builder'))
        .appendField('.')
        .appendField(createNonEditableField('build'));
    this.appendValueInput('RUMBLE_EFFECT_BUILDER').setCheck('Gamepad.RumbleEffect.Builder')
        .appendField('rumbleEffectBuilder')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(functionColor);
    this.setTooltip('Returns a RumbleEffect object.');
  }
};

Blockly.JavaScript['rumbleEffect_build'] = function(block) {
  var rumbleEffectBuilder = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_EFFECT_BUILDER', Blockly.JavaScript.ORDER_NONE);
  var code = rumbleEffectIdentifierForJavaScript + '.build(' + rumbleEffectBuilder + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['rumbleEffect_build'] = function(block) {
  var rumbleEffectBuilder = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_EFFECT_BUILDER', Blockly.FtcJava.ORDER_MEMBER);
  var code = rumbleEffectBuilder + '.build()';
  return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
};
