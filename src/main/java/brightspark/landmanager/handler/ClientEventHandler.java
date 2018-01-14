package brightspark.landmanager.handler;

import brightspark.landmanager.Config;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.CapabilityAreas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler
{
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random rand = new Random();
    private static boolean renderAll = false;
    private static String renderArea = "";
    private static Map<Area, Color> colourCache = new HashMap<>();

    public static void setRenderArea(String areaName)
    {
        renderAll = false;
        renderArea = areaName;
        colourCache.clear();
    }

    public static void toggleRenderAll()
    {
        renderAll = !renderArea.isEmpty() || !renderAll;
        renderArea = "";
        colourCache.clear();
        if(renderAll)
            mc.player.sendMessage(new TextComponentString("Now showing all nearby areas"));
        else
            mc.player.sendMessage(new TextComponentString("Turned off showing all nearby areas"));
    }

    private static Color getColour(Area area)
    {
        Color colour = colourCache.get(area);
        if(colour == null)
            colourCache.put(area, colour = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 255));
        return colour;
    }

    private static void renderBox(Area area, double partialTicks)
    {
        //Get player's actual position
        EntityPlayerSP player = mc.player;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        //Render the box
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(5f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.translate(-x, -y, -z);
        float[] rgb = getColour(area).getRGBColorComponents(null);
        AxisAlignedBB box = new AxisAlignedBB(area.getMinPos(), area.getMaxPos().add(1, 1, 1)).grow(0.001d);
        GlStateManager.enableDepth();
        RenderGlobal.renderFilledBox(box, rgb[0], rgb[1], rgb[2], 0.2f);
        GlStateManager.disableDepth();
        RenderGlobal.drawSelectionBoundingBox(box, rgb[0], rgb[1], rgb[2], 0.4f);
        renderName(area, box.getCenter());
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    //Copied a lot of this from EntityRenderer#drawNameplate and changed for my needs
    private static void renderName(Area area, Vec3d center)
    {
        RenderManager rm = mc.getRenderManager();
        float viewerYaw = rm.playerViewY;
        float viewerPitch = rm.playerViewX;
        boolean isThirdPersonFrontal = rm.options.thirdPersonView == 2;

        GlStateManager.translate(center.x, center.y, center.z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        float scale = 0.04f * Config.areaNameScale;
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableTexture2D();

        FontRenderer fr = mc.fontRenderer;
        String name = area.getName();
        int i = fr.getStringWidth(name) / 2;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)(-i - 1), -1D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos((double)(-i - 1), 8D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos((double)(i + 1), 8D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        bufferbuilder.pos((double)(i + 1), -1D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        fr.drawString(name, -i, 0, -1);
        GlStateManager.disableBlend();
    }

    @SubscribeEvent
    public static void areaRendering(RenderWorldLastEvent event)
    {
        if(!renderAll && renderArea.isEmpty()) return;

        CapabilityAreas cap = mc.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null) return;

        if(renderAll)
        {
            Set<Area> areas = cap.getNearbyAreas(mc.player.getPosition());
            areas.forEach(area -> renderBox(area, event.getPartialTicks()));
        }
        else if(!renderArea.isEmpty())
        {
            Area area = cap.getArea(renderArea);
            if(area != null) renderBox(area, event.getPartialTicks());
        }
    }
}
