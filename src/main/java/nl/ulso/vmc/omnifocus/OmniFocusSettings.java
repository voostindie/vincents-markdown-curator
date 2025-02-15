package nl.ulso.vmc.omnifocus;

import java.util.Optional;
import java.util.function.Predicate;

public record OmniFocusSettings(String projectFolder, String omniFocusFolder, Predicate<String> includePredicate, Optional<String> documentToTouchAfterRefresh)
{
}
