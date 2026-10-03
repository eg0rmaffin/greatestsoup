package io.github.eg0rmaffin.greatestsoup.content.hbm;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Sends the server's Mask Man rules to a joining player, so the detector counts what the server rolls. */
public class MaskManRulesMessage implements IMessage {

    private boolean enabled;
    private int delay;
    private int chance;
    private int minRad;
    private boolean underground;

    public MaskManRulesMessage() {
    }

    public static MaskManRulesMessage fromServerRules() {
        MaskManRulesMessage message = new MaskManRulesMessage();
        message.enabled = MaskManRules.enabled;
        message.delay = MaskManRules.delay;
        message.chance = MaskManRules.chance;
        message.minRad = MaskManRules.minRad;
        message.underground = MaskManRules.underground;
        return message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enabled = buf.readBoolean();
        delay = buf.readInt();
        chance = buf.readInt();
        minRad = buf.readInt();
        underground = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeInt(delay);
        buf.writeInt(chance);
        buf.writeInt(minRad);
        buf.writeBoolean(underground);
    }

    public static class Handler implements IMessageHandler<MaskManRulesMessage, IMessage> {

        @Override
        public IMessage onMessage(MaskManRulesMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> MaskManRules.set(message.enabled, message.delay,
                    message.chance, message.minRad, message.underground));
            return null;
        }

    }

}
