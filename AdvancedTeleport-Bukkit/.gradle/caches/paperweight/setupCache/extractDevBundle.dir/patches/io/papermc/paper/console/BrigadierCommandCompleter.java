package io.papermc.paper.console;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import com.google.common.base.Suppliers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.dedicated.DedicatedServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import static com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion.completion;

public final class BrigadierCommandCompleter {
    private final Supplier<CommandSourceStack> commandSourceStack;
    private final DedicatedServer server;

    public BrigadierCommandCompleter(final @NonNull DedicatedServer server) {
        this.server = server;
        this.commandSourceStack = Suppliers.memoize(this.server::createCommandSourceStack);
    }

    public void complete(final @NonNull LineReader reader, final @NonNull ParsedLine line, final @NonNull List<Candidate> candidates, final @NonNull List<Completion> existing) {
        //noinspection ConstantConditions
        if (this.server.overworld() == null) { // check if overworld is null, as worlds haven't been loaded yet
            return;
        } else if (!io.papermc.paper.configuration.GlobalConfiguration.get().console.enableBrigadierCompletions) {
            this.addCandidates(candidates, Collections.emptyList(), existing);
            return;
        }
        final CommandDispatcher<CommandSourceStack> dispatcher = this.server.getCommands().getDispatcher();
        final ParseResults<CommandSourceStack> results = dispatcher.parse(prepareStringReader(line.line()), this.commandSourceStack.get());
        this.addCandidates(
            candidates,
            dispatcher.getCompletionSuggestions(results, line.cursor()).join().getList(),
            existing
        );
    }

    private void addCandidates(
        final @NonNull List<Candidate> candidates,
        final @NonNull List<Suggestion> brigSuggestions,
        final @NonNull List<Completion> existing
    ) {
        final List<Completion> completions = new ArrayList<>();
        brigSuggestions.forEach(it -> completions.add(toCompletion(it)));
        for (final Completion completion : existing) {
            if (completion.suggestion().isEmpty() || brigSuggestions.stream().anyMatch(it -> it.getText().equals(completion.suggestion()))) {
                continue;
            }
            completions.add(completion);
        }
        for (final Completion completion : completions) {
            if (completion.suggestion().isEmpty()) {
                continue;
            }
            candidates.add(toCandidate(completion));
        }
    }

    private static @NonNull Candidate toCandidate(final @NonNull Completion completion) {
        final String suggestionText = completion.suggestion();
        final String suggestionTooltip = PaperAdventure.PLAIN.serializeOr(completion.tooltip(), null);
        return new Candidate(
            suggestionText,
            suggestionText,
            null,
            suggestionTooltip,
            null,
            null,
            false
        );
    }

    private static @NonNull Completion toCompletion(final @NonNull Suggestion suggestion) {
        if (suggestion.getTooltip() == null) {
            return completion(suggestion.getText());
        }
        return completion(suggestion.getText(), PaperAdventure.asAdventure(ComponentUtils.fromMessage(suggestion.getTooltip())));
    }

    static @NonNull StringReader prepareStringReader(final @NonNull String line) {
        final StringReader stringReader = new StringReader(line);
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }
        return stringReader;
    }
}
