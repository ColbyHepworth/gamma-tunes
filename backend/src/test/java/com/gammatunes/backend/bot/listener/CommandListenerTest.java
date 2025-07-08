package com.gammatunes.backend.bot.listener;

import com.gammatunes.backend.bot.command.Command;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the CommandListener.
 */
@ExtendWith(MockitoExtension.class)
class CommandListenerTest {

    @Mock
    private SlashCommandInteractionEvent mockEvent;

    @Mock
    private ReplyCallbackAction mockReplyCallbackAction;

    @Mock
    private User mockUser;

    @Test
    void onSlashCommandInteraction_whenCommandExists_executesCorrectCommand() {
        // Arrange
        Command mockCommand = mock(Command.class);
        when(mockCommand.getCommandData()).thenReturn(Commands.slash("play", "A test command"));
        CommandListener commandListener = new CommandListener(List.of(mockCommand));

        when(mockEvent.getName()).thenReturn("play");
        // Stub the user for the logger call inside the listener
        when(mockEvent.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("TestUser#1234");

        // Act
        commandListener.onSlashCommandInteraction(mockEvent);

        // Assert
        verify(mockCommand, times(1)).execute(mockEvent);
    }

    @Test
    void onSlashCommandInteraction_whenCommandDoesNotExist_repliesWithUnknownCommand() {
        // Arrange
        CommandListener commandListener = new CommandListener(List.of());
        when(mockEvent.getName()).thenReturn("foo");
        when(mockEvent.reply("Unknown command.")).thenReturn(mockReplyCallbackAction);
        when(mockReplyCallbackAction.setEphemeral(true)).thenReturn(mockReplyCallbackAction);
        // This test does NOT need the user stubbed, as the logger is not called.

        // Act
        commandListener.onSlashCommandInteraction(mockEvent);

        // Assert
        verify(mockEvent).reply("Unknown command.");
        verify(mockReplyCallbackAction).setEphemeral(true);
        verify(mockReplyCallbackAction).queue();
    }

    @Test
    void onSlashCommandInteraction_whenCommandThrowsException_repliesWithError() {
        // Arrange
        Command mockCommand = mock(Command.class);
        when(mockCommand.getCommandData()).thenReturn(Commands.slash("play", "A test command"));
        doThrow(new RuntimeException("Test exception")).when(mockCommand).execute(mockEvent);

        CommandListener commandListener = new CommandListener(List.of(mockCommand));

        when(mockEvent.getName()).thenReturn("play");
        // Stub the user for the logger call inside the listener
        when(mockEvent.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("TestUser#1234");

        when(mockEvent.reply("An unexpected error occurred. Please try again later.")).thenReturn(mockReplyCallbackAction);
        when(mockReplyCallbackAction.setEphemeral(true)).thenReturn(mockReplyCallbackAction);

        // Act
        commandListener.onSlashCommandInteraction(mockEvent);

        // Assert
        verify(mockEvent).reply("An unexpected error occurred. Please try again later.");
        verify(mockReplyCallbackAction).setEphemeral(true);
        verify(mockReplyCallbackAction).queue();
    }
}
