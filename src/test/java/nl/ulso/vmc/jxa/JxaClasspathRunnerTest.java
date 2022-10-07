package nl.ulso.vmc.jxa;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SoftAssertionsExtension.class)
class JxaClasspathRunnerTest
{
    @Test
    void helloWorldNoArgs()
    {
        var output = new JxaClasspathRunner().runScriptForObject("hello");
        var message = output.getString("message");
        assertThat(message).isEqualTo("Hello, world");
    }

    @Test
    void helloWorldWithArgs()
    {
        var output = new JxaClasspathRunner().runScriptForObject("hello", "Vincent");
        var message = output.getString("message");
        assertThat(message).isEqualTo("Hello, Vincent");
    }

    @Test
    void nonExistingScript()
    {
        assertThatThrownBy(() -> new JxaClasspathRunner().runScriptForArray("non-existing"))
                .isInstanceOf(IllegalStateException.class);
    }
}
