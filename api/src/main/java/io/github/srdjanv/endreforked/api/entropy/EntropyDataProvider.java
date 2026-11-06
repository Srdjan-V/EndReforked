package io.github.srdjanv.endreforked.api.entropy;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;
import io.github.srdjanv.endreforked.utils.LangUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public interface EntropyDataProvider {

    default Optional<EntropyRadius> getEntropyRadius() {
        return Optional.empty();
    }

    default Optional<EntropyStorage> getEntropyStorage() {
        return Optional.empty();
    }

    default Optional<ActiveInducer> getActiveInducer() {
        return Optional.empty();
    }

    default Optional<ActiveDrainer> getActiveDrainer() {
        return Optional.empty();
    }

    default Optional<PassiveInducer> getPassiveInducer() {
        return Optional.empty();
    }

    default Optional<PassiveDrainer> getPassiveDrainer() {
        return Optional.empty();
    }

    default Optional<List<String>> getFormatedEntropyData() {
        final ObjectList<String> list = new ObjectArrayList<>();
        getEntropyRadius().ifPresent(entropyRange -> list
                .add(LangUtil.translateToLocal("entropy.radius") + " " + (entropyRange.getRadius() + 1)));

        getEntropyStorage().ifPresent(entropyStorage -> {
            list.add(LangUtil.translateToLocal("entropy.storage.max_entropy") + " " + entropyStorage.getMaxEntropy());
            list.add(LangUtil.translateToLocal("entropy.storage.current_entropy") + " " +
                    entropyStorage.getCurrentEntropy());
        });

        getActiveInducer().ifPresent(activeInducer -> {
            list.add(LangUtil.translateToLocal("entropy.inducer.active") + " " + activeInducer.getInduced());
        });
        getActiveDrainer().ifPresent(activeDrainer -> {
            list.add(LangUtil.translateToLocal("entropy.drainer.active") + " " + activeDrainer.getDrained());
        });

        getPassiveInducer().ifPresent(passiveInducer -> {
            list.add(LangUtil.translateToLocal("entropy.inducer.passive") + " " + passiveInducer.getInduced());
            passiveInducer.getFrequency()
                    .ifPresent(value -> list
                            .add(LangUtil.translateToLocal("entropy.inducer.passive.frequency") + " " + value));
        });

        getPassiveDrainer().ifPresent(passiveInducer -> {
            list.add(LangUtil.translateToLocal("entropy.drainer.passive") + " " + passiveInducer.getDrained());
            passiveInducer.getFrequency()
                    .ifPresent(value -> list
                            .add(LangUtil.translateToLocal("entropy.drainer.passive.frequency") + " " + value));
        });

        return list.isEmpty() ? Optional.empty() : Optional.of(list);
    }

    interface ActiveInducer {

        int getInduced();
    }

    interface ActiveDrainer {

        int getDrained();
    }

    interface PassiveInducer extends ActiveInducer {

        OptionalInt getFrequency();
    }

    interface PassiveDrainer extends ActiveDrainer {

        OptionalInt getFrequency();
    }
}
