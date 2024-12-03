/*
 * Copyright (C) 2020-2023 Velocity Contributors
 *
 * The Velocity API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the api top-level directory.
 */

package com.velocitypowered.api.event.command;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.annotation.AwaitingEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This event is fired when someone executes a command. Velocity will wait for this event to finish
 * firing before trying to handle the command and/or forwarding it to the server.
 */
@AwaitingEvent
public final class CommandExecuteEvent implements ResultedEvent<CommandResult> {

  private final CommandSource commandSource;
  private final String command;
  private CommandResult result;
  private InvocationSource invocationSource;

  /**
   * Constructs a CommandExecuteEvent.
   *
   * @param commandSource the source executing the command
   * @param command the command being executed without first slash
   */
  public CommandExecuteEvent(CommandSource commandSource, String command) {
    this(commandSource, command, InvocationSource.API);
  }

  /**
   * Constructs a CommandExecuteEvent.
   *
   * @param commandSource the source executing the command
   * @param command the command being executed without first slash
   * @param invocationSource the invocation source of this command
   */
  public CommandExecuteEvent(CommandSource commandSource, String command, InvocationSource invocationSource) {
    this.commandSource = Preconditions.checkNotNull(commandSource, "commandSource");
    this.command = Preconditions.checkNotNull(command, "command");
    this.result = CommandResult.allowed();
    this.invocationSource = invocationSource;
  }

  /**
   * Gets the source responsible for the execution of this command.
   *
   * @return the source executing the command
   */
  public CommandSource getCommandSource() {
    return commandSource;
  }

  /**
   * Gets the original command being executed without the first slash.
   *
   * @return the original command being executed
   * @apiNote Note that the player can provide a command that begins with spaces,
   *          but still be validly executed. For example, the command {@code /  velocity info},
   *          although not valid in the chat bar, will be executed as correctly as if
   *          the player had executed {@code /velocity info}
   */
  public String getCommand() {
    return command;
  }

  /**
   * Returns the source of the command invocation, indicating how the command was executed.
   *
   * @return invocation source
   */
  @NonNull
  public InvocationSource getInvocationSource() {
    return this.invocationSource;
  }

  @Override
  public CommandResult getResult() {
    return result;
  }

  @Override
  public void setResult(final @NonNull CommandResult result) {
    this.result = Preconditions.checkNotNull(result, "result");
  }

  @Override
  public String toString() {
    return "CommandExecuteEvent{"
        + "commandSource=" + commandSource
        + ", command=" + command
        + ", result=" + result
        + '}';
  }

  /**
   * Represents the source of a command invocation.
   */
  public enum InvocationSource {
    /**
     * Indicates that the command was executed from an signed source,
     * such as a player's direct input (e.g., typing in chat).
     */
    SIGNED,
    /**
     * Indicates that the command was executed from an unsigned source,
     * such as clicking a component with a {@link net.kyori.adventure.text.event.ClickEvent.Action#RUN_COMMAND}.
     *
     * <p>Sent by clients on 1.20.5+
     */
    UNSIGNED,
    /**
     * Indicates that the command was invoked programmatically via an API call.
     */
    API,
    /**
     * Indicates that the command was executed from an unknown source.
     *
     * <p>This is sent on command execution for pre 1.19.3 clients.
     */
    UNKNOWN
  }

  /**
   * Represents the result of the {@link CommandExecuteEvent}.
   */
  public static final class CommandResult implements ResultedEvent.Result {

    private static final CommandResult ALLOWED = new CommandResult(true, false, null);
    private static final CommandResult DENIED = new CommandResult(false, false, null);
    private static final CommandResult FORWARD_TO_SERVER = new CommandResult(false, true, null);

    private final @Nullable String command;
    private final boolean status;
    private final boolean forward;

    private CommandResult(final boolean status, final boolean forward, final @Nullable String command) {
      this.status = status;
      this.forward = forward;
      this.command = command;
    }

    public Optional<String> getCommand() {
      return Optional.ofNullable(command);
    }

    public boolean isForwardToServer() {
      return forward;
    }

    @Override
    public boolean isAllowed() {
      return status;
    }

    @Override
    public String toString() {
      return status ? "allowed" : "denied";
    }

    /**
     * Allows the command to be sent, without modification.
     *
     * @return the allowed result
     */
    public static CommandResult allowed() {
      return ALLOWED;
    }

    /**
     * Prevents the command from being executed.
     *
     * @return the denied result
     */
    public static CommandResult denied() {
      return DENIED;
    }

    /**
     * Forwards the command to server instead of executing it on the proxy. This is the
     * default behavior when a command is not registered on Velocity.
     *
     * @return the forward result
     */
    public static CommandResult forwardToServer() {
      return FORWARD_TO_SERVER;
    }

    /**
     * Prevents the command from being executed on proxy, but forward command to server.
     *
     * @param newCommand the command without first slash to use instead
     * @return a result with a new command being forwarded to server
     */
    public static CommandResult forwardToServer(final @NonNull String newCommand) {
      Preconditions.checkNotNull(newCommand, "newCommand");
      return new CommandResult(false, true, newCommand);
    }

    /**
     * Allows the command to be executed, but silently replaces the command with a different
     * command.
     *
     * @param newCommand the command to use instead without first slash
     * @return a result with a new command
     */
    public static CommandResult command(final @NonNull String newCommand) {
      Preconditions.checkNotNull(newCommand, "newCommand");
      return new CommandResult(true, false, newCommand);
    }
  }
}
