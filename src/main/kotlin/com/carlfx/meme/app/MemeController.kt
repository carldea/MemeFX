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

import com.carlfx.meme.view.MainView
import com.carlfx.meme.view.MemeTextControlView
import javafx.beans.InvalidationListener
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.print.PrinterJob
import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.input.TransferMode
import javafx.stage.FileChooser
import tornadofx.*
import java.io.IOException
import java.net.MalformedURLException
import java.util.*
import java.util.concurrent.ExecutionException
import javax.imageio.ImageIO

class MemeController: Controller() {
    private val mainView: MainView by inject()
    private val memeViewModel: MemeViewModel by inject()

    init {
        mainView.fontSizeToggleGroup
                .selectedToggleProperty()
                .addListener(InvalidationListener {
            if (mainView.fontSizeToggleGroup.selectedToggle != null) {
                memeViewModel.fontSize.set(mainView.fontSizeToggleGroup.selectedToggle.userData as Int)
            }
        })

        // When nodes are visible they can be repositioned.
        primaryStage.setOnShown {
            val scene = primaryStage.scene
            val repositionProgressIndicator = {
                // update progress x
                mainView.progress.translateX = scene.width / 2 - mainView.progress.width / 2
                mainView.progress.translateY = scene.height / 2 - mainView.progress.height / 2
            }

            scene.widthProperty().addListener { observable -> repositionProgressIndicator() }
            scene.heightProperty().addListener { observable -> repositionProgressIndicator() }

            mainView.imageView.fitWidthProperty()
                    .bind(scene.widthProperty())

            setupDragNDrop()

            repositionProgressIndicator()

        }
    }

    fun openImageFile() {
        val fileChooser = FileChooser()
        val imageFile = fileChooser.showOpenDialog(primaryStage)
        if (imageFile != null) {
            try {
                val url = imageFile.toURI().toURL().toString()
                if (isValidImageFile(url)) {
                    loadAndDisplayImage(url)
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Returns true if URL's file extensions match jpg, jpeg,
     * png, gif and bmp.
     * @param url standard URL path to image file.
     * @return Boolean returns true if URL's extension matches
     * jpg, jpeg, png, bmp and gif.
     */
    fun isValidImageFile(url: String?): Boolean {
        val imgTypes = Arrays.asList(".jpg", ".jpeg",
                ".png", ".gif", ".bmp")
        return imgTypes.stream()
                .anyMatch { t -> url!!.toLowerCase().endsWith(t) }
    }

    fun saveImageAs() {
        val fileChooser = FileChooser()
        val fileSave = fileChooser.showSaveDialog(primaryStage)
        if (fileSave != null) {

            val image = mainView.memeContent
                    .snapshot(SnapshotParameters(), null)
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null),
                        "png", fileSave)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Creates a task to load an image in the background. During
     * the load process the progress indicator is displayed. Once
     * the image is successfully loaded the image will be displayed.
     * Also, various image attributes will be applied to the
     * current image view node such as rotation and color adjustments.
     */
    private fun loadAndDisplayImage(url: String?) {

        // show spinner while image is loading
        mainView.progress.isVisible = true

        //String urlStr = String.valueOf(_currentViewImage.getUserData());
        val loadImage = createWorker(url)

        // after loading has succeeded apply image info
        loadImage.setOnSucceeded { workerStateEvent ->
            try {
                mainView.imageView.image = loadImage.get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } finally {
                // hide progress indicator
                mainView.progress.isVisible = false
            }
        }

        // any failure turn off spinner
        loadImage.setOnFailed { workerStateEvent ->
            mainView.progress.isVisible = false
        }

        runAsync {
            loadImage.run()
        }
    }

    /**
     * Returns a worker task (Task) which will off-load the image
     * on a separate thread when finished; the current image will
     * be displayed on the JavaFX application thread.
     * @param imageUrl ImageInfo instance containing a url string
     * representation of the path to the image file.
     * The imageInfo also has the degrees in rotation.
     * @return Task worker task to load image and display into ImageView
     * control.
     */
    private fun createWorker(imageUrl: String?): Task<Image> {
        return object : Task<Image>() {
            @Throws(Exception::class)
            override fun call(): Image {
                // On the worker thread...
                return Image(imageUrl!!, false)
            }
        }
    }

    fun printMeme() {
        val job = PrinterJob.createPrinterJob()
        job!!.jobStatusProperty().addListener { listener -> println("status " + job.jobStatus) }
        if (job != null && job.showPrintDialog(mainView.primaryStage)) {
            if (job.jobStatus == PrinterJob.JobStatus.NOT_STARTED) {
                println("canceled")
            }
            val success = job.printPage(mainView.memeContent)
            if (success) {
                job.endJob()
            }
        }
    }

    fun addMemeText() {
        memeViewModel.memeCount.set(memeViewModel.memeCount.get() + 1)
        mainView.memeContent += MemeTextControlView("MEME TEXT " + memeViewModel.memeCount.get(),
                memeViewModel.fontSize.get())
    }


    /**
     * Sets up the drag and drop capability for files and URLs to be
     * dragged and dropped onto the scene. This will load the image into
     * the current image view area.
     */
    private fun setupDragNDrop() {
        val scene = primaryStage.scene

        // Dragging over surface
        scene.setOnDragOver {
            val db = it.dragboard
            if (db.hasFiles()
                 || db.hasUrl()
                 && isValidImageFile(db.url)) {
                it.acceptTransferModes(TransferMode.LINK)
            } else {
                it.consume()
            }
        }

        // Dropping over surface
        scene.setOnDragDropped {

            val db = it.dragboard

            var file: String? = null
            // image from the local file system.
            if (db.hasFiles() && !db.hasUrl()) {
                try {
                    file = db.files[0]
                            .toURI()
                            .toURL()
                            .toString()
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }

            } else {
                file = db.url
            }
            //LOGGER.log(Level.FINE, "dropped file: " + file!!)
            if (!isValidImageFile(file)) {
                openImageFile()
            } else {
                loadAndDisplayImage(file.toString())
            }

            it.isDropCompleted = true
            it.consume()
        }
    }
}