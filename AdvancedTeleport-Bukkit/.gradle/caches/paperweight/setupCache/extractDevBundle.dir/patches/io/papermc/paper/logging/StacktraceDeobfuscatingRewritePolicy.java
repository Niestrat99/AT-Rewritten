package io.papermc.paper.logging;

import io.papermc.paper.util.StacktraceDeobfuscator;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

@Plugin(
    name = "StacktraceDeobfuscatingRewritePolicy",
    category = Core.CATEGORY_NAME,
    elementType = "rewritePolicy",
    printObject = true
)
public final class StacktraceDeobfuscatingRewritePolicy implements RewritePolicy {
    private StacktraceDeobfuscatingRewritePolicy() {
    }

    @Override
    public @NonNull LogEvent rewrite(final @NonNull LogEvent rewrite) {
        final Throwable thrown = rewrite.getThrown();
        if (thrown != null) {
            StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(thrown);
            return new Log4jLogEvent.Builder(rewrite)
                .setThrownProxy(null)
                .build();
        }
        return rewrite;
    }

    @PluginFactory
    public static @NonNull StacktraceDeobfuscatingRewritePolicy createPolicy() {
        return new StacktraceDeobfuscatingRewritePolicy();
    }
}
