package com.technokratos.simpledemo.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.util.logging.Logger

fun Project.log(text: String) {
    Logger.getLogger(name).warning(text)
}

fun Project.showNotification(
    content: String,
    type: NotificationType,
) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("Demo notification group")
        .createNotification(content, type)
        .notify(this)
}

fun Project.showInfoMessage(
    title: String = "Info title",
    message: String = "Info message",
) {
    Messages.showMessageDialog(
        this,
        message,
        title,
        Messages.getInformationIcon()
    )
}