package com.codingforcookies.betterrecords.client.models;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.codingforcookies.betterrecords.StaticInfo;
import com.codingforcookies.betterrecords.items.TileEntityFrequencyTuner;

public class BlockFrequencyTunerRenderer extends TileEntitySpecialRenderer {
	public BlockFrequencyTunerRenderer() { }
	
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float scale) {
		if(!(te instanceof TileEntityFrequencyTuner))
			return;
		
		TileEntityFrequencyTuner tileEntityFrequencyTuner = (TileEntityFrequencyTuner)te;
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(te.blockMetadata * 90 + 180, 0.0F, 1.0F, 0.0F);
			bindTexture(StaticInfo.modelFrequencyTunerRes);
			StaticInfo.modelFrequencyTuner.render((Entity)null, tileEntityFrequencyTuner.crystalFloaty, 0F, 0F, 0.0F, 0.0F, 0.0625F, tileEntityFrequencyTuner.crystal);
		}
		GL11.glPopMatrix();
	}
}