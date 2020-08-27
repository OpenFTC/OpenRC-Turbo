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
 * @fileoverview Miscellaneous blocks.
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are generated dynamically in HardwareUtil.fetchJavaScriptForHardware():
// miscIdentifierForJavaScript
// The following are defined in vars.js:
// commentColor
// functionColor
// knownTypeToClassName

Blockly.Blocks['comment'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldTextInput(''), 'COMMENT');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(commentColor);
  }
};

Blockly.JavaScript['comment'] = function(block) {
  return '';
};

Blockly.FtcJava['comment'] = function(block) {
  return '// ' + block.getFieldValue('COMMENT') + '\n';
};

Blockly.Blocks['misc_null'] = {
  init: function() {
    this.setOutput(true); // no type for null
    this.appendDummyInput()
        .appendField(createNonEditableField('null'));
    this.setColour(functionColor);
    this.setTooltip('null');
  }
};

Blockly.JavaScript['misc_null'] = function(block) {
  var code = miscIdentifierForJavaScript + '.getNull()';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['misc_null'] = function(block) {
  return ['null', Blockly.FtcJava.ORDER_ATOMIC];
};

Blockly.Blocks['misc_isNull'] = {
  init: function() {
    this.setOutput(true, 'Boolean');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('isNull'));
    this.appendValueInput('VALUE') // all types allowed
        .appendField('value')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(functionColor);
    this.setTooltip('Returns true if the given value is null, false otherwise.');
  }
};

Blockly.JavaScript['misc_isNull'] = function(block) {
  var value = Blockly.JavaScript.valueToCode(
      block, 'VALUE', Blockly.JavaScript.ORDER_NONE);
  var code = miscIdentifierForJavaScript + '.isNull(' + value + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['misc_isNull'] = function(block) {
  var value = Blockly.FtcJava.valueToCode(
      block, 'VALUE', Blockly.FtcJava.ORDER_EQUALITY);
  var code = value + ' == null';
  return [code, Blockly.FtcJava.ORDER_EQUALITY];
};

Blockly.Blocks['misc_isNotNull'] = {
  init: function() {
    this.setOutput(true, 'Boolean');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('isNotNull'));
    this.appendValueInput('VALUE') // all types allowed
        .appendField('value')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(functionColor);
    this.setTooltip('Returns true if the given value is not null, false otherwise.');
  }
};

Blockly.JavaScript['misc_isNotNull'] = function(block) {
  var value = Blockly.JavaScript.valueToCode(
      block, 'VALUE', Blockly.JavaScript.ORDER_NONE);
  var code = miscIdentifierForJavaScript + '.isNotNull(' + value + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['misc_isNotNull'] = function(block) {
  var value = Blockly.FtcJava.valueToCode(
      block, 'VALUE', Blockly.FtcJava.ORDER_EQUALITY);
  var code = value + ' != null';
  return [code, Blockly.FtcJava.ORDER_EQUALITY];
};

Blockly.Blocks['misc_atan2'] = {
  init: function() {
    this.setOutput(true, 'Number');
    this.appendDummyInput()
        .appendField('atan2');
    this.appendValueInput('Y').setCheck('Number')
        .appendField('y')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('X').setCheck('Number')
        .appendField('x')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(Blockly.Msg.MATH_HUE);
    this.setTooltip('Returns a numerical value between -180 and +180 degrees, representing ' +
        'the counterclockwise angle between the positive X axis, and the point (x, y).');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'Y':
        case 'X':
          return 'double';
      }
      return '';
    };
    this.getFtcJavaOutputType = function() {
      return 'double';
    };
  }
};

Blockly.JavaScript['misc_atan2'] = function(block) {
  var y = Blockly.JavaScript.valueToCode(
      block, 'Y', Blockly.JavaScript.ORDER_COMMA);
  var x = Blockly.JavaScript.valueToCode(
      block, 'X', Blockly.JavaScript.ORDER_COMMA);
  var code = 'Math.atan2(' + y + ', ' + x + ') / Math.PI * 180';
  return [code, Blockly.JavaScript.ORDER_DIVISION];
};

Blockly.FtcJava['misc_atan2'] = function(block) {
  var y = Blockly.FtcJava.valueToCode(
      block, 'Y', Blockly.FtcJava.ORDER_COMMA);
  var x = Blockly.FtcJava.valueToCode(
      block, 'X', Blockly.FtcJava.ORDER_COMMA);
  var code = 'Math.atan2(' + y + ', ' + x + ') / Math.PI * 180';
  return [code, Blockly.FtcJava.ORDER_MULTIPLICATION];
};

Blockly.Blocks['misc_formatNumber'] = {
  init: function() {
    this.setOutput(true, 'String');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('formatNumber'));
    this.appendValueInput('NUMBER').setCheck('Number')
        .appendField('number')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('PRECISION').setCheck('Number')
        .appendField('precision')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(functionColor);
    this.setTooltip('Returns a text value of the given number formatted with the given precision, padded with zeros if necessary.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'NUMBER':
          return 'double';
        case 'PRECISION':
          return 'int';
      }
      return '';
    };
  }
};

Blockly.JavaScript['misc_formatNumber'] = function(block) {
  var number = Blockly.JavaScript.valueToCode(
      block, 'NUMBER', Blockly.JavaScript.ORDER_COMMA);
  var precision = Blockly.JavaScript.valueToCode(
      block, 'PRECISION', Blockly.JavaScript.ORDER_COMMA);
  var code = miscIdentifierForJavaScript + '.formatNumber(' + number + ', ' + precision + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['misc_formatNumber'] = function(block) {
  var number = Blockly.FtcJava.valueToCode(
      block, 'NUMBER', Blockly.FtcJava.ORDER_COMMA);
  var precision = Blockly.FtcJava.valueToCode(
      block, 'PRECISION', Blockly.FtcJava.ORDER_COMMA);
  // Due to issues with floating point precision, we always call the JavaUtil method.
  Blockly.FtcJava.generateImport_('JavaUtil');
  var code = 'JavaUtil.formatNumber(' + number + ', ' + precision + ')';
  return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
};

Blockly.Blocks['misc_roundDecimal'] = {
  init: function() {
    this.setOutput(true, 'Number');
    this.appendDummyInput()
        .appendField('call')
        .appendField(createNonEditableField('roundDecimal'));
    this.appendValueInput('NUMBER').setCheck('Number')
        .appendField('number')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.appendValueInput('PRECISION').setCheck('Number')
        .appendField('precision')
        .setAlign(Blockly.ALIGN_RIGHT);
    this.setColour(functionColor);
    this.setTooltip('Returns a numeric value of the given number rounded to the given precision.');
    this.getFtcJavaInputType = function(inputName) {
      switch (inputName) {
        case 'NUMBER':
          return 'double';
        case 'PRECISION':
          return 'int';
      }
      return '';
    };
  }
};

Blockly.JavaScript['misc_roundDecimal'] = function(block) {
  var number = Blockly.JavaScript.valueToCode(
      block, 'NUMBER', Blockly.JavaScript.ORDER_COMMA);
  var precision = Blockly.JavaScript.valueToCode(
      block, 'PRECISION', Blockly.JavaScript.ORDER_COMMA);
  var code = miscIdentifierForJavaScript + '.roundDecimal(' + number + ', ' + precision + ')';
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.FtcJava['misc_roundDecimal'] = function(block) {
  var number = Blockly.FtcJava.valueToCode(
      block, 'NUMBER', Blockly.FtcJava.ORDER_COMMA);
  var precision = Blockly.FtcJava.valueToCode(
      block, 'PRECISION', Blockly.FtcJava.ORDER_COMMA);
  // Due to issues with floating point precision, we always call the JavaUtil method.
  Blockly.FtcJava.generateImport_('JavaUtil');
  var code = 'Double.parseDouble(JavaUtil.formatNumber(' + number + ', ' + precision + '))';
  return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
};

Blockly.Blocks['misc_addItemToList'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('add item');
    this.appendValueInput('ITEM');
    this.appendDummyInput()
        .appendField('to list');
    this.appendValueInput('LIST')
        .setCheck('Array');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(Blockly.Msg.LISTS_HUE);
    this.setTooltip('Add the item to the end of the list.');
  }
};

Blockly.JavaScript['misc_addItemToList'] = function(block) {
  var item = Blockly.JavaScript.valueToCode(
      block, 'ITEM', Blockly.JavaScript.ORDER_NONE);
  var list = Blockly.JavaScript.valueToCode(
      block, 'LIST', Blockly.JavaScript.ORDER_MEMBER);
  return list + '.push(' + item + ');\n';
};

Blockly.FtcJava['misc_addItemToList'] = function(block) {
  var item = Blockly.FtcJava.valueToCode(
      block, 'ITEM', Blockly.FtcJava.ORDER_NONE);
  var list = Blockly.FtcJava.valueToCode(
      block, 'LIST', Blockly.FtcJava.ORDER_MEMBER);
  return list + '.add(' + item + ');\n';
};

function callJava_mutationToDom(block) {
  var xmlElement = Blockly.utils.xml.createElement('mutation');
  xmlElement.setAttribute('methodLookupString', block.methodLookupString_);
  xmlElement.setAttribute('parameterCount', block.parameterCount_);
  xmlElement.setAttribute('returnType', block.returnType_);
  xmlElement.setAttribute('comment', block.comment_);
  xmlElement.setAttribute('tooltip', block.tooltip_);
  xmlElement.setAttribute('accessMethod', block.accessMethod_);
  xmlElement.setAttribute('convertReturnValue', block.convertReturnValue_);
  for (var i = 0; i < block.parameterCount_; i++) {
    xmlElement.setAttribute('argLabel' + i, block.argLabels_[i]);
    xmlElement.setAttribute('argType' + i, block.argTypes_[i]);
    xmlElement.setAttribute('argAuto' + i, block.argAutos_[i]);
  }
  return xmlElement;
}

function callJava_domToMutation(block, xmlElement) {
  block.methodLookupString_ = xmlElement.getAttribute('methodLookupString');
  block.parameterCount_ = parseInt(xmlElement.getAttribute('parameterCount'), 10);
  block.returnType_ = xmlElement.getAttribute('returnType');
  block.comment_ = xmlElement.getAttribute('comment');
  block.tooltip_ = xmlElement.getAttribute('tooltip');
  block.accessMethod_ = xmlElement.getAttribute('accessMethod');
  block.convertReturnValue_ = xmlElement.getAttribute('convertReturnValue');
  // When the PR was first created, accessMethod and convertReturnValue were not saved in the block.
  if (!block.accessMethod_) {
    figureOutAccessMethodAndConvertReturnValue(block);
  }

  // Update the block.
  if (block.returnType_ != 'void') {
    var outputCheck = classTypeToCheck(block.returnType_, false);
    if (outputCheck) {
      if (block.outputConnection) {
        block.outputConnection.setCheck(outputCheck)
      }
    }
    block.outputType_ = classTypeToFtcJavaInputOutputType(block.returnType_, outputCheck, false);
    block.getFtcJavaOutputType = function() {
      return block.outputType_;
    }
  }
  // Add argument sockets.
  block.argLabels_ = [];
  block.argTypes_ = [];
  block.argAutos_ = [];
  block.inputTypes_ = [];
  for (var i = 0; i < block.parameterCount_; i++) {
    block.argLabels_[i] = xmlElement.getAttribute('argLabel' + i);
    block.argTypes_[i] = xmlElement.getAttribute('argType' + i);
    block.argAutos_[i] = xmlElement.getAttribute('argAuto' + i);
    var inputCheck = classTypeToCheck(block.argTypes_[i], true);
    block.inputTypes_[i] = classTypeToFtcJavaInputOutputType(block.argTypes_[i], inputCheck, true);
    if (block.argAutos_[i]) {
      // No socket if parameter is provided automatically.
    } else {
      var input = block.appendValueInput('ARG' + i);
      if (inputCheck) {
        input.setCheck(inputCheck);
      }
      var label = block.argLabels_[i]
          ? block.argLabels_[i]
          : classTypeToLabel(block.argTypes_[i], true);
      input.appendField(label)
          .setAlign(Blockly.ALIGN_RIGHT);
    }
  }
  // Add a comment for the method's javadoc comment.
  if (block.comment_) {
    block.setCommentText(block.comment_);
  }
  block.getFtcJavaInputType = function(inputName) {
    if (inputName.startsWith('ARG')) {
      var i = parseInt(inputName.substr(3), 10);
      return block.inputTypes_[i];
    }
    return ''; // This shouldn't happen.
  };
}

function figureOutAccessMethodAndConvertReturnValue(block) {
  block.accessMethod_ = 'callJava';
  block.convertReturnValue_ = '';
  switch (block.returnType_) {
    case 'boolean':
    case 'java.lang.Boolean':
      block.accessMethod_ += '_boolean';
      break;
    case 'char':
    case 'java.lang.Character':
    case 'java.lang.String':
      block.accessMethod_ += '_String';
      break;
    case 'byte':
    case 'java.lang.Byte':
    case 'short':
    case 'java.lang.Short':
    case 'int':
    case 'java.lang.Integer':
    case 'long':
    case 'java.lang.Long':
    case 'float':
    case 'java.lang.Float':
    case 'double':
    case 'java.lang.Double':
      block.accessMethod_ += '_String';
      block.convertReturnValue_ = 'Number';
      break;
  }
}

function classTypeToCheck(classType, input) {
  if (classType.startsWith('[')) {
    return classType;
  }

  switch (classType) {
    // No checking for java.lang.Object.
    case 'java.lang.Object':
      return false;

    // Types that are provided automatically.
    case 'com.qualcomm.robotcore.eventloop.opmode.LinearOpMode':
    case 'com.qualcomm.robotcore.eventloop.opmode.OpMode':
    case 'com.qualcomm.robotcore.hardware.HardwareMap':
    case 'org.firstinspires.ftc.robotcore.external.Telemetry':
      return 'ProvidedAutomatically';

    // Java primitives and wrappers
    case 'boolean':
    case 'java.lang.Boolean':
      return 'Boolean';
    case 'char':
    case 'java.lang.Character':
    case 'java.lang.String':
      return 'String';
    case 'byte':
    case 'java.lang.Byte':
    case 'short':
    case 'java.lang.Short':
    case 'int':
    case 'java.lang.Integer':
    case 'long':
    case 'java.lang.Long':
    case 'float':
    case 'java.lang.Float':
    case 'double':
    case 'java.lang.Double':
    case 'java.lang.Number':
      return 'Number';
  }

  // If it's a type that's already used in blocks, use that type.
  var type = classType;
  var lastDot = type.lastIndexOf('.');
  if (lastDot != -1) {
    type = type.substr(lastDot + 1);
  }
  type = type.replace(/\$/g, '.'); // Change any $ to . (for inner classes).
  var knownJavaClassName = knownTypeToClassName(type);
  if (knownJavaClassName) {
    if (type == 'MatrixF' && input) {
      // Special case: A MatrixF parameter can accept a MatrixF or an OpenGLMatrix.
      return ['MatrixF', 'OpenGLMatrix'];
    }
    return type;
  }

  return classType;
}

function classTypeToFtcJavaInputOutputType(classType, check, input) {
  if (check == 'Number') {
    switch (classType) {
      case 'byte':
      case 'short':
      case 'int':
      case 'long':
      case 'float':
      case 'double':
        return classType;
    }
  }
  if (input && Array.isArray(check) &&
      check.length == 2 &&
      check.includes('MatrixF') &&
      check.includes('OpenGLMatrix')) {
    return 'MatrixF';
  }

  return classTypeToLabel(classType, true);
}

function classTypeToLabel(classType, removePackage) {
  if (classType.charAt(0) == '[') {
    return arrayTypeToLabel(classType, removePackage);
  }

  var label = classType;
  if (removePackage) {
    var lastDot = label.lastIndexOf('.');
    if (lastDot != -1) {
      label = label.substr(lastDot + 1);
    }
  }
  return label.replace(/\$/g, '.'); // Change any $ to . (for inner classes).
}

function arrayTypeToLabel(arrayType, removePackage) {
  // if arrayType is [I, label is int[]
  // if arrayType is [[J, label is long[][]
  // if arrayType is [Ljava.lang.Object;, label is java.lang.Object[]
  var elementType;
  switch (arrayType.charAt(1)) {
    case '[':
      elementType = arrayTypeToLabel(arrayType.substring(1), removePackage)
      break;
    case 'L':
      elementType = arrayType.substring(2);
      if (elementType.endsWith(';')) {
        elementType = elementType.substring(0, elementType.length - 1);
      }
      elementType = classTypeToLabel(elementType, removePackage);
      break;
    case 'Z':
      elementType = 'boolean';
      break;
    case 'B':
      elementType = 'byte';
      break;
    case 'C':
      elementType = 'char';
      break;
    case 'D':
      elementType = 'double';
      break;
    case 'F':
      elementType = 'float';
      break;
    case 'I':
      elementType = 'int';
      break;
    case 'J':
      elementType = 'long';
      break;
    case 'S':
      elementType = 'short';
      break;
  }
  return elementType + '[]';
}

Blockly.Blocks['misc_callJava_return'] = {
  init: function() {
    this.setOutput(true);
    this.appendDummyInput()
        .appendField('call Java method');
    this.appendDummyInput()
        .appendField(createNonEditableField(''), 'CLASS_NAME')
        .appendField('.')
        .appendField(createNonEditableField(''), 'METHOD_NAME');
    this.setColour(functionColor);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      if (thisBlock.tooltip_) {
        return thisBlock.tooltip_;
      } else {
        var className = thisBlock.getFieldValue('CLASS_NAME');
        var methodName = thisBlock.getFieldValue('METHOD_NAME');
        return 'Calls the Java method named ' + methodName +
            ' from the class named org.firstinspires.ftc.teamcode.' + className + '.' +
            ' The Java method\'s return type is ' + classTypeToLabel(thisBlock.returnType_, false) + '.';
      }
    });
  },
  mutationToDom: function() {
    return callJava_mutationToDom(this);
  },
  domToMutation: function(xmlElement) {
    callJava_domToMutation(this, xmlElement);
  },
};

function generateJavaScriptCallJava(block) {
  var delimiter = ',\n' + Blockly.JavaScript.INDENT + Blockly.JavaScript.INDENT;
  var code = 'callJava(' + miscIdentifierForJavaScript + delimiter +
      '"' + block.returnType_ + '"' + delimiter +
      '"' + block.accessMethod_ + '"' + delimiter +
      '"' + block.convertReturnValue_ + '"' + delimiter +
      '"' + block.methodLookupString_ + '"';
  for (var i = 0; i < block.parameterCount_; i++) {
    code += delimiter;
    code += (block.argAutos_[i]
        ? 'null'
        : (Blockly.JavaScript.valueToCode(block, 'ARG' + i, Blockly.JavaScript.ORDER_COMMA) || 'null'));
  }
  code += ')';
  return code;
}

Blockly.JavaScript['misc_callJava_return'] = function(block) {
  var code = generateJavaScriptCallJava(block);
  return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

function generateFtcJavaCallJava(block) {
  if (block.returnType_ != 'void') {
    generateFtcImport(block.returnType_);
  }
  for (var i = 0; i < block.parameterCount_; i++) {
    generateFtcImport(block.argTypes_[i]);
  }

  var className = block.getFieldValue('CLASS_NAME');
  var methodName = block.getFieldValue('METHOD_NAME');
  var code = className + '.' + methodName + '(';
  var delimiter = '';
  var order = (block.parameterCount_ == 1) ? Blockly.FtcJava.ORDER_NONE : Blockly.FtcJava.ORDER_COMMA;
  for (var i = 0; i < block.parameterCount_; i++) {
    code += delimiter;
    code += (block.argAutos_[i]
        ? block.argAutos_[i]
        : (Blockly.FtcJava.valueToCode(block, 'ARG' + i, order) || 'null'));
    delimiter = ', ';
  }
  code += ')';
  return code;
}

function generateFtcImport(classType) {
  if (classType.includes('.')) {
    // Check for array of class.
    if (classType.match(/^\[*L.*\;$/)) {
      var lastBracket = classType.indexOf('[L');
      var semiColon = classType.indexOf(';');
      generateFtcImport(classType.substring(lastBracket + 2, semiColon));
    } else {
      var classToImport = classType;
      // For inner classes, import the outer class.
      if (classToImport.includes('$')) {
        classToImport = classToImport.substring(0, classToImport.indexOf('$'));
      }
      // Don't import classes in the java.lang package.
      if (classToImport.startsWith('java.lang.') && classToImport.lastIndexOf('.') == 9) {
        return;
      }
      Blockly.FtcJava.generateImportForJavaClass_(classToImport);
    }
  }
}

Blockly.FtcJava['misc_callJava_return'] = function(block) {
  var code = generateFtcJavaCallJava(block);
  return [code, Blockly.FtcJava.ORDER_FUNCTION_CALL];
};

Blockly.Blocks['misc_callJava_noReturn'] = {
  init: function() {
    this.appendDummyInput()
        .appendField('call Java method');
    this.appendDummyInput()
        .appendField(createNonEditableField(''), 'CLASS_NAME')
        .appendField('.')
        .appendField(createNonEditableField(''), 'METHOD_NAME');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(functionColor);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    this.setTooltip(function() {
      if (thisBlock.tooltip_) {
        return thisBlock.tooltip_;
      } else {
        var className = thisBlock.getFieldValue('CLASS_NAME');
        var methodName = thisBlock.getFieldValue('METHOD_NAME');
        return 'Calls the Java method named ' + methodName +
            ' from the class named org.firstinspires.ftc.teamcode.' + className + '.';
      }
    });
  },
  mutationToDom: function() {
    return callJava_mutationToDom(this);
  },
  domToMutation: function(xmlElement) {
    callJava_domToMutation(this, xmlElement);
  },
};

Blockly.JavaScript['misc_callJava_noReturn'] = function(block) {
  return generateJavaScriptCallJava(block) + ';\n';
};

Blockly.FtcJava['misc_callJava_noReturn'] = function(block) {
  return generateFtcJavaCallJava(block) + ';\n';
};
