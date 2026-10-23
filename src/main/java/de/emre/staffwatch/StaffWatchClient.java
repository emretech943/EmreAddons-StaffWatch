package de.emre.staffwatch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Independent client-only Fabric mod. No server-side actions and no disconnect code. */
public final class StaffWatchClient implements ClientModInitializer {
    private static final Set<String> STAFF = Set.of(
        "pastagamer08", "archivepedro", "fluffymaster07"
    ); // Starter list only. Expand with verified staff names, not guesses.
    private final Map<UUID, Snapshot> last = new HashMap<>();
    private final Map<UUID, Long> cooldown = new HashMap<>();
    private KeyMapping testKey;
    private long start = -1;
    private String name = "";
    private String reason = "";
    private UUID watcher;

    private record Snapshot(String name, boolean listed, GameType mode) {}

    @Override public void onInitializeClient() {
        testKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.emreaddons.staffwatch.test", GLFW.GLFW_KEY_F8,
            "category.emreaddons.staffwatch"));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudRenderCallback.EVENT.register((graphics, counter) -> render(graphics));
    }

    private void tick(Minecraft mc) {
        while (testKey.consumeClick()) alert(mc, UUID.randomUUID(), "Pastagamer08", "F8 DEMO – kein echter Staff-Nachweis");
        if (mc.level == null || mc.getConnection() == null) { last.clear(); return; }
        Map<UUID, Snapshot> now = new HashMap<>();
        for (var info : mc.getConnection().getOnlinePlayers()) {
            UUID id = info.getProfile().getId();
            String username = info.getProfile().getName();
            Snapshot s = new Snapshot(username, info.isListed(), info.getGameMode());
            now.put(id, s);
            Snapshot before = last.get(id);
            if (!STAFF.contains(username.toLowerCase(Locale.ROOT)) || before == null) continue;
            if (before.listed && !s.listed) maybeAlert(mc, id, username, "Staff aus Tab-Liste ausgeblendet");
            else if (before.mode != GameType.SPECTATOR && s.mode == GameType.SPECTATOR)
                maybeAlert(mc, id, username, "Staff wechselte zu Spectator");
        }
        // A missing tab entry can also mean a normal disconnect; don't claim it proves vanish.
        last.clear(); last.putAll(now);
    }

    private void maybeAlert(Minecraft mc, UUID id, String username, String cause) {
        long time = System.currentTimeMillis();
        if (time - cooldown.getOrDefault(id, 0L) < 20_000L) return;
        cooldown.put(id, time);
        alert(mc, id, username, cause + " (kein Beweis für Beobachtung)");
    }

    private void alert(Minecraft mc, UUID id, String username, String cause) {
        watcher = id;
        name = username;
        reason = cause;
        start = System.nanoTime();
        // Local UI only. Never sends chat, commands or network packets.
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[StaffWatch] " + cause), false);
    }

    private void render(GuiGraphics g) {
        if (start < 0) return;
        float seconds = (System.nanoTime() - start) / 1_000_000_000f;
        if (seconds >= 5.5f) { start = -1; return; }
        Minecraft mc = Minecraft.getInstance();
        int cx = g.guiWidth() / 2, cy = g.guiHeight() / 2;
        // Lightweight 2D skin-head animation (not yet the original ImGui 3D cube).
        if (seconds < 1.5f) {
            float p = seconds / 1.5f;
            float alpha = Math.max(0, (float)Math.sin(p * Math.PI));
            int size = 54;
            int y = Math.round(cy - 125 + p * 90);
            var info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(watcher);
            if (info != null) {
                ResourceLocation skin = info.getSkin().texture();
                g.blit(RenderPipelines.GUI_TEXTURED, skin, cx - size / 2, y, 8, 8, size, size, 8, 8, 64, 64);
                g.blit(RenderPipelines.GUI_TEXTURED, skin, cx - size / 2, y, 40, 8, size, size, 8, 8, 64, 64);
            } else {
                g.drawCenteredString(mc.font, Component.literal("◈"), cx, y + 20, 0xFFAA4444);
            }
        }
        if (seconds >= 1.25f && seconds < 4.5f) {
            int red = 0xFFFF5555;
            g.drawCenteredString(mc.font, Component.literal(name), cx, cy - 14, red);
            g.drawCenteredString(mc.font, Component.literal("WATCHING YOU"), cx, cy + 2, red);
            g.drawCenteredString(mc.font, Component.literal("STAFF-AKTIVITÄT • NICHT BESTÄTIGT"), cx, cy + 19, 0xFFE0C0C0);
        }
        if (seconds >= 4.5f) g.drawCenteredString(mc.font, Component.literal(reason), cx, cy, 0xFFFFFF55);
    }
}
