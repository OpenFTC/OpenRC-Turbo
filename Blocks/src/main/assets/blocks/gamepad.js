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
 * @fileoverview FTC robot blocks related to game pads.
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are defined in vars.js:
// getPropertyColor
// functionColor

function createGamepadDropdown() {
  var CHOICES = [
      ['gamepad1', 'gamepad1'],
      ['gamepad2', 'gamepad2'],
      ];
  return new Blockly.FieldDropdown(CHOICES);
}

Blockly.Blocks['gamepad_getProperty'] = {
  init: function() {
    var PROPERTY_CHOICES = [
        ['A', 'A'],
        ['AtRest', 'AtRest'],
        ['B', 'B'],
        ['Back', 'Back'],
        ['Circle', 'Circle'],
        ['Cross', 'Cross'],
        ['DpadDown', 'DpadDown'],
        ['DpadLeft', 'DpadLeft'],
        ['DpadRight', 'DpadRight'],
        ['DpadUp', 'DpadUp'],
        ['Guide', 'Guide'],
        ['LeftBumper', 'LeftBumper'],
        ['LeftStickButton', 'LeftStickButton'],
        ['LeftStickX', 'LeftStickX'],
        ['LeftStickY', 'LeftStickY'],
        ['LeftTrigger', 'LeftTrigger'],
        ['Options', 'Options'],
        ['PS', 'PS'],
        ['RightBumper', 'RightBumper'],
        ['RightStickButton', 'RightStickButton'],
        ['RightStickX', 'RightStickX'],
        ['RightStickY', 'RightStickY'],
        ['RightTrigger', 'RightTrigger'],
        ['Share', 'Share'],
        ['Square', 'Square'],
        ['Start', 'Start'],
        ['Touchpad', 'Touchpad'],
        ['TouchpadFinger1', 'TouchpadFinger1'],
        ['TouchpadFinger2', 'TouchpadFinger2'],
        ['TouchpadFinger1X', 'TouchpadFinger1X'],
        ['TouchpadFinger1Y', 'TouchpadFinger1Y'],
        ['TouchpadFinger2X', 'TouchpadFinger2X'],
        ['TouchpadFinger2Y', 'TouchpadFinger2Y'],
        ['Triangle', 'Triangle'],
        ['X', 'X'],
        ['Y', 'Y'],
    ];
    this.setOutput(true); // no type, for compatibility
    this.appendDummyInput()
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(new Blockly.FieldDropdown(PROPERTY_CHOICES), 'PROP');
    this.setColour(getPropertyColor);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    var TOOLTIPS = [
        ['A', 'Returns true if the A button is pressed.'],
        ['AtRest', 'Returns true if all analog sticks and triggers are in their rest position.'],
        ['B', 'Returns true if the B button is pressed.'],
        ['Back', 'Returns true if the Back button is pressed.'],
        ['Circle', 'Returns true if the Circle button is pressed.'],
        ['Cross', 'Returns true if the Cross button is pressed.'],
        ['DpadDown', 'Returns true if the dpad down button is pressed.'],
        ['DpadLeft', 'Returns true if the dpad left button is pressed.'],
        ['DpadRight', 'Returns true if the dpad right button is pressed.'],
        ['DpadUp', 'Returns true if the dpad up button is pressed.'],
        ['Guide', 'Returns true if the Guide button is pressed. The Guide button is often the large button in the middle of the controller.'],
        ['LeftBumper', 'Returns true if the left bumper is pressed.'],
        ['LeftStickButton', 'Returns true if the left stick button is pressed.'],
        ['LeftStickX', 'Returns a numeric value between -1.0 and +1.0 representing the left analog stick horizontal axis value.'],
        ['LeftStickY', 'Returns a numeric value between -1.0 and +1.0 representing the left analog stick vertical axis value.'],
        ['LeftTrigger', 'Returns a numeric value between 0.0 and +1.0 representing the left trigger value.'],
        ['Options', 'Returns true if the Options button is pressed.'],
        ['PS', 'Returns true if the PS button is pressed.'],
        ['RightBumper', 'Returns true if the right bumper is pressed.'],
        ['RightStickButton', 'Returns true if the right stick button is pressed.'],
        ['RightStickX', 'Returns a numeric value between -1.0 and +1.0 representing the right analog stick horizontal axis value.'],
        ['RightStickY', 'Returns a numeric value between -1.0 and +1.0 representing the right analog stick vertical axis value .'],
        ['RightTrigger', 'Returns a numeric value between 0.0 and +1.0 representing the right trigger value.'],
        ['Share', 'Returns true if the Share button is pressed.'],
        ['Square', 'Returns true if the Square button is pressed.'],
        ['Start', 'Returns true if the Start button is pressed.'],
        ['Touchpad', 'Returns true if the Touchpad button is pressed.'],
        ['TouchpadFinger1', 'Returns true if the Touchpad is tracking finger ID # 1.'],
        ['TouchpadFinger2', 'Returns true if the Touchpad is tracking finger ID # 2.'],
        ['TouchpadFinger1X', 'Returns  a numeric value between -1.0 and +1.0 representing the horizontal axis value.'],
        ['TouchpadFinger1Y', 'Returns  a numeric value between -1.0 and +1.0 representing the vertical axis value.'],
        ['TouchpadFinger2X', 'Returns  a numeric value between -1.0 and +1.0 representing the horizontal axis value.'],
        ['TouchpadFinger2Y', 'Returns  a numeric value between -1.0 and +1.0 representing the vertical axis value.'],
        ['Triangle', 'Returns true if the Triangle button is pressed.'],
        ['X', 'Returns true if the X button is pressed.'],
        ['Y', 'Returns true if the Y button is pressed.'],
    ];
    this.setTooltip(function() {
      var key = thisBlock.getFieldValue('PROP');
      for (var i = 0; i < TOOLTIPS.length; i++) {
        if (TOOLTIPS[i][0] == key) {
          return TOOLTIPS[i][1];
        }
      }
      return '';
    });
  }
};

