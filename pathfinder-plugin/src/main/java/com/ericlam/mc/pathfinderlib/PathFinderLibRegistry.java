package com.ericlam.mc.pathfinderlib;

import com.ericlam.mc.eld.registrations.CommandRegistry;
import com.ericlam.mc.eld.registrations.ComponentsRegistry;
import com.ericlam.mc.eld.registrations.ListenerRegistry;
import com.ericlam.mc.pathfinderlib.command.*;

public final class PathFinderLibRegistry implements ComponentsRegistry {
    @Override
    public void registerCommand(CommandRegistry registry) {
        registry.command(GPSCommand.class, gps -> {
            gps.command(GPSGoCommand.class);
            gps.command(GPSLocateCommand.class);
            gps.command(GPSDebugCommand.class);
            gps.command(GPSTerminateCommand.class);
            gps.command(GPSRouteCommand.class);
            gps.command(GPSRouteBackCommand.class);
            //gps.command(GPSTestCommand.class);
        });
    }

    @Override
    public void registerListeners(ListenerRegistry registry) {

    }
}
