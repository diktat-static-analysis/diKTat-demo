package org.cqfn.diktat.demo.frontend.components

import org.cqfn.diktat.demo.frontend.uploadCodeForm
import org.cqfn.diktat.demo.frontend.utils.Ace
import org.cqfn.diktat.demo.views.CodeForm
import org.cqfn.diktat.demo.views.RulesSetTypes

import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.get
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import react.dom.button
import react.dom.div
import react.dom.form
import react.dom.input
import react.dom.label
import react.dom.option
import react.dom.select
import react.dom.span
import react.dom.textArea

import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onSubmitFunction

/**
 * A component for a form, where initial and fixed code are displayed and linter settings are set.
 */
class EditorForm : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
//        val (codeForm, setCodeForm) = useState(CodeForm())
        form {
            div {
                attrs.id = "warnings-pane"
                child(WarningsPane::class) {}
            }
            div("row try-wrapper") {
                span("arrow glyphicon glyphicon-arrow-right") {}
                div("col-md-6") {
                    div("form-group") {
                        div("form-group") {
                            div("ace_editor ace-monokai ace_dark") {
                                attrs.id = "editor"
                            }
                            textArea(classes = "source") {
                                attrs.id = "source"
                                attrs.name = "source"
                                attrs.onSubmitFunction = { it.preventDefault() }
                            }
                        }
                    }
                }
                div("col-md-6") {
                    div("ace_editor ace-monokai ace_dark") {
                        attrs.id = "result"
                    }
                }
            }
            div("row") {
                setProp("align", "center")
                div("col-sm") {
                    input(InputType.checkBox, classes = "form-check-input", name = "check") {
                        attrs.id = "check"
                        attrs.value = "radioChk"
                        attrs.onSubmitFunction = { it.preventDefault() }
                    }
                    label("form-check-label") {
                        attrs.htmlFor = "check"
                        +"Check"
                    }

                    input(InputType.checkBox, classes = "form-check-input", name = "fix") {
                        attrs.id = "fix"
                        attrs.value = "radioChk"
                        attrs.onSubmitFunction = { it.preventDefault() }
                    }
                    label("form-check-label") {
                        attrs.htmlFor = "fix"
                        +"Fix"
                    }
                }
                div("rulSet") {
                    setProp("align", "center")
                    label {
                        +"Choose rules set provider:"
                    }
                    select {
                        attrs.name = "rulSet-select"
                        attrs.id = "rulSet-select"
                        option {
                            +"ktlint"
                        }
                        option {
                            attrs.selected = true
                            +"diKTat"
                        }
                        attrs.onSubmitFunction = { it.preventDefault() }
                    }
                }
            }
            br {}
            div("row") {
                setProp("align", "center")
//                <!--            <div th:if="*{diktatConfigFile != null}">-->
//                <!--                <label> File selected </label>-->
//                <!--            </div>-->
                div("upload-btn-wrapper") {
                    button(classes = "btn") {
                        +"Upload config"
                        input(type = InputType.file, name = "myfile") {
                            attrs.onSubmitFunction = { it.preventDefault() }
                        }
                    }
                }
                div("row") {
                    setProp("align", "center")
                    br {}
                    button(classes = "btn btn-primary", type = ButtonType.submit) {
                        +"Submit"
                            // className(codeFormStore.tracker.map {
                            // if (it) "spinner-border spinner-border-sm mr-2" else ""
                            // })
                    }
                }
            }
            attrs {
                id = "main-form"
                onSubmitFunction = { event: Event ->
                    event.preventDefault()
                    GlobalScope.launch {
                        val form = document.getElementById("main-form") as HTMLFormElement
                        val textarea = document.getElementById("source") as HTMLTextAreaElement
                        val resultSession = Ace.edit("result").getSession()
                        uploadCodeForm("http://localhost:8082/demo",
                            CodeForm(
                                initialCode = textarea.value,
                                check = (form.elements["check"] as HTMLInputElement).checked,
                                fix = (form.elements["fix"] as HTMLInputElement).checked,
                                ruleSet = (form.elements["rulSet-select"] as HTMLSelectElement)
                                    .selectedOptions
                                    .asList()
                                    .map { (it as HTMLOptionElement).value.toUpperCase() }
                                    .map(RulesSetTypes::valueOf)
                            )
                        )
                            .apply {
                                fixedCode?.let { resultSession.setValue(it) }
                            }
                    }
                }
            }
        }
    }
}
