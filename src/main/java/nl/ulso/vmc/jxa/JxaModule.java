package nl.ulso.vmc.jxa;

import dagger.Binds;
import dagger.Module;

/**
 * Provides utilities for running JXA scripts on macOS.
 */
@Module
public abstract class JxaModule
{
    @Binds
    abstract JxaRunner bindJxaRunner(JxaClasspathRunner jxaClasspathRunner);
}
