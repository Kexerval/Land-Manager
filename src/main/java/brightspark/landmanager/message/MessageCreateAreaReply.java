package brightspark.landmanager.message;

import brightspark.landmanager.data.AddAreaResult;
import brightspark.landmanager.gui.GuiCreateArea;
import brightspark.landmanager.handler.ClientEventHandler;
import brightspark.landmanager.item.ItemAdmin;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCreateAreaReply implements IMessage
{
    public String areaName;
    public AddAreaResult result;

    public MessageCreateAreaReply() {}

    public MessageCreateAreaReply(String areaName, AddAreaResult result)
    {
        this.areaName = areaName;
        this.result = result;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        areaName = ByteBufUtils.readUTF8String(buf);
        result = AddAreaResult.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, areaName);
        buf.writeByte((byte) result.ordinal());
    }

    public static class Handler implements IMessageHandler<MessageCreateAreaReply, IMessage>
    {
        public Handler() {}

        private static void resetItem(EntityPlayer player)
        {
            ItemStack stack = player.getHeldItemMainhand();
            if(ItemAdmin.getPos(stack) != null)
                ItemAdmin.setPos(stack, null);
        }

        @Override
        public IMessage onMessage(MessageCreateAreaReply message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    Minecraft mc = Minecraft.getMinecraft();
                    EntityPlayer player = mc.player;
                    switch(message.result)
                    {
                        case SUCCESS:
                            player.sendMessage(new TextComponentString("Added area " + message.areaName));
                            player.closeScreen();
                            resetItem(player);
                            ClientEventHandler.setRenderArea(message.areaName);
                            break;
                        case NAME_EXISTS:
                            player.sendMessage(new TextComponentString("An area with the name " + message.areaName + " already exists!"));
                            GuiScreen gui = mc.currentScreen;
                            if(gui != null && gui instanceof GuiCreateArea) ((GuiCreateArea) gui).clearTextField();
                            break;
                        case AREA_INTERSECTS:
                            player.sendMessage(new TextComponentString("New area intersects with an existing area!"));
                            player.closeScreen();
                            resetItem(player);
                    }
                }
            });
            return null;
        }
    }
}
