/**
 * @fileoverview FTC robot blocks related to Locale
 * @author lizlooney@google.com (Liz Looney)
 */

// The following are generated dynamically in HardwareUtil.fetchJavaScriptForHardware():
// createLanguageCodeDropdown
// LANGUAGE_CODE_TOOLTIPS
// createCountryCodeDropdown
// COUNTRY_CODE_TOOLTIPS
// The following are defined in vars.js:
// createNonEditableField
// getPropertyColor

Blockly.Blocks['locale_languageCode'] = {
  init: function() {
    this.setOutput(true, 'String');
    this.appendDummyInput()
        .appendField(createNonEditableField('Language'))
        .appendField(createLanguageCodeDropdown(), 'LANGUAGE_CODE');
    this.setColour(getPropertyColor);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    var TOOLTIPS = LANGUAGE_CODE_TOOLTIPS;
    this.setTooltip(function() {
      var key = thisBlock.getFieldValue('LANGUAGE_CODE');
      for (var i = 0; i < TOOLTIPS.length; i++) {
        if (TOOLTIPS[i][0] == key) {
          return TOOLTIPS[i][1];
        }
      }
      return '';
    });
  }
};

Blockly.JavaScript['locale_languageCode'] = function(block) {
  var code = '"' + block.getFieldValue('LANGUAGE_CODE') + '"';
  return [code, Blockly.JavaScript.ORDER_ATOMIC];
};

Blockly.FtcJava['locale_languageCode'] = function(block) {
  // Even in Java, a language code is actually just a string, not an enum.
  var code = '"' + block.getFieldValue('LANGUAGE_CODE') + '"';
  return [code, Blockly.FtcJava.ORDER_ATOMIC];
};

Blockly.Blocks['locale_countryCode'] = {
  init: function() {
    this.setOutput(true, 'String');
    this.appendDummyInput()
        .appendField(createNonEditableField('Country'))
        .appendField(createCountryCodeDropdown(), 'COUNTRY_CODE');
    this.setColour(getPropertyColor);
    // Assign 'this' to a variable for use in the tooltip closure below.
    var thisBlock = this;
    var TOOLTIPS = COUNTRY_CODE_TOOLTIPS;
    this.setTooltip(function() {
      var key = thisBlock.getFieldValue('COUNTRY_CODE');
      for (var i = 0; i < TOOLTIPS.length; i++) {
        if (TOOLTIPS[i][0] == key) {
          return TOOLTIPS[i][1];
        }
      }
      return '';
    });
  }
};

Blockly.JavaScript['locale_countryCode'] = function(block) {
  var code = '"' + block.getFieldValue('COUNTRY_CODE') + '"';
  return [code, Blockly.JavaScript.ORDER_ATOMIC];
};

Blockly.FtcJava['locale_countryCode'] = function(block) {
  // Even in Java, a country code is actually just a string, not an enum.
  var code = '"' + block.getFieldValue('COUNTRY_CODE') + '"';
  return [code, Blockly.FtcJava.ORDER_ATOMIC];
};
