package io.github.srdjanv.endreforked.api.base.crafting

enum class TileStatus(val langKey: String) {
    IDLE("tile.status.gui.idle"),
    OUT_FULL("tile.status.gui.output_full"),
    FAILED("tile.status.gui.failed"),
    INVALID("tile.status.gui.invalid_recipe"),
    NOT_ENOUGH_ENTROPY("tile.status.gui.not_enough_entropy"),
    RUNNING("tile.status.gui.running")
}
