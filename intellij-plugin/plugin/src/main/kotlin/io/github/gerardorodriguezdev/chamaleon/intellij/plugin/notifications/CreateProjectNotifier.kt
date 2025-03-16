package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.notifications

import com.intellij.util.messages.Topic
import io.github.gerardorodriguezdev.chamaleon.core.models.Project

interface CreateProjectNotifier {
    fun createProject(project: Project)

    companion object {
        val TOPIC = Topic.create("CreateProjectTopic", CreateProjectNotifier::class.java)
    }
}