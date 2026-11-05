package io.github.srdjanv.endreforked.api.worldgen;

public class MissingModifierException extends RuntimeException {

    public MissingModifierException(Modifier modifier) {
        super("Modifier " + modifier + " is missing");
    }
}
