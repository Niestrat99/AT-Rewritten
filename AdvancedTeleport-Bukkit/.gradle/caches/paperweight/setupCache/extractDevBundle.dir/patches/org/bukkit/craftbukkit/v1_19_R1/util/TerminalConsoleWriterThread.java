package org.bukkit.craftbukkit.v1_19_R1.util;

import com.mojang.logging.LogQueues;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
//import jline.console.ConsoleReader;
import org.bukkit.craftbukkit.v1_19_R1.Main;
//import org.fusesource.jansi.Ansi;
//import org.fusesource.jansi.Ansi.Erase;

public class TerminalConsoleWriterThread /*extends Thread*/ {/* // Paper - disable
    private final ConsoleReader reader;
    private final OutputStream output;

    public TerminalConsoleWriterThread(OutputStream output, ConsoleReader reader) {
        super("TerminalConsoleWriter");
        this.output = output;
        this.reader = reader;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        String message;

        // Using name from log4j config in vanilla jar
        while (true) {
            message = LogQueues.getNextLogEvent("TerminalConsole");
            if (message == null) {
                continue;
            }

            try {
                if (Main.useJline) {
                    this.reader.print(Ansi.ansi().eraseLine(Erase.ALL).toString() + ConsoleReader.RESET_LINE);
                    this.reader.flush();
                    this.output.write(message.getBytes());
                    this.output.flush();

                    try {
                        this.reader.drawLine();
                    } catch (Throwable ex) {
                        this.reader.getCursorBuffer().clear();
                    }
                    this.reader.flush();
                } else {
                    this.output.write(message.getBytes());
                    this.output.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }*/
}
