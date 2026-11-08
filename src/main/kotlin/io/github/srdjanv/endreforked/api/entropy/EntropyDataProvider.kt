package io.github.srdjanv.endreforked.api.entropy

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import io.github.srdjanv.endreforked.utils.LangUtil.translateToLocal
import java.util.Optional
import java.util.OptionalInt

interface EntropyDataProvider {
    val entropyRadius: Optional<EntropyRadius> get() = Optional.empty()
    val entropyStorage: Optional<EntropyStorage> get() = Optional.empty()
    val activeInducer: Optional<ActiveInducer> get() = Optional.empty()
    val activeDrainer: Optional<ActiveDrainer> get() = Optional.empty()
    val passiveInducer: Optional<PassiveInducer> get() = Optional.empty()
    val passiveDrainer: Optional<PassiveDrainer> get() = Optional.empty()

    val formatedEntropyData: Optional<List<String>>
        get() {
            val list = ArrayList<String>()
            entropyRadius.ifPresent { list += translateToLocal("entropy.radius") + " " + (it.radius + 1) }
            entropyStorage.ifPresent {
                list += translateToLocal("entropy.storage.max_entropy") + " " + it.maxEntropy
                list += translateToLocal("entropy.storage.current_entropy") + " " + it.currentEntropy
            }
            activeInducer.ifPresent { list += translateToLocal("entropy.inducer.active") + " " + it.induced }
            activeDrainer.ifPresent { list += translateToLocal("entropy.drainer.active") + " " + it.drained }
            passiveInducer.ifPresent {
                list += translateToLocal("entropy.inducer.passive") + " " + it.induced
                it.frequency.ifPresent { f -> list += translateToLocal("entropy.inducer.passive.frequency") + " " + f }
            }
            passiveDrainer.ifPresent {
                list += translateToLocal("entropy.drainer.passive") + " " + it.drained
                it.frequency.ifPresent { f -> list += translateToLocal("entropy.drainer.passive.frequency") + " " + f }
            }
            return if (list.isEmpty()) Optional.empty() else Optional.of(list)
        }

    interface ActiveInducer {
        val induced: Int
    }

    interface ActiveDrainer {
        val drained: Int
    }

    interface PassiveInducer : ActiveInducer {
        val frequency: OptionalInt
    }

    interface PassiveDrainer : ActiveDrainer {
        val frequency: OptionalInt
    }
}
