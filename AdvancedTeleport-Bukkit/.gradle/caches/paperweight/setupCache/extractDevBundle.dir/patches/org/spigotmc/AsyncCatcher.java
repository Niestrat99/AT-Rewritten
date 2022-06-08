package org.spigotmc;

import net.minecraft.server.MinecraftServer;

public class AsyncCatcher
{

    public static boolean enabled = true;
    public static boolean shuttingDown = false; // Paper

    public static void catchOp(String reason)
    {
        if ( !io.papermc.paper.util.TickThread.isTickThread() ) // Paper // Paper - rewrite chunk system
        {
            MinecraftServer.LOGGER.error("Thread " + Thread.currentThread().getName() + " failed main thread check: " + reason, new Throwable()); // Paper
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
