package de.moving.turtle.sandbox.cli;

import org.apache.commons.cli.*;

import static de.moving.turtle.sandbox.cli.CliOptionEnum.*;
import static de.moving.turtle.sandbox.cli.OptionRecognizedPrinter.*;

public class Main1 {

    public static void main(String... args) throws ParseException {

        CliOptions options = new CliOptions(args);
        final CliCommandLine commandLine =
                options.withOptions(WITH_VALUE, WITHOUT_VALUE)
                        .parse();
        commandLine.executeForOption(WITH_VALUE, aRecognizedPrinter())
                .executeForOption(WITHOUT_VALUE, aRecognizedPrinter());
    }
}

class CliOptions extends Options {

    private final String[] args;
    private final CommandLineParser parser;

    public CliOptions(final String[] args) {
        super();
        this.args = args;
        this.parser = new DefaultParser();
    }

    public CliOptions withOptions(final OptionEnum... optionEnum) {
        for (OptionEnum anEnum : optionEnum) {
            super.addOption(
                    anEnum.identifier(),
                    anEnum.argumentRequired(),
                    anEnum.description());
        }

        return this;
    }

    public CliCommandLine parse() throws ParseException {
        return CliCommandLine.aCliCommandLine(parser.parse(this, this.args));
    }

    public CliCommandLine parse(String... args) throws ParseException {
        return CliCommandLine.aCliCommandLine(parser.parse(this, args));
    }
}

class CliCommandLine {
    final private CommandLine commandLine;

    static CliCommandLine aCliCommandLine(final CommandLine commandLine) {
        return new CliCommandLine(commandLine);
    }

    private CliCommandLine(final CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public boolean hasOption(OptionEnum optionEnum) {
        return commandLine().hasOption(optionEnum.identifier());
    }

    public String getOptionValue(OptionEnum optionEnum) {
        return commandLine().getOptionValue(optionEnum.identifier());
    }

    public CliCommandLine executeForOption(OptionEnum optionEnum, OptionExecutor... executorChain) {
        for (OptionExecutor executor : executorChain) {
            executor.withCommandLine(this)
                    .withOption(optionEnum)
                    .execute();
        }
        return this;
    }

    public CommandLine commandLine() {
        return commandLine;
    }
}

interface OptionEnum {
    String identifier();

    boolean argumentRequired();

    String description();
}

interface OptionExecutor {
    void execute();

    OptionExecutor withOption(OptionEnum optionEnum);

    OptionExecutor withCommandLine(CliCommandLine cmd);
}

class OptionRecognizedPrinter implements OptionExecutor {

    private OptionEnum option;
    private CliCommandLine cmd;

    public static OptionExecutor aRecognizedPrinter() {
        return new OptionRecognizedPrinter();
    }

    private OptionRecognizedPrinter() {
    }

    @Override
    public void execute() {
        String message = "";
        if (cmd.hasOption(option)) {
            message = String.format("Recognized option '%s'", option.identifier());
            String attachment = " without value!";
            if (option.argumentRequired()) {
                attachment = String.format(" with value '%s'!", cmd.getOptionValue(option));
            }
            message = message + attachment;
        } else {
            message = String.format("Option '%s' not found", option.identifier());
        }

        System.out.println(message);
    }

    @Override
    public OptionExecutor withOption(final OptionEnum optionEnum) {
        this.option = optionEnum;
        return this;
    }

    @Override
    public OptionExecutor withCommandLine(final CliCommandLine cmd) {
        this.cmd = cmd;
        return this;
    }
}

enum CliOptionEnum implements OptionEnum {
    WITHOUT_VALUE("wo", false, "without value"),
    WITH_VALUE("w", true, "with value");
    private String identifier;
    private boolean argumentRequired;
    private String description;

    CliOptionEnum(final String identifier, final boolean argumentRequired, final String description) {
        this.identifier = identifier;
        this.argumentRequired = argumentRequired;
        this.description = description;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public boolean argumentRequired() {
        return argumentRequired;
    }

    @Override
    public String description() {
        return description;
    }
}