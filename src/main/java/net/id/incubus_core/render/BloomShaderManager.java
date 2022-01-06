package net.id.incubus_core.render;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.id.incubus_core.IncubusCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BloomShaderManager implements IdentifiableResourceReloadListener {
    public static final BloomShaderManager INSTANCE = new BloomShaderManager();
    private static final Identifier ID = new Identifier(IncubusCore.MODID, "bloom_shader_reloader");
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean initialized;
    private ShaderEffect effect;
    private Framebuffer framebuffer;

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
            if(RenderSystem.isOnRenderThread()) {
                if (effect != null) {
                    effect.close();
                }
                var client = MinecraftClient.getInstance();
                var id = new Identifier(IncubusCore.MODID, "shaders/post/bloom.json");
                try {
                    effect = new ShaderEffect(client.getTextureManager(), client.getResourceManager(), client.getFramebuffer(), id);
                    effect.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
                    framebuffer = effect.getSecondaryTarget("light_sources");
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to load shader: " + id, iOException);
                    effect = null;
                    framebuffer = null;
                }
                catch (JsonSyntaxException iOException) {
                    LOGGER.warn("Failed to parse shader: " + id, iOException);
                    effect = null;
                    framebuffer = null;
                }
            }
        }, applyExecutor);
    }

    public ShaderEffect getEffect() {
        return effect;
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

    public void render(MinecraftClient client, float tickDelta) {
        if (effect != null) {
            framebuffer.copyDepthFrom(client.getFramebuffer());
            effect.render(tickDelta);
            framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        }
    }

    public static void init() {
        synchronized (INSTANCE) {
            if (!initialized) {
                ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(INSTANCE);
                initialized = true;
            }
        }
    }
}