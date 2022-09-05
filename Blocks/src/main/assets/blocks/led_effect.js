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
 * @fileoverview FTC robot blocks related to LedEffect.
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are generated dynamically in HardwareUtil.fetchJavaScriptForHardware():
// ledEffectIdentifierForJavaScript
// The following are defined in vars.js:
// createNonEditableField
// functionColor

// Constructors

Blockly.Blocks['ledEffect_createBuilder'] = {
  init: function() {
    this.setOutput(true, 'Gamepad.LedEffect.Builder');
    this.appendDummyInput()
        .appendField('new')
        .appendField(createNonEditableField('LedEffect.Builder'));
    this.setColour(functionColor);
    this.setTooltip('Creates a new LedEffect.Builder object.');
  }
};

Blockly.JavaScript['ledEffect_createBuilder'] = function(block) {
  var code = ledEffectIdentifierForJavaScript + '.createBuilder()';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['ledEffect_createBuilder'] = function(block) {
  var code = 'new Gamepad.LedEffect.Builder()';
  Blockly.FtcJava.generateImport_('Gamepad');
  return [code, Blockly.FtcJava.ORDER_NEW];
};

// Functions

Blockly.Blocks['ledEffect_addStep'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('LedEffect.Builder'))
        .appendField('.')
        .appendField(createNonEditableField('addStep'));
    this.appendValueInput('LED_EFFECT_BUILDER').setCheck('Gamepad.LedEffect.Builder')
        .appendField('ledEffectBuilder')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('RED').setCheck('Number')
        .appendField('red')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('GREEN').setCheck('Number')
        .appendField('green')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('BLUE').setCheck('Number')
        .appendField('blue')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('MILLISECONDS').setCheck('Number')
        .appendField('duration')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Add a "step" to this LED effect. A step basically just means to set ' +
        'the LED to a certain color (r,g,b) for a certain duration. By creating a chain of ' +
        'steps, you can create unique effects.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'MILLISECONDS':
          return 'int';
        case 'RED':
        case 'GREEN':
        case 'BLUE':
          return 'double';
      }
      return '';
    };
  }
};

Blockly.JavaScript['ledEffect_addStep'] = function(block) {
  var ledEffectBuilder = Blockly.JavaScript.valueToCode(
      block, 'LED_EFFECT_BUILDER', Blockly.JavaScript.ORDER_COMMA);
  var red = Blockly.JavaScript.valueToCode(
      block, 'RED', Blockly.JavaScript.ORDER_COMMA);
  var green = Blockly.JavaScript.valueToCode(
      block, 'GREEN', Blockly.JavaScript.ORDER_COMMA);
  var blue = Blockly.JavaScript.valueToCode(
      block, 'BLUE', Blockly.JavaScript.ORDER_COMMA);
  var millis = Blockly.JavaScript.valueToCode(
      block, 'MILLISECONDS', Blockly.JavaScript.ORDER_COMMA);
  return ledEffectIdentifierForJavaScript + '.addStep(' + ledEffectBuilder + ', ' +
      red + ', ' + green + ', ' + blue + ', ' + millis + ');\n';
};

Blockly.FtcJava['ledEffect_addStep'] = function(block) {
  var ledEffectBuilder = Blockly.FtcJava.valueToCode(
      block, 'LED_EFFECT_BUILDER', Blockly.FtcJava.ORDER_MEMBER);
  var red = Blockly.FtcJava.valueToCode(
      block, 'RED', Blockly.FtcJava.ORDER_COMMA);
  var green = Blockly.FtcJava.valueToCode(
      block, 'GREEN', Blockly.FtcJava.ORDER_COMMA);
  var blue = Blockly.FtcJava.valueToCode(
      block, 'BLUE', Blockly.FtcJava.ORDER_COMMA);
  var millis = Blockly.FtcJava.valueToCode(
      block, 'MILLISECONDS', Blockly.FtcJava.ORDER_COMMA);
  return ledEffectBuilder + '.addStep(' + red + ', ' + green + ', ' + blue + ', ' + millis + ');\n';
};

Blockly.Blocks['ledEffect_setRepeating'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('LedEffect.Builder'))
        .appendField('.')
        .appendField(createNonEditableField('setRepeating'));
    this.appendValueInput('LED_EFFECT_BUILDER').setCheck('Gamepad.LedEffect.Builder')
        .appendField('ledEffectBuilder')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('REPEATING').setCheck('Boolean')
        .appendField('repeating')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Set whether this LED effect should loop after finishing, ' +
        'unless the LED is otherwise commanded differently.');
  }
};

Blockly.JavaScript['ledEffect_setRepeating'] = function(block) {
  var ledEffectBuilder = Blockly.JavaScript.valueToCode(
      block, 'LED_EFFECT_BUILDER', Blockly.JavaScript.ORDER_COMMA);
  var repeating = Blockly.JavaScript.valueToCode(
      block, 'REPEATING', Blockly.JavaScript.ORDER_COMMA);
  return ledEffectIdentifierForJavaScript + '.setRepeating(' + ledEffectBuilder + ', ' +
      repeating + ');\n';
};

Blockly.FtcJava['ledEffect_setRepeating'] = function(block) {
  var ledEffectBuilder = Blockly.FtcJava.valueToCode(
      block, 'LED_EFFECT_BUILDER', Blockly.FtcJava.ORDER_MEMBER);
  var repeating = Blockly.FtcJava.valueToCode(
      block, 'REPEATING', Blockly.FtcJava.ORDER_NONE);
  return ledEffectBuilder + '.setRepeating(' + repeating + ');\n';
};

Blockly.Blocks['ledEffect_build'] = {
  init: function() {
    this.setOutput(true, 'Gamepad.LedEffect');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('LedEffect.Builder'))
        .appendField('.')
        .appendField(createNonEditableField('build'));
    this.appendValueInput('LED_EFFECT_BUILDER').setCheck('Gamepad.LedEffect.Builder')
        .appendField('ledEffectBuilder')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(functionColor);
    this.setTooltip('Returns an LedEffect object.');
  }
};

Blockly.JavaScript['ledEffect_build'] = function(block) {
  var ledEffectBuilder = Blockly.JavaScript.valueToCode(
      block, 'LED_EFFECT_BUILDER', Blockly.JavaScript.ORDER_NONE);
  var code = ledEffectIdentifierForJavaScript + '.build(' + ledEffectBuilder + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['ledEffect_build'] = function(block) {
  var ledEffectBuilder = Blockly.FtcJava.valueToCode(
      block, 'LED_EFFECT_BUILDER', Blockly.FtcJava.ORDER_MEMBER);
  var code = ledEffectBuilder + '.build()';
  return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
};
