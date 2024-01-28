package net.id.incubus_core.mixin.blocklikeentities.client;

import net.id.incubus_core.blocklikeentities.util.PostTickEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// This gets disabled if another mod is present, if you add something to this mixin make sure you account for that.
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements PostTickEntity {
    @Shadow protected abstract void sendMovementPackets();
    
    @Unique boolean incubus_core$sendMovement = false;
    
    /**
     * Since the player can be moved by FloatingBlockEntity after ClientPlayerEntity.tick()
     * the call to sendMovementPackets() needs to be delayed till after all FloatingBlockEntities have ticked
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V"))
    void redirectSendMovementPackets(ClientPlayerEntity clientPlayerEntity) {
        incubus_core$sendMovement = true;
    }

    @Override
    public void incubus_Concern$postTick() {
        if (incubus_core$sendMovement) {
            sendMovementPackets();
            incubus_core$sendMovement = false;
        }
    }
}
