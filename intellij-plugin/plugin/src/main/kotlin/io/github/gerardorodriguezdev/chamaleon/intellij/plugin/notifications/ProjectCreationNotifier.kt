package io.github.gerardorodriguezdev.chamaleon.intellij.plugin.notifications

import com.intellij.util.messages.Topic

fun interface ProjectCreationNotifier {
    fun invoke()

    companion object {
        val TOPIC = Topic.create("ProjectCreationTopic", ProjectCreationNotifier::class.java)
    }
}