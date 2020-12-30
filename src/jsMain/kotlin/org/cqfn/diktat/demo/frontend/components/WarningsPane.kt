package org.cqfn.diktat.demo.frontend.components

import kotlinx.browser.document
import kotlinx.html.ButtonType
import kotlinx.html.id
import org.cqfn.diktat.demo.frontend.CodeFormProps
import org.cqfn.diktat.demo.views.CodeForm
import react.RBuilder
import react.RComponent
import react.RState
import react.dom.br
import react.dom.button
import react.dom.div

import react.dom.render
import react.dom.span

/**
 * A UI component to display warnings from [CodeForm.warnings]
 */
@Suppress("USE_DATA_CLASS", "EMPTY_BLOCK_STRUCTURE_ERROR")
class WarningsPane : RComponent<CodeFormProps, RState>() {
    override fun RBuilder.render() {
        div("row") {
            div("col-md-12") {
                div("alert alert-danger alert-dismissible") {
                    button(classes = "close", type = ButtonType.button) {
                        attrs {
                            attributes["data-dismiss"] = "alert"
                        }
                        span { +"×" }
                    }
                }
                div {
                    attrs.id = "warnings-list"
                }
            }
        }
    }

    override fun componentDidUpdate(prevProps: CodeFormProps, prevState: RState, snapshot: Any) {
        super.componentDidUpdate(prevProps, prevState, snapshot)
        // todo: this doesn't work now
        render(document.getElementById("warnings-list")) {
            span {
                prevProps.codeForm.warnings?.map {
                    span {
                        +it
                    }
                    br { }
                }
            }
        }
    }
}
