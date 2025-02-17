package io.quarkiverse.githubaction.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;

import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.CommandsInitializer;
import io.quarkiverse.githubaction.Inputs;
import io.quarkiverse.githubaction.InputsInitializer;
import io.quarkiverse.githubaction.it.CommandsActionTest.CommandsActionTestProfile;
import io.quarkiverse.githubaction.runtime.CommandsImpl;
import io.quarkiverse.githubaction.runtime.github.EnvFiles;
import io.quarkiverse.githubaction.testing.DefaultTestInputs;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
@TestProfile(CommandsActionTestProfile.class)
public class CommandsActionTest {

    @Test
    @Launch(value = {})
    public void testLaunchCommand(LaunchResult result) throws IOException {
        assertThat(Path.of(System.getProperty("java.io.tmpdir") + "/temp-github-output.txt")).content()
                .contains("testOutputKey=test output value" + System.lineSeparator());
    }

    public static class CommandsActionTestProfile implements QuarkusTestProfile {

        @Override
        public Set<Class<?>> getEnabledAlternatives() {
            return Set.of(MockInputsInitializer.class, MockCommandsInitializer.class);
        }
    }

    @Alternative
    @Singleton
    public static class MockInputsInitializer implements InputsInitializer {

        @Override
        public Inputs createInputs() {
            return new DefaultTestInputs(Map.of(Inputs.ACTION, CommandsAction.ACTION_NAME));
        }
    }

    @Alternative
    @Singleton
    public static class MockCommandsInitializer implements CommandsInitializer {

        @Override
        public Commands createCommands() {
            try {
                Path githubOutputPath = Path.of(System.getProperty("java.io.tmpdir") + "/temp-github-output.txt");
                Files.deleteIfExists(githubOutputPath);

                return new CommandsImpl(Map.of(EnvFiles.GITHUB_OUTPUT, githubOutputPath.toString()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
