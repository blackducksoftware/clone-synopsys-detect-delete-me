package com.synopsys.integration.detectable.detectable.executable.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectable.executable.Executable;
import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;

public class SimpleExecutableRunner implements ExecutableRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ExecutableOutput execute(final File workingDirectory, final String exeCmd, final String... args) throws ExecutableRunnerException {
        return execute(new Executable(workingDirectory, null, exeCmd, Arrays.asList(args)));
    }

    @Override
    public ExecutableOutput execute(final File workingDirectory, final String exeCmd, final List<String> args) throws ExecutableRunnerException {
        return execute(new Executable(workingDirectory, null, exeCmd, args));
    }

    @Override
    public ExecutableOutput execute(final File workingDirectory, final File exeFile, final String... args) throws ExecutableRunnerException {
        return execute(new Executable(workingDirectory, null, exeFile.getAbsolutePath(), Arrays.asList(args)));
    }

    @Override
    public ExecutableOutput execute(final File workingDirectory, final File exeFile, final List<String> args) throws ExecutableRunnerException {
        return execute(new Executable(workingDirectory, null, exeFile.getAbsolutePath(), args));
    }

    @Override
    public ExecutableOutput execute(final Executable executable) throws ExecutableRunnerException {
        logger.info(String.format("Running executable >%s", executable.getMaskedExecutableDescription()));
        try {
            final ProcessBuilder processBuilder = executable.createProcessBuilder();
            final Process process = processBuilder.start();

            try (InputStream standardOutputStream = process.getInputStream(); InputStream standardErrorStream = process.getErrorStream()) {
                final ExecutableStreamThread standardOutputThread = new ExecutableStreamThread(standardOutputStream, logger::info, logger::trace);
                standardOutputThread.start();

                final ExecutableStreamThread errorOutputThread = new ExecutableStreamThread(standardErrorStream, logger::info, logger::trace);
                errorOutputThread.start();

                final int returnCode = process.waitFor();
                logger.info("Executable finished: " + returnCode);

                standardOutputThread.join();
                errorOutputThread.join();

                final String standardOutput = standardOutputThread.getExecutableOutput().trim();
                final String errorOutput = errorOutputThread.getExecutableOutput().trim();

                final ExecutableOutput output = new ExecutableOutput(returnCode, standardOutput, errorOutput);
                return output;
            }
        } catch (final Exception e) {
            throw new ExecutableRunnerException(e);
        }
    }
}