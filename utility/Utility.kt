package com.matteblack.utility

import javafx.fxml.FXMLLoader
import java.nio.file.Paths

fun fxLoaderFromResource(path: String): FXMLLoader {
    val fxmlLoader = FXMLLoader().apply {
        location = Paths.get(path).toUri().toURL()
    }

    return fxmlLoader
}