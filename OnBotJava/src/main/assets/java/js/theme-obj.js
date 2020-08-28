/* ***** BEGIN LICENSE BLOCK *****
 * Distributed under the BSD license:
 *
 * Copyright (c) 2010, Ajax.org B.V.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Ajax.org B.V. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL AJAX.ORG B.V. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */

define("ace/theme/obj", ["require", "exports", "module", "ace/lib/dom"], function (require, exports, module) {
    let colorScheme = env.settings.get('theme')

    exports.cssClass = "ace-obj";
    let colors = {
        dark: { // from ace chaos
            is_dark: true,
            primary_1: 'rgb(22,22,22)',
            primary_2: 'rgb(36, 36, 36)',
            primary_3: 'rgb(48,48,48)',
            active_line_back: 'rgb(51,51,51)',
            comment: 'rgb(153,153,153)',
            comment_doc: 'rgb(153,153,153)',
            comment_doc_tag: 'rgb(153,153,153)',
            constant: 'rgb(30,218,251)',
            constant_builtin: 'rgb(30,218,251)',
            constant_language: 'rgb(253,194,81)',
            constant_library: 'rgb(141,255,10)',
            constant_numeric: 'rgb(88,197,90)',
            cursor: 'rgb(230,225,220)',
            cursor_bor: 'rgb(230,225,220)',
            entity_name_fn: 'rgb(0,174,239)',
            entity_other_attr_name: 'rgb(255,255,137)',
            fn: 'rgb(0,174,239)',
            fold: 'rgb(119,170,255)',
            fold_back: 'rgb(34,34,34)',
            fold_hover: 'rgb(0,0,0)',
            fold_hover_back: 'rgb(51,51,51)',
            fold_widget_hover: 'rgb(119,119,119)',
            gutter: 'rgb(89,89,89)',
            gutter_active_line_back: 'rgb(34,34,34)',
            gutter_back: 'rgb(20,20,20)',
            gutter_bor: 'rgb(40,40,40)',
            gutter_error: 'rgb(0,0,0)',
            gutter_error_back: 'rgb(255,17,0)',
            gutter_warn: 'rgb(0,0,0)',
            gutter_warn_back: 'rgb(255,204,0)',
            heading: 'rgb(88,197,90)',
            indent_guide_bor: 'rgb(34,34,34)',
            invalid_back: 'rgb(153,0,0)',
            invalid: 'rgb(22,22,22)',
            invisible: 'rgb(64,64,64)',
            keyword: 'rgb(51,156,187)',
            keyword_operator: 'rgb(255,48,143)',
            main: 'rgb(230,225,220)',
            main_back: 'rgb(22,22,22)',
            marker_layer_active_line_back: `rgba(0, 0, 0, 0.07)`,
            marker_layer_bracket_bor: 'rgb(252,233,79)',
            marker_layer_selected_word_back: 'rgb(22,22,22)',
            marker_layer_selected_word_bor: 'rgb(22,22,22)',
            marker_layer_selection_back: 'rgb(73,72,54)',
            marker_layer_selection_bor: 'rgb(73,72,54)',
            marker_layer_stack: 'rgb(88,197,90)',
            marker_layer_step_back: 'rgb(198,219,174)',
            print_margin_back: 'rgb(29,29,29)',
            print_margin_bor: 'rgb(85,85,85)',
            regex: 'rgb(255,17,0)',
            string: 'rgb(88,197,90)',
            support: 'rgb(153,153,153)',
            support_fn: 'rgb(0,174,239)',
            support_other: 'rgb(153,153,153)',
            support_constant: 'rgb(153,153,153)',
            meta_tag: 'rgb(190,83,230)',
            variable: 'rgb(153,119,68)',
            variable_parameter: 'rgb(153,119,68)'
        }, light: { // from ace chrome
            is_dark: false,
            primary_1: 'rgb(255,255,255)',
            primary_2: 'rgb(251,251,250)',
            primary_3: 'rgb(245,245,245)',
            active_line_back: `rgb(255, 255, 255)`,
            comment: `rgb(35, 110, 36)`,
            comment_doc: `rgb(35, 110, 36)`,
            comment_doc_tag: `rgb(35, 110, 36)`,
            constant: `rgb(88, 72, 246)`,
            constant_builtin: `rgb(88, 72, 246)`,
            constant_language: `rgb(88, 92, 246)`,
            constant_library: `rgb(6, 150, 14)`,
            constant_numeric: `rgb(0, 0, 205)`,
            cursor: `rgb(0, 0, 0)`,
            cursor_bor: 'rgb(0,0,0)',
            entity_name_fn: `rgb(0, 0, 162)`,
            entity_other_attr_name: `rgb(153, 68, 9)`,
            fn: `rgb(60, 76, 114)`,
            fold: 'rgb(119,170,255)',
            fold_back: `rgb(191, 191, 191)`,
            fold_hover: `rgb(0, 0, 0)`,
            fold_hover_back: `rgb(191, 191, 191)`,
            fold_widget_hover: `rgb(0, 0, 0)`,
            gutter: `rgb(51, 51, 51)`,
            gutter_active_line_back: `rgb(220, 220, 220)`,
            gutter_back: `rgb(255, 255, 255)`,
            gutter_bor: `rgb(235, 235, 235)`,
            gutter_error: `rgb(0, 0, 0)`,
            gutter_error_back: `rgb(153, 0, 0)`,
            gutter_warn: `rgb(0, 0, 0)`,
            gutter_warn_back: `rgb(252, 255, 0)`,
            heading: `rgb(12, 7, 255)`,
            indent_guide_bor: `rgb(191, 191, 191)`,
            invalid: `rgb(255, 255, 255)`,
            invalid_back: `rgb(153, 0, 0)`,
            invisible: `rgb(191, 191, 191)`,
            keyword: `rgb(147, 15, 128)`,
            keyword_operator: `rgb(104, 118, 135)`,
            list: `rgb(185, 6, 144)`,
            main: `rgb(0, 0, 0)`,
            main_back: `rgb(255, 255, 255)`,
            marker_layer_active_line_back: `rgba(0, 0, 0, 0.07)`,
            marker_layer_bracket_bor: `rgb(192, 192, 192)`,
            marker_layer_selected_word_back: `rgb(250, 250, 255)`,
            marker_layer_selected_word_bor: `rgb(200, 200, 250)`,
            marker_layer_selection_back: `rgb(181, 213, 255)`,
            marker_layer_selection_bor: `rgb(181, 213, 255)`,
            marker_layer_stack: `rgb(164, 229, 101)`,
            marker_layer_step_back: `rgb(252, 255, 0)`,
            meta_tag: `rgb(147, 15, 128)`,
            print_margin_back: `rgb(255, 255, 255)`,
            print_margin_bor: `rgb(232, 232, 232)`,
            regex: `rgb(255, 0, 0)`,
            string: `rgb(26, 26, 166)`,
            support: `rgb(109, 121, 222)`,
            support_fn: `rgb(60, 76, 114)`,
            support_constant: `rgb(6, 150, 14)`,
            support_other: `rgb(109, 121, 222)`,
            variable: `rgb(49, 132, 149)`,
            variable_parameter: `rgb(253, 151, 31)`
        }
    }

    let selColor = null;
    if (colorScheme in colors) {
        selColor = colors[colorScheme];
    } else {
        selColor = colors.light;
    }

    exports.isDark = selColor.is_dark;
    exports.color = selColor;

    let theme = {
        gutter: {
            background: selColor.gutter_back,
            color: selColor.gutter,
            border_right: `none`,
            warning: {
                background_image: 'none',
                background: selColor.gutter_warn_back,
                border_left: 'none',
                padding_left: '0',
                color: selColor.gutter_warn
            },
            error: {
                background_position: '-6px center',
                background_image: 'none',
                background: selColor.gutter_error_back,
                border_left: 'none',
                padding_left: '0',
                color: selColor.gutter_error
            },
            gutter_active_line: {
                background_color: selColor.gutter_active_line_back
            }
        },
        print_margin: {
            border_left: `1px solid ${selColor.print_margin_bor}`,
            right: '0',
            background: selColor.print_margin_back
        },
        background_color: selColor.main_back,
        color: selColor.main,
        colors: {
          primary_1: selColor.primary_1,
          primary_2: selColor.primary_2,
          primary_3: selColor.primary_3
        },
        cursor: {
            border_left: `2px solid ${selColor.cursor_bor}`,
            overwrite: {
                border_left: '0',
                border_bottom: `1px solid ${selColor.cursor_bor}`
            },
        },
        marker_layer: {
            selection: {
                background: selColor.marker_layer_selection_back
            },
            step: {
                background: selColor.marker_layer_step_back
            },
            bracket: {
                margin: '-1px 0 0 -1px',
                border: `1px solid ${selColor.marker_layer_bracket_bor}`
            },
            active_line: {
                background: selColor.active_line_back
            },
        },
        invisible: {
            color: selColor.invisible
        },
        keyword: {
            color: selColor.keyword,
            operator: {
                color: selColor.keyword_operator
            },
        },
        constant: {
            color: selColor.constant,
            language: {
                color: selColor.constant_language,
            },
            library: {
                color: selColor.constant_library
            },
            numeric: {
                color: selColor.constant_numeric
            }
        },
        invalid: {
            color: selColor.invalid,
            background_color: selColor.invalid_back,

            deprecated: {
                color: selColor.invalid,
                background_color: selColor.invalid_back
            }
        },
        support: {
            color: selColor.support,
            fn: {
                color: selColor.support_fn
            },
        },
        fn: {
            color: selColor.support_fn
        },
        string: {
            color: selColor.string
        },
        comment: {
            color: selColor.comment,
            font_style: 'italic',
            padding_bottom: '0'
        },
        variable: {
            color: selColor.variable,
            parameter: {
                color: selColor.variable_parameter
            }
        },
        meta_tag: {
            color: selColor.meta_tag
        },
        entity_other_attr_name: {
            color: selColor.entity_other_attr_name,
        },
        markup_underline: {
            text_decoration: 'underline'
        },
        fold_widget: {
            text_align: 'center',
            hover: {
                color: selColor.fold_widget_hover
            },
            start: {
                background: 'none',
                border: 'none',
                box_shadow: 'none',
                after: {
                    content: '▾'
                }
            },
            end: {
                background: 'none',
                border: 'none',
                box_shadow: 'none',
                after: {
                    content: '▴'
                }
            },
            closed: {
                background: 'none',
                border: 'none',
                box_shadow: 'none',
                after: {
                    content: '‣'
                }
            },
        },
        indent_guide: {
            border_right: `1px dotted ${selColor.indent_guide_bor}`,
            margin_right: '-1px'
        },
        fold: {
            background: selColor.fold_back,
            border_radius: '3px',
            color: selColor.fold,
            border: 'none',
            hover: {
                background: selColor.fold_hover_back,
                color: selColor.fold_hover
            }
        }
    };

    exports.cssText = `.ace-obj .ace_gutter {
background: ${theme.gutter.background};\
color:  ${theme.gutter.color};\
border-right: ${theme.gutter.border_right};\
}\
.ace-obj .ace_gutter-cell.ace_warning {\
background-image: ${theme.gutter.warning.background_image};\
background: ${theme.gutter.warning.background};\
border-left: ${theme.gutter.warning.border_left};\
padding-left: ${theme.gutter.warning.padding_left};\
color: ${theme.gutter.warning.color};\
}\
.ace-obj .ace_gutter-cell.ace_error {\
background-position: ${theme.gutter.error.background_position};\
background-image: ${theme.gutter.error.background_image};\
background: ${theme.gutter.error.background};\
border-left: ${theme.gutter.error.border_left};\
padding-left: ${theme.gutter.error.padding_left};\
color: ${theme.gutter.error.color};\
}\
.ace-obj .ace_print-margin {\
border-left: ${theme.print_margin.border_left};\
right: ${theme.print_margin.right};\
background: ${theme.print_margin.background};\
}\
.ace-obj {\
background-color: ${theme.background_color};\
color: ${theme.color};\
}\
.ace-obj .ace_cursor {\
border-left: ${theme.cursor.border_left};\
}\
.ace-obj .ace_cursor.ace_overwrite {\
border-left: ${theme.cursor.overwrite.border_left};\
border-bottom: ${theme.cursor.overwrite.border_bottom};\
}\
.ace-obj .ace_marker-layer .ace_selection {\
background: ${theme.marker_layer.selection.background};\
}\
.ace-obj .ace_marker-layer .ace_step {\
background: ${theme.marker_layer.step.background};\
}\
.ace-obj .ace_marker-layer .ace_bracket {\
margin: ${theme.marker_layer.bracket.margin};\
border: ${theme.marker_layer.bracket.border};\
}\
.ace-obj .ace_marker-layer .ace_active-line {\
background: ${theme.marker_layer.active_line.background};\
}\
.ace-obj .ace_gutter-active-line {\
background-color: ${theme.gutter.gutter_active_line.background_color};\
}\
.ace-obj .ace_invisible {\
color: ${theme.invisible.color};\
}\
.ace-obj .ace_keyword,
.ace-obj .ace-storage {
color: ${theme.keyword.color};\
}\
.ace-obj .ace_keyword.ace_operator {\
color: ${theme.keyword.operator.color};\
}\
.ace-obj .ace_constant {\
color: ${theme.constant.color};\
}\
.ace-obj .ace_constant.ace_language {\
color: ${theme.constant.language.color};\
}\
.ace-obj .ace_constant.ace_library {\
color: ${theme.constant.library.color};\
}\
.ace-obj .ace_constant.ace_numeric {\
color: ${theme.constant.numeric};\
}\
.ace-obj .ace_invalid {\
color: ${theme.invalid.color};\
background-color: ${theme.invalid.background_color};\
}\
.ace-obj .ace_invalid.ace_deprecated {\
color: ${theme.invalid.deprecated.color};\
background-color: ${theme.invalid.deprecated.background_color};\
}\
.ace-obj .ace_support {\
color: ${theme.support.color};\
}\

.ace-obj .ace_support.ace_function {\
color: ${theme.support.fn.color};\
}\
.ace-obj .ace_function {\
color: ${theme.fn.color};\
}\
.ace-obj .ace_string {\
color: ${theme.string.color};\
}\
.ace-obj .ace_comment {\
color: ${theme.comment.color};\
font-style: ${theme.comment.font_style};\
padding-bottom: ${theme.comment.padding_bottom};\
}\
.ace-obj .ace_variable {\
color: ${theme.variable.color};\
}\
.ace-obj .ace_variable.ace_parameter {\\
color: ${theme.variable.parameter.color};\\
}\\
.ace-obj .ace_meta.ace_tag {\
color: ${theme.meta_tag.color};\
}\
.ace-obj .ace_entity.ace_other.ace_attribute-name {\
color: ${theme.entity_other_attr_name.color};\
}\
.ace-obj .ace_markup.ace_underline {\
text-decoration: ${theme.markup_underline.text_decoration};\
}\
.ace-obj .ace_fold-widget {\
text-align: ${theme.fold_widget.text_align};\
}\
.ace-obj .ace_fold-widget:hover {\
color: ${theme.fold_widget.hover.color};\
}\
.ace-obj .ace_fold-widget.ace_start\
{\
background: ${theme.fold_widget.start.background};\
border: ${theme.fold_widget.start.border};\
box-shadow: ${theme.fold_widget.start.box_shadow};\
}\
.ace-obj .ace_fold-widget.ace_end\
{\
background: ${theme.fold_widget.start.background};\\
border: ${theme.fold_widget.start.border};\\
box-shadow: ${theme.fold_widget.start.box_shadow};\\
}\
.ace-obj .ace_fold-widget.ace_closed\
{\
background: ${theme.fold_widget.start.background};\\
border: ${theme.fold_widget.start.border};\\
box-shadow: ${theme.fold_widget.start.box_shadow};\\
}\
.ace-obj .ace_fold-widget.ace_start:after {\
content: ${theme.fold_widget.start.after.content}\
}\
.ace-obj .ace_fold-widget.ace_end:after {\
content: ${theme.fold_widget.end.after.content}\
}\
.ace-obj .ace_fold-widget.ace_closed:after {\
content: ${theme.fold_widget.closed.after.content}\
}\
.ace-obj .ace_indent-guide {\
border-right: ${theme.indent_guide.border_right};\
margin-right: ${theme.indent_guide.margin_right};\
}\
.ace-obj .ace_fold { \
background: ${theme.fold.background}; \
border-radius: ${theme.fold.border_radius}; \
color: ${theme.fold.color}; \
border: ${theme.fold.border}; \
}\
.ace-obj .ace_fold:hover {\
background: ${theme.fold.hover.background}; \
color: ${theme.fold.hover.color};\
}\

.ace-primary-1 {
    background: ${theme.colors.primary_1}
}

.ace-primary-2 {
    background: ${theme.colors.primary_2}
}

.ace-primary-3 {
    background: ${theme.colors.primary_3}
}
`;

    var dom = require("../lib/dom");
    dom.importCssString(exports.cssText, exports.cssClass);

});
(function () {
    window.require(["ace/theme/obj"], function (m) {
        if (typeof module == "object" && typeof exports == "object" && module) {
            module.exports = m;
        }
    });
})();
            