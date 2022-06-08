package org.spigotmc;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;

public final class WatchdogThread extends io.papermc.paper.util.TickThread // Paper - rewrite chunk system
{

    public static final boolean DISABLE_WATCHDOG = Boolean.getBoolean("disable.watchdog"); // Paper
    private static WatchdogThread instance;
    private long timeoutTime;
    private boolean restart;
    private final long earlyWarningEvery; // Paper - Timeout time for just printing a dump but not restarting
    private final long earlyWarningDelay; // Paper
    public static volatile boolean hasStarted; // Paper
    private long lastEarlyWarning; // Paper - Keep track of short dump times to avoid spamming console with short dumps
    private volatile long lastTick;
    private volatile boolean stopping;

    // Paper start - log detailed tick information
    private void dumpEntity(net.minecraft.world.entity.Entity entity) {
        Logger log = Bukkit.getServer().getLogger();
        double posX, posY, posZ;
        net.minecraft.world.phys.Vec3 mot;
        double moveStartX, moveStartY, moveStartZ;
        net.minecraft.world.phys.Vec3 moveVec;
        synchronized (entity.posLock) {
            posX = entity.getX();
            posY = entity.getY();
            posZ = entity.getZ();
            mot = entity.getDeltaMovement();
            moveStartX = entity.getMoveStartX();
            moveStartY = entity.getMoveStartY();
            moveStartZ = entity.getMoveStartZ();
            moveVec = entity.getMoveVector();
        }

        String entityType = net.minecraft.world.entity.EntityType.getKey(entity.getType()).toString();
        java.util.UUID entityUUID = entity.getUUID();
        net.minecraft.world.level.Level world = entity.level;

        log.log(Level.SEVERE, "Ticking entity: " + entityType + ", entity class: " + entity.getClass().getName());
        log.log(Level.SEVERE, "Entity status: removed: " + entity.isRemoved() + ", valid: " + entity.valid + ", alive: " + entity.isAlive() + ", is passenger: " + entity.isPassenger());
        log.log(Level.SEVERE, "Entity UUID: " + entityUUID);
        log.log(Level.SEVERE, "Position: world: '" + (world == null ? "unknown world?" : world.getWorld().getName()) + "' at location (" + posX + ", " + posY + ", " + posZ + ")");
        log.log(Level.SEVERE, "Velocity: " + (mot == null ? "unknown velocity" : mot.toString()) + " (in blocks per tick)");
        log.log(Level.SEVERE, "Entity AABB: " + entity.getBoundingBox());
        if (moveVec != null) {
            log.log(Level.SEVERE, "Move call information: ");
            log.log(Level.SEVERE, "Start position: (" + moveStartX + ", " + moveStartY + ", " + moveStartZ + ")");
            log.log(Level.SEVERE, "Move vector: " + moveVec.toString());
        }
    }

    private void dumpTickingInfo() {
        Logger log = Bukkit.getServer().getLogger();

        // ticking entities
        for (net.minecraft.world.entity.Entity entity : net.minecraft.server.level.ServerLevel.getCurrentlyTickingEntities()) {
            this.dumpEntity(entity);
            net.minecraft.world.entity.Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                log.log(Level.SEVERE, "Detailing vehicle for above entity:");
                this.dumpEntity(vehicle);
            }
        }

