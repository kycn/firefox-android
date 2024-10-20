/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.service.nimbus.messaging

import androidx.annotation.VisibleForTesting

/**
 * A data class that holds a representation of GleanPlum message from Nimbus.
 *
 * @param id identifies a message as unique.
 * @param data Data information provided from Nimbus.
 * @param action A strings that represents which action should be performed
 * after a message is clicked.
 * @param style Indicates how a message should be styled.
 * @param triggers A list of strings corresponding to targeting expressions. The message
 * will be shown if all expressions `true`.
 * @param metadata Metadata that help to identify if a message should shown.
 */
data class Message(
    val id: String,
    internal val data: MessageData,
    internal val action: String,
    internal val style: StyleData,
    internal val triggers: List<String>,
    internal val metadata: Metadata,
) {
    val text: String
        get() = data.text

    val title: String?
        get() = data.title

    val buttonLabel: String?
        get() = data.buttonLabel

    val priority: Int
        get() = style.priority

    val surface: MessageSurfaceId
        get() = data.surface

    val isExpired: Boolean
        get() = metadata.displayCount >= style.maxDisplayCount

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val displayCount: Int
        get() = metadata.displayCount

    /**
     * Returns true if the passed boot id, taken from [BootUtils] matches the one associated
     * with this message when it was last displayed.
     */
    fun hasShownThisCycle(bootId: String) = bootId == metadata.latestBootIdentifier

    /**
     * A data class that holds metadata that help to identify if a message should shown.
     *
     * @param id identifies a message as unique.
     * @param displayCount Indicates how many times a message is displayed.
     * @param pressed Indicates if a message has been clicked.
     * @param dismissed Indicates if a message has been closed.
     * @param lastTimeShown A timestamp indicating when was the last time, the message was shown.
     * @param latestBootIdentifier A unique boot identifier for when the message was last displayed
     * (this may be a boot count or a boot id).
     */
    data class Metadata(
        val id: String,
        val displayCount: Int = 0,
        val pressed: Boolean = false,
        val dismissed: Boolean = false,
        val lastTimeShown: Long = 0L,
        val latestBootIdentifier: String? = null,
    )
}