Blockly.JavaScript['gamepad_getProperty'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var property = block.getFieldValue('PROP');
  var code = identifier + '.get' + property + '()';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['gamepad_getProperty'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var property = block.getFieldValue('PROP');
  var code;
  switch (property) {
    case 'A':
      code = 'a';
      break;
    case 'AtRest':
      code = 'atRest()';
      break;
    case 'B':
      code = 'b';
      break;
    case 'Back':
      code = 'back';
      break;
    case 'Circle':
      code = 'circle';
      break;
    case 'Cross':
      code = 'cross';
      break;
    case 'DpadDown':
      code = 'dpad_down';
      break;
    case 'DpadLeft':
      code = 'dpad_left';
      break;
    case 'DpadRight':
      code = 'dpad_right';
      break;
    case 'DpadUp':
      code = 'dpad_up';
      break;
    case 'Guide':
      code = 'guide';
      break;
    case 'LeftBumper':
      code = 'left_bumper';
      break;
    case 'LeftStickButton':
      code = 'left_stick_button';
      break;
    case 'LeftStickX':
      code = 'left_stick_x';
      break;
    case 'LeftStickY':
      code = 'left_stick_y';
      break;
    case 'LeftTrigger':
      code = 'left_trigger';
      break;
    case 'Options':
      code = 'options';
      break;
    case 'PS':
      code = 'ps';
      break;
    case 'RightBumper':
      code = 'right_bumper';
      break;
    case 'RightStickButton':
      code = 'right_stick_button';
      break;
    case 'RightStickX':
      code = 'right_stick_x';
      break;
    case 'RightStickY':
      code = 'right_stick_y';
      break;
    case 'RightTrigger':
      code = 'right_trigger';
      break;
    case 'Share':
      code = 'share';
      break;
    case 'Square':
      code = 'square';
      break;
    case 'Start':
      code = 'start';
      break;
    case 'Touchpad':
      code = 'touchpad';
      break;
    case 'TouchpadFinger1':
      code = 'touchpad_finger_1';
      break;
    case 'TouchpadFinger2':
      code = 'touchpad_finger_2';
      break;
    case 'TouchpadFinger1X':
      code = 'touchpad_finger_1_x';
      break;
    case 'TouchpadFinger1Y':
      code = 'touchpad_finger_1_y';
      break;
    case 'TouchpadFinger2X':
      code = 'touchpad_finger_2_x';
      break;
    case 'TouchpadFinger2Y':
      code = 'touchpad_finger_2_y';
      break;
    case 'Triangle':
      code = 'triangle';
      break;
    case 'X':
      code = 'x';
      break;
    case 'Y':
      code = 'y';
      break;
    default:
      throw 'Unexpected property ' + property + ' (gamepad_getProperty).';
  }
  var code = identifier + '.' + code;
  if (code.endsWith(')')) { // atRest() is a method.
    return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
  }
  return [code, Blockly.FtcJava.ORDER_MEMBER];
};

Blockly.Blocks['gamepad_getProperty_Boolean'] = {
  init: function() {
    var PROPERTY_CHOICES = [
        ['A', 'A'],
        ['AtRest', 'AtRest'],
        ['B', 'B'],
        ['Back', 'Back'],
        ['Circle', 'Circle'],
        ['Cross', 'Cross'],
        ['DpadDown', 'DpadDown'],
        ['DpadLeft', 'DpadLeft'],
        ['DpadRight', 'DpadRight'],
        ['DpadUp', 'DpadUp'],
        ['Guide', 'Guide'],
        ['LeftBumper', 'LeftBumper'],
        ['LeftStickButton', 'LeftStickButton'],
        ['Options', 'Options'],
        ['PS', 'PS'],
        ['RightBumper', 'RightBumper'],
        ['RightStickButton', 'RightStickButton'],
        ['Share', 'Share'],
        ['Square', 'Square'],
        ['Start', 'Start'],
        ['Touchpad', 'Touchpad'],
        ['TouchpadFinger1', 'TouchpadFinger1'],
        ['TouchpadFinger2', 'TouchpadFinger2'],
        ['Triangle', 'Triangle'],
        ['X', 'X'],
        ['Y', 'Y'],
    ];
    this.setOutput(true, 'Boolean');
    this.appendDummyInput()
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(new Blockly.FieldDropdown(PROPERTY_CHOICES), 'PROP');
    this.setColour(getPropertyColor);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    var TOOLTIPS = [
        ['A', 'Returns true if the A button is pressed.'],
        ['AtRest', 'Returns true if all analog sticks and triggers are in their rest position.'],
        ['B', 'Returns true if the B button is pressed.'],
        ['Back', 'Returns true if the Back button is pressed.'],
        ['Circle', 'Returns true if the Circle button is pressed.'],
        ['Cross', 'Returns true if the Cross button is pressed.'],
        ['DpadDown', 'Returns true if the dpad down button is pressed.'],
        ['DpadLeft', 'Returns true if the dpad left button is pressed.'],
        ['DpadRight', 'Returns true if the dpad right button is pressed.'],
        ['DpadUp', 'Returns true if the dpad up button is pressed.'],
        ['Guide', 'Returns true if the Guide button is pressed. The Guide button is often the large button in the middle of the controller.'],
        ['LeftBumper', 'Returns true if the left bumper is pressed.'],
        ['LeftStickButton', 'Returns true if the left stick button is pressed.'],
        ['Options', 'Returns true if the Options button is pressed.'],
        ['PS', 'Returns true if the PS button is pressed.'],
        ['RightBumper', 'Returns true if the right bumper is pressed.'],
        ['RightStickButton', 'Returns true if the right stick button is pressed.'],
        ['Share', 'Returns true if the Share button is pressed.'],
        ['Square', 'Returns true if the Square button is pressed.'],
        ['Start', 'Returns true if the Start button is pressed.'],
        ['Touchpad', 'Returns true if the Touchpad button is pressed.'],
        ['TouchpadFinger1', 'Returns true if the Touchpad is tracking finger ID # 1.'],
        ['TouchpadFinger2', 'Returns true if the Touchpad is tracking finger ID # 2.'],
        ['Triangle', 'Returns true if the Triangle button is pressed.'],
        ['X', 'Returns true if the X button is pressed.'],
        ['Y', 'Returns true if the Y button is pressed.'],
    ];
    this.setTooltip(function() {
      var key = thisBlock.getFieldValue('PROP');
      for (var i = 0; i < TOOLTIPS.length; i++) {
        if (TOOLTIPS[i][0] == key) {
          return TOOLTIPS[i][1];
        }
      }
      return '';
    });
  }
};

Blockly.JavaScript['gamepad_getProperty_Boolean'] =
    Blockly.JavaScript['gamepad_getProperty'];

Blockly.FtcJava['gamepad_getProperty_Boolean'] =
    Blockly.FtcJava['gamepad_getProperty'];


Blockly.Blocks['gamepad_getProperty_Number'] = {
  init: function() {
    var PROPERTY_CHOICES = [
        ['LeftStickX', 'LeftStickX'],
        ['LeftStickY', 'LeftStickY'],
        ['LeftTrigger', 'LeftTrigger'],
        ['RightStickX', 'RightStickX'],
        ['RightStickY', 'RightStickY'],
        ['RightTrigger', 'RightTrigger'],
        ['TouchpadFinger1X', 'TouchpadFinger1X'],
        ['TouchpadFinger1Y', 'TouchpadFinger1Y'],
        ['TouchpadFinger2X', 'TouchpadFinger2X'],
        ['TouchpadFinger2Y', 'TouchpadFinger2Y'],
    ];
    this.setOutput(true, 'Number');
    this.appendDummyInput()
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(new Blockly.FieldDropdown(PROPERTY_CHOICES), 'PROP');
    this.setColour(getPropertyColor);
    // Assign 'this' to a variable for use in the closures below.
    var thisBlock = this;
    var TOOLTIPS = [
        ['LeftStickX', 'Returns a numeric value between -1.0 and +1.0 representing the left analog stick horizontal axis value.'],
        ['LeftStickY', 'Returns a numeric value between -1.0 and +1.0 representing the left analog stick vertical axis value.'],
        ['LeftTrigger', 'Returns a numeric value between 0.0 and +1.0 representing the left trigger value.'],
        ['RightStickX', 'Returns a numeric value between -1.0 and +1.0 representing the right analog stick horizontal axis value.'],
        ['RightStickY', 'Returns a numeric value between -1.0 and +1.0 representing the right analog stick vertical axis value .'],
        ['RightTrigger', 'Returns a numeric value between 0.0 and +1.0 representing the right trigger value.'],
        ['TouchpadFinger1X', 'Returns  a numeric value between -1.0 and +1.0 representing the horizontal axis value.'],
        ['TouchpadFinger1Y', 'Returns  a numeric value between -1.0 and +1.0 representing the vertical axis value.'],
        ['TouchpadFinger2X', 'Returns  a numeric value between -1.0 and +1.0 representing the horizontal axis value.'],
        ['TouchpadFinger2Y', 'Returns  a numeric value between -1.0 and +1.0 representing the vertical axis value.'],
    ];
    this.setTooltip(function() {
      var key = thisBlock.getFieldValue('PROP');
      for (var i = 0; i < TOOLTIPS.length; i++) {
        if (TOOLTIPS[i][0] == key) {
          return TOOLTIPS[i][1];
        }
      }
      return '';
    });
    this.getFtcJavaOutputType = function() {
      var property = thisBlock.getFieldValue('PROP');
      switch (property) {
        case 'LeftStickX':
        case 'LeftStickY':
        case 'LeftTrigger':
        case 'RightStickX':
        case 'RightStickY':
        case 'RightTrigger':
        case 'TouchpadFinger1X':
        case 'TouchpadFinger1Y':
        case 'TouchpadFinger2X':
        case 'TouchpadFinger2Y':
          return 'float';
        default:
          throw 'Unexpected property ' + property + ' (gamepad_getProperty_Number getOutputType).';
      }
    };
  }
};

Blockly.JavaScript['gamepad_getProperty_Number'] =
    Blockly.JavaScript['gamepad_getProperty'];

Blockly.FtcJava['gamepad_getProperty_Number'] =
    Blockly.FtcJava['gamepad_getProperty'];

Blockly.Blocks['gamepad_rumble_with1'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('rumble'));
    this.appendValueInput('MILLISECONDS').setCheck('Number')
        .appendField('duration')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Rumble the gamepad\'s first rumble motor at maximum power for the given amount of milliseconds.');
    this.getFtcJavaInputType = function(inputName) {
      if (inputName == 'MILLISECONDS') {
        return 'int';
      }
      return '';
    };
  }
};

Blockly.JavaScript['gamepad_rumble_with1'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var millis = Blockly.JavaScript.valueToCode(
      block, 'MILLISECONDS', Blockly.JavaScript.ORDER_NONE);
  return identifier + '.rumble_with1(' + millis + ');\n';
};

Blockly.FtcJava['gamepad_rumble_with1'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var millis = Blockly.FtcJava.valueToCode(
      block, 'MILLISECONDS', Blockly.FtcJava.ORDER_NONE);
  return identifier + '.rumble(' + millis + ');\n';
};

Blockly.Blocks['gamepad_rumble_with3'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('rumble'));
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
    this.setTooltip('Rumble the gamepad at a fixed rumble power for the given amount of milliseconds.');
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

Blockly.JavaScript['gamepad_rumble_with3'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var rumble1 = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_1', Blockly.JavaScript.ORDER_COMMA);
  var rumble2 = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_2', Blockly.JavaScript.ORDER_COMMA);
  var millis = Blockly.JavaScript.valueToCode(
      block, 'MILLISECONDS', Blockly.JavaScript.ORDER_COMMA);
  return identifier + '.rumble_with3(' + rumble1 + ', ' + rumble2 + ', ' + millis + ');\n';
};

Blockly.FtcJava['gamepad_rumble_with3'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var rumble1 = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_1', Blockly.FtcJava.ORDER_COMMA);
  var rumble2 = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_2', Blockly.FtcJava.ORDER_COMMA);
  var millis = Blockly.FtcJava.valueToCode(
      block, 'MILLISECONDS', Blockly.FtcJava.ORDER_COMMA);
  return identifier + '.rumble(' + rumble1 + ', ' + rumble2 + ', ' + millis + ');\n';
};

Blockly.Blocks['gamepad_stopRumble'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('stopRumble'));
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Cancel the currently running rumble effect, if any.');
  }
};

Blockly.JavaScript['gamepad_stopRumble'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  return identifier + '.stopRumble();\n';
};

Blockly.FtcJava['gamepad_stopRumble'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  return identifier + '.stopRumble();\n';
};

Blockly.Blocks['gamepad_rumbleBlips'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('rumbleBlips'));
    this.appendValueInput('COUNT').setCheck('Number')
        .appendField('count')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Rumble the gamepad for a certain number of "blips" using predetermined blip timing.');
    this.getFtcJavaInputType = function(inputName) {
      if (inputName == 'COUNT') {
        return 'int';
      }
      return '';
    };
  }
};

Blockly.JavaScript['gamepad_rumbleBlips'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var count = Blockly.JavaScript.valueToCode(
      block, 'COUNT', Blockly.JavaScript.ORDER_NONE);
  return identifier + '.rumbleBlips(' + count + ');\n';
};

Blockly.FtcJava['gamepad_rumbleBlips'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var count = Blockly.FtcJava.valueToCode(
      block, 'COUNT', Blockly.FtcJava.ORDER_NONE);
  return identifier + '.rumbleBlips(' + count + ');\n';
};

Blockly.Blocks['gamepad_isRumbling'] = {
  init: function() {
    this.setOutput(true, 'Boolean');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('isRumbling'));
    this.setColour(functionColor);
    this.setTooltip('Returns an educated guess about whether there is a rumble ' +
        'action ongoing on this gamepad.');
  }
};

Blockly.JavaScript['gamepad_isRumbling'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var code = identifier + '.isRumbling()';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['gamepad_isRumbling'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var code = identifier + '.isRumbling()';
  return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
};

Blockly.Blocks['gamepad_runRumbleEffect'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('runRumbleEffect'));
    this.appendValueInput('RUMBLE_EFFECT').setCheck('Gamepad.RumbleEffect')
        .appendField('rumbleEffect')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Run a rumble effect.');
  }
};

Blockly.JavaScript['gamepad_runRumbleEffect'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var rumbleEffect = Blockly.JavaScript.valueToCode(
      block, 'RUMBLE_EFFECT', Blockly.JavaScript.ORDER_NONE);
  return identifier + '.runRumbleEffect(' + rumbleEffect + ');\n';
};

Blockly.FtcJava['gamepad_runRumbleEffect'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var rumbleEffect = Blockly.FtcJava.valueToCode(
      block, 'RUMBLE_EFFECT', Blockly.FtcJava.ORDER_MEMBER);
  return identifier + '.runRumbleEffect(' + rumbleEffect + ');\n';
};

Blockly.Blocks['gamepad_RUMBLE_DURATION_CONTINUOUS'] = {
  init: function() {
    this.setOutput(true, 'Number');
    this.appendDummyInput()
        .appendField(createNonEditableField('Gamepad'))
        .appendField('.')
        .appendField(createNonEditableField('RUMBLE_DURATION_CONTINUOUS'));
    this.setColour(getPropertyColor);
    this.setTooltip('Duration indicating continuous rumbling.');
    this.getFtcJavaOutputType = function() {
      return 'int';
    };
  }
};

Blockly.JavaScript['gamepad_RUMBLE_DURATION_CONTINUOUS'] = function(block) {
  return ['-1', Blockly.JavaScript.ORDER_UNARY_NEGATION];
};

Blockly.FtcJava['gamepad_RUMBLE_DURATION_CONTINUOUS'] = function(block) {
  var code = 'Gamepad.RUMBLE_DURATION_CONTINUOUS';
  Blockly.FtcJava.generateImport_('Gamepad');
  return [code, Blockly.FtcJava.ORDER_MEMBER];
};

// LED Effect

Blockly.Blocks['gamepad_setLedColor'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('setLedColor'));
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
    this.setTooltip('Set the LED to a certain color (r,g,b) for a certain duration.');
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

Blockly.JavaScript['gamepad_setLedColor'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var red = Blockly.JavaScript.valueToCode(
      block, 'RED', Blockly.JavaScript.ORDER_COMMA);
  var green = Blockly.JavaScript.valueToCode(
      block, 'GREEN', Blockly.JavaScript.ORDER_COMMA);
  var blue = Blockly.JavaScript.valueToCode(
      block, 'BLUE', Blockly.JavaScript.ORDER_COMMA);
  var millis = Blockly.JavaScript.valueToCode(
      block, 'MILLISECONDS', Blockly.JavaScript.ORDER_COMMA);
  return identifier + '.setLedColor(' +
      red + ', ' + green + ', ' + blue + ', ' + millis + ');\n';
};

Blockly.FtcJava['gamepad_setLedColor'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var red = Blockly.FtcJava.valueToCode(
      block, 'RED', Blockly.FtcJava.ORDER_COMMA);
  var green = Blockly.FtcJava.valueToCode(
      block, 'GREEN', Blockly.FtcJava.ORDER_COMMA);
  var blue = Blockly.FtcJava.valueToCode(
      block, 'BLUE', Blockly.FtcJava.ORDER_COMMA);
  var millis = Blockly.FtcJava.valueToCode(
      block, 'MILLISECONDS', Blockly.FtcJava.ORDER_COMMA);
  return identifier + '.setLedColor(' + red + ', ' + green + ', ' + blue + ', ' + millis + ');\n';
};

Blockly.Blocks['gamepad_runLedEffect'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call')
        .appendField(createGamepadDropdown(), 'IDENTIFIER')
        .appendField('.')
        .appendField(createNonEditableField('runLedEffect'));
    this.appendValueInput('LED_EFFECT').setCheck('Gamepad.LedEffect')
        .appendField('ledEffect')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    this.setTooltip('Run an led effect.');
  }
};

Blockly.JavaScript['gamepad_runLedEffect'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var ledEffect = Blockly.JavaScript.valueToCode(
      block, 'LED_EFFECT', Blockly.JavaScript.ORDER_NONE);
  return identifier + '.runLedEffect(' + ledEffect + ');\n';
};

Blockly.FtcJava['gamepad_runLedEffect'] = function(block) {
  var identifier = block.getFieldValue('IDENTIFIER');
  var ledEffect = Blockly.FtcJava.valueToCode(
      block, 'LED_EFFECT', Blockly.FtcJava.ORDER_MEMBER);
  return identifier + '.runLedEffect(' + ledEffect + ');\n';
};

Blockly.Blocks['gamepad_LED_DURATION_CONTINUOUS'] = {
  init: function() {
    this.setOutput(true, 'Number');
    this.appendDummyInput()
        .appendField(createNonEditableField('Gamepad'))
        .appendField('.')
        .appendField(createNonEditableField('LED_DURATION_CONTINUOUS'));
    this.setColour(getPropertyColor);
    this.setTooltip('Duration indicating continuous LED effect.');
    this.getFtcJavaOutputType = function() {
      return 'int';
    };
  }
};

Blockly.JavaScript['gamepad_LED_DURATION_CONTINUOUS'] = function(block) {
  return ['-1', Blockly.JavaScript.ORDER_UNARY_NEGATION];
};

Blockly.FtcJava['gamepad_LED_DURATION_CONTINUOUS'] = function(block) {
  var code = 'Gamepad.LED_DURATION_CONTINUOUS';
  Blockly.FtcJava.generateImport_('Gamepad');
  return [code, Blockly.FtcJava.ORDER_MEMBER];
};