        // packet processors
        for (net.minecraft.network.PacketListener packetListener : net.minecraft.network.protocol.PacketUtils.getCurrentPacketProcessors()) {
            if (packetListener instanceof net.minecraft.server.network.ServerGamePacketListenerImpl) {
                net.minecraft.server.level.ServerPlayer player = ((net.minecraft.server.network.ServerGamePacketListenerImpl)packetListener).player;
                long totalPackets = net.minecraft.network.protocol.PacketUtils.getTotalProcessedPackets();
                if (player == null) {
                    log.log(Level.SEVERE, "Handling packet for player connection or ticking player connection (null player): " + packetListener);
                    log.log(Level.SEVERE, "Total packets processed on the main thread for all players: " + totalPackets);
                } else {
                    this.dumpEntity(player);
                    net.minecraft.world.entity.Entity vehicle = player.getVehicle();
                    if (vehicle != null) {
                        log.log(Level.SEVERE, "Detailing vehicle for above entity:");
                        this.dumpEntity(vehicle);
                    }
                    log.log(Level.SEVERE, "Total packets processed on the main thread for all players: " + totalPackets);
                }
            } else {
                log.log(Level.SEVERE, "Handling packet for connection: " + packetListener);
            }
        }
    }
    // Paper end - log detailed tick information

    private WatchdogThread(long timeoutTime, boolean restart)
    {
        super( "Paper Watchdog Thread" );
        this.timeoutTime = timeoutTime;
        this.restart = restart;
        earlyWarningEvery = Math.min(io.papermc.paper.configuration.GlobalConfiguration.get().watchdog.earlyWarningEvery, timeoutTime); // Paper
        earlyWarningDelay = Math.min(io.papermc.paper.configuration.GlobalConfiguration.get().watchdog.earlyWarningDelay, timeoutTime); // Paper
    }

    private static long monotonicMillis()
    {
        return System.nanoTime() / 1000000L;
    }

    public static void doStart(int timeoutTime, boolean restart)
    {
        if ( WatchdogThread.instance == null )
        {
            if (timeoutTime <= 0) timeoutTime = 300; // Paper
            WatchdogThread.instance = new WatchdogThread( timeoutTime * 1000L, restart );
            WatchdogThread.instance.start();
        } else
        {
            instance.timeoutTime = timeoutTime * 1000L;
            instance.restart = restart;
        }
    }

    public static void tick()
    {
        instance.lastTick = WatchdogThread.monotonicMillis();
    }

    public static void doStop()
    {
        if ( WatchdogThread.instance != null )
        {
            instance.stopping = true;
        }
    }

    @Override
    public void run()
    {
        while ( !this.stopping )
        {
            //
            // Paper start
            Logger log = Bukkit.getServer().getLogger();
            long currentTime = WatchdogThread.monotonicMillis();
            MinecraftServer server = MinecraftServer.getServer();
            if ( this.lastTick != 0 && this.timeoutTime > 0 && WatchdogThread.hasStarted && (!server.isRunning() || (currentTime > this.lastTick + this.earlyWarningEvery && !DISABLE_WATCHDOG) )) // Paper - add property to disable
            {
                boolean isLongTimeout = currentTime > lastTick + timeoutTime || (!server.isRunning() && !server.hasStopped() && currentTime > lastTick + 1000);
                // Don't spam early warning dumps
                if ( !isLongTimeout && (earlyWarningEvery <= 0 || !hasStarted || currentTime < lastEarlyWarning + earlyWarningEvery || currentTime < lastTick + earlyWarningDelay)) continue;
                if ( !isLongTimeout && server.hasStopped()) continue; // Don't spam early watchdog warnings during shutdown, we'll come back to this...
                lastEarlyWarning = currentTime;
                if (isLongTimeout) {
                // Paper end
                log.log( Level.SEVERE, "------------------------------" );
                log.log( Level.SEVERE, "The server has stopped responding! This is (probably) not a Paper bug." ); // Paper
                log.log( Level.SEVERE, "If you see a plugin in the Server thread dump below, then please report it to that author" );
                log.log( Level.SEVERE, "\t *Especially* if it looks like HTTP or MySQL operations are occurring" );
                log.log( Level.SEVERE, "If you see a world save or edit, then it means you did far more than your server can handle at once" );
                log.log( Level.SEVERE, "\t If this is the case, consider increasing timeout-time in spigot.yml but note that this will replace the crash with LARGE lag spikes" );
                log.log( Level.SEVERE, "If you are unsure or still think this is a Paper bug, please report this to https://github.com/PaperMC/Paper/issues" );
                log.log( Level.SEVERE, "Be sure to include ALL relevant console errors and Minecraft crash reports" );
                log.log( Level.SEVERE, "Paper version: " + Bukkit.getServer().getVersion() );
                //
                if ( net.minecraft.world.level.Level.lastPhysicsProblem != null )
                {
                    log.log( Level.SEVERE, "------------------------------" );
                    log.log( Level.SEVERE, "During the run of the server, a physics stackoverflow was supressed" );
                    log.log( Level.SEVERE, "near " + net.minecraft.world.level.Level.lastPhysicsProblem );
                }
                // Paper start - Warn in watchdog if an excessive velocity was ever set
                if ( org.bukkit.craftbukkit.v1_19_R1.CraftServer.excessiveVelEx != null )
                {
                    log.log( Level.SEVERE, "------------------------------" );
                    log.log( Level.SEVERE, "During the run of the server, a plugin set an excessive velocity on an entity" );
                    log.log( Level.SEVERE, "This may be the cause of the issue, or it may be entirely unrelated" );
                    log.log( Level.SEVERE, org.bukkit.craftbukkit.v1_19_R1.CraftServer.excessiveVelEx.getMessage());
                    for ( StackTraceElement stack : io.papermc.paper.util.StacktraceDeobfuscator.INSTANCE.deobfuscateStacktrace(org.bukkit.craftbukkit.v1_19_R1.CraftServer.excessiveVelEx.getStackTrace()) ) // Paper
                    {
                        log.log( Level.SEVERE, "\t\t" + stack );
                    }
                }
                // Paper end
                } else
                {
                    log.log(Level.SEVERE, "--- DO NOT REPORT THIS TO PAPER - THIS IS NOT A BUG OR A CRASH  - " + Bukkit.getServer().getVersion() + " ---");
                    log.log(Level.SEVERE, "The server has not responded for " + (currentTime - lastTick) / 1000 + " seconds! Creating thread dump");
                }
                // Paper end - Different message for short timeout
                log.log( Level.SEVERE, "------------------------------" );
                log.log( Level.SEVERE, "Server thread dump (Look for plugins here before reporting to Paper!):" ); // Paper
                io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler.dumpAllChunkLoadInfo(isLongTimeout); // Paper // Paper - rewrite chunk system
                this.dumpTickingInfo(); // Paper - log detailed tick information
                WatchdogThread.dumpThread( ManagementFactory.getThreadMXBean().getThreadInfo( MinecraftServer.getServer().serverThread.getId(), Integer.MAX_VALUE ), log );
                log.log( Level.SEVERE, "------------------------------" );
                //
                // Paper start - Only print full dump on long timeouts
                if ( isLongTimeout )
                {
                log.log( Level.SEVERE, "Entire Thread Dump:" );
                ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads( true, true );
                for ( ThreadInfo thread : threads )
                {
                    WatchdogThread.dumpThread( thread, log );
                }
                } else {
                    log.log(Level.SEVERE, "--- DO NOT REPORT THIS TO PAPER - THIS IS NOT A BUG OR A CRASH ---");
                }

                log.log( Level.SEVERE, "------------------------------" );

                if ( isLongTimeout )
                {
                if ( !server.hasStopped() )
                {
                    AsyncCatcher.enabled = false; // Disable async catcher incase it interferes with us
                    AsyncCatcher.shuttingDown = true;
                    server.forceTicks = true;
                    if (restart) {
                        RestartCommand.addShutdownHook( SpigotConfig.restartScript );
                    }
                    // try one last chance to safe shutdown on main incase it 'comes back'
                    server.abnormalExit = true;
                    server.safeShutdown(false, restart);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!server.hasStopped()) {
                        server.close();
                    }
                }
                break;
                } // Paper end
            }

            try
            {
                sleep( 1000 ); // Paper - Reduce check time to every second instead of every ten seconds, more consistent and allows for short timeout
            } catch ( InterruptedException ex )
            {
                interrupt();
            }
        }
    }

    private static void dumpThread(ThreadInfo thread, Logger log)
    {
        log.log( Level.SEVERE, "------------------------------" );
        //
        log.log( Level.SEVERE, "Current Thread: " + thread.getThreadName() );
        log.log( Level.SEVERE, "\tPID: " + thread.getThreadId()
                + " | Suspended: " + thread.isSuspended()
                + " | Native: " + thread.isInNative()
                + " | State: " + thread.getThreadState() );
        if ( thread.getLockedMonitors().length != 0 )
        {
            log.log( Level.SEVERE, "\tThread is waiting on monitor(s):" );
            for ( MonitorInfo monitor : thread.getLockedMonitors() )
            {
                log.log( Level.SEVERE, "\t\tLocked on:" + monitor.getLockedStackFrame() );
            }
        }
        log.log( Level.SEVERE, "\tStack:" );
        //
        for ( StackTraceElement stack : io.papermc.paper.util.StacktraceDeobfuscator.INSTANCE.deobfuscateStacktrace(thread.getStackTrace()) ) // Paper
        {
            log.log( Level.SEVERE, "\t\t" + stack );
        }
    }
}
