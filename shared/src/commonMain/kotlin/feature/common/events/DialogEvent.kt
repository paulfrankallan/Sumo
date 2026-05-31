package feature.common.events

class DialogEvent(
    override val id: String,
    val title: String? = null,
    override val body: String? = null,
    val positiveButton: DialogButton? = null,
    val negativeButton: DialogButton? = null,
    val isCancelable: Boolean = true,
) : Event(id, body)

data class DialogButton(
    val ctaLabel: String? = null,
    val action: (() -> Unit)? = null,
)
