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

import com.carlfx.meme.app.MemeController
import javafx.application.Platform
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Pane
import tornadofx.*

class MainView : View("MemeFX") {

    private val memeController: MemeController by inject()

    val imageView = imageview {
        isPreserveRatio = true
        isSmooth = true
    }

    val progress = progressindicator {
        isVisible = false
        setMaxSize(100.0, 100.0)
    }

    // Meme as a container to hold the image view and Text
    val memeContent: Pane = pane {
        this += imageView
    }

    val fontSizeToggleGroup = ToggleGroup()

    override val root = borderpane {
        anchorpaneConstraints {
            topAnchor = 0.0
            leftAnchor = 0.0
        }

        // Main display area
        center {
            stackpane {
                anchorpane{
                    this += memeContent
                    this += progress
                }
            }
        }
        // Menu bar
        top {
            menubar {
                menu("File") {
                    isMnemonicParsing = true
                    item("Open", "Shortcut+O").action {
                        memeController.openImageFile()
                    }
                    item("Save As", "Shortcut+A").action {
                        memeController.saveImageAs()
                    }
                    item("Print", "Shortcut+P").action {
                        memeController.printMeme()
                    }
                    item("Quit",  "Shortcut+Q").action {
                        Platform.exit()
                    }
                }
                menu("Meme Text") {
                    menu("Font Size") {
                        radiomenuitem("30") {
                            userData = 30
                            toggleGroup = fontSizeToggleGroup
                        }
                        radiomenuitem("40") {
                            userData = 40
                            toggleGroup = fontSizeToggleGroup
                        }
                        radiomenuitem("50") {
                            userData = 50
                            toggleGroup = fontSizeToggleGroup
                        }
                        radiomenuitem("60") {
                            userData = 60
                            toggleGroup = fontSizeToggleGroup
                        }
                        radiomenuitem("70") {
                            userData = 70
                            toggleGroup = fontSizeToggleGroup
                        }
                        radiomenuitem("80") {
                            userData = 80
                            toggleGroup = fontSizeToggleGroup
                        }
                        radiomenuitem("90") {
                            userData = 90
                            toggleGroup = fontSizeToggleGroup
                        }
                    }
                    item("Add Meme Text").action {
                        memeController.addMemeText()
                    }
                }
            }
        }
    }
}