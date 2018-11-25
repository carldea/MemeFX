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
package com.carlfx.meme.app

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val memeText by cssclass()
        val memeTextField by cssclass()
        val textContainerOn by cssclass()
        val textContainerOff by cssclass()
        val resizeCorner by cssclass()
    }

    init {
        loadFont("/Anton.ttf", 14)!!

        memeText {
            fontFamily = "Anton"
            fill = c("#ffffff")
            stroke = c("black")
            strokeWidth = Dimension(2.0, Dimension.LinearUnits.px)
        }

        memeTextField {
            fontFamily = "Anton"
            fill = c("white")
            textFill = c("white")
            stroke = c("black")
            strokeWidth = Dimension(2.0, Dimension.LinearUnits.px)
            backgroundColor = multi(c("#00000000"))
            minWidth = 200.0.px
        }

        textContainerOn {
            borderColor += box(Color.ORANGE)
            backgroundColor = multi(c(0,0,0, .20))
            borderWidth = multi(box(.5.px))
            cursor = Cursor.HAND
        }

        textContainerOff {
            borderColor += box(Color.TRANSPARENT)
            backgroundColor = multi(c(0,0,0, 0.0))
            borderWidth = multi(box(.5.px))
        }

        resizeCorner {
            strokeWidth = 0.0.px
            fill = LinearGradient(0.0, 0.0, 1.0, 1.0, true,
                    CycleMethod.NO_CYCLE,
                    Stop(0.0, Color.WHITE),
                    Stop(.5, Color.ORANGE),
                    Stop(1.0, Color.ORANGERED))
            cursor = Cursor.W_RESIZE
        }
    }
}
