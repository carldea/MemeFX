/*
 * Copyright (c) 2018. Carl Dea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carlfx.meme.view

import com.carlfx.meme.app.Styles.Companion.memeText
import com.carlfx.meme.app.Styles.Companion.memeTextField
import com.carlfx.meme.app.Styles.Companion.resizeCorner
import com.carlfx.meme.app.Styles.Companion.textContainerOff
import com.carlfx.meme.app.Styles.Companion.textContainerOn
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import tornadofx.*

class MemeTextControlView(text:String, var textFontSize:Int): View() {
    var isDragged = false

    override val root: StackPane = StackPane()
        init {

            with(root) {

                addClass(textContainerOff)

                val textFieldPhrase = textfield(text) {
                    addClass(memeTextField)
                    style { fontSize = textFontSize.px }
                }

                val textPhrase = text {
                    addClass(memeText)
                    style { fontSize = textFontSize.px }
                }

                textPhrase.textProperty().bind(textFieldPhrase.textProperty())

                val textLabel = Label(null, textPhrase)

                this += textFieldPhrase
                this += textLabel

                val resizeCornerShape = path {
                    addClass(resizeCorner)
                    moveTo(15, 0.0)
                    lineTo(15.0, 15.0)
                    lineTo(0.0, 15.0)
                    closepath()
                    stackpaneConstraints { alignment = Pos.BOTTOM_RIGHT }
                }

                this += resizeCornerShape

                val focusOn = {
                    requestFocus()
                    resizeCornerShape.isVisible = true
                    styleClass.clear()
                    addClass(textContainerOn)
                }

                val focusOff = {
                    textLabel.isVisible = true
                    textFieldPhrase.isVisible = false
                    resizeCornerShape.isVisible = false
                    styleClass.clear()
                    addClass(textContainerOff)
                }

                val resizeCornerAnchor = SimpleObjectProperty(Point2D(0.0, 0.0))
                val anchorWidth = SimpleDoubleProperty()
                val widthOffset = SimpleDoubleProperty()

                // Update this component based on the size of the Label text.
                textLabel.widthProperty().addListener { _ ->
                    val minWidth = textLabel.boundsInParent.width
                    prefWidth = minWidth
                }

                // Begin resize process safe current width
                resizeCornerShape.setOnMousePressed {
                    val x = it.sceneX
                    val y = it.sceneY
                    val w = boundsInParent.width
                    anchorWidth.set(w)
                    widthOffset.set(0.0)
                    resizeCornerAnchor.set(Point2D(x, y))
                    it.consume()
                }
                // Resize width of this component
                resizeCornerShape.setOnMouseDragged {
                    isDragged = true
                    val x = it.sceneX
                    // val y = mouseEvent.sceneY
                    val length = x - resizeCornerAnchor.get().x
                    val calculatedWidth = anchorWidth.get() + length
                    if (calculatedWidth >= textLabel.boundsInParent.width) {
                        root.prefWidth = calculatedWidth
                    }
                    it.consume()
                }

                // After releasing resize update new width
                resizeCornerShape.setOnMouseReleased {
                    isDragged = false
                    val x = it.sceneX
                    val y = it.sceneY
                    resizeCornerAnchor.set(Point2D(x, y))
                    val w = root.prefWidth
                    anchorWidth.set(w)
                    widthOffset.set(0.0)
                    it.consume()
                }

                val anchor = SimpleObjectProperty(Point2D(0.0, 0.0))

                // Go to view meme mode
                textFieldPhrase.setOnAction {
                    focusOff()
                }

                // Escape key exits edit mode
                textFieldPhrase.addEventHandler(KeyEvent.KEY_PRESSED) {
                    if (it.code == KeyCode.ESCAPE) {
                        focusOff()
                    }
                }

                // Clicking on the scene exits edit mode.
                primaryStage.scene.addEventHandler(MouseEvent.MOUSE_CLICKED) {
                    focusOff()
                }

                // Go to edit meme mode
                setOnMouseClicked {
                    if (it.clickCount == 2) {
                        textLabel.isVisible = false
                        textFieldPhrase.isVisible = true
                        focusOn()
                        Platform.runLater {
                            textFieldPhrase.selectAll()
                            textFieldPhrase.requestFocus()
                        }
                    }
                    it.consume()
                }

                // Focus this component
                setOnMouseEntered {
                    if (!textFieldPhrase.isVisible) {
                        focusOn()
                    }
                }

                // Lose Focus
                setOnMouseExited {
                    if (!textFieldPhrase.isVisible && !isDragged) {
                        focusOff()
                    }
                }

                // Create anchor for drag operation also bring to front.
                setOnMousePressed {
                    this.toFront()
                    val x = it.sceneX - anchor.get().x
                    val y = it.sceneY - anchor.get().y
                    anchor.set(Point2D(x, y))
                }

                // Drag operation to move this component.
                setOnMouseDragged {
                    val x = it.sceneX - anchor.get().x
                    val y = it.sceneY - anchor.get().y
                    layoutX = x
                    layoutY = y
                }

                // Release mouse to update new anchor point
                setOnMouseReleased {
                    val x = it.sceneX - anchor.get().x
                    val y = it.sceneY - anchor.get().y
                    anchor.set(Point2D(x, y))
                }

                // Pressing the delete key will remove this component
                addEventHandler<KeyEvent>(KeyEvent.KEY_PRESSED) {
                    if (it.code == KeyCode.DELETE) {
                        focusOn()
                        val parent = parent as Pane
                        parent.children.remove(this)
                    }
                }

                // Default to off position
                focusOff()
            }
        }
}