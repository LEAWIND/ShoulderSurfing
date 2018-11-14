package com.teamderpy.shouldersurfing.asm.transformer.method;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.teamderpy.shouldersurfing.asm.Mappings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TransformerRayTrace extends ATransformerOrientCamera
{
	@Override
	public InsnList getSearchList(Mappings mappings)
	{
		InsnList searchList = new InsnList();
		searchList.add(new MethodInsnNode(INVOKEVIRTUAL, mappings.getClassPath("WorldClient"), mappings.getFieldOrMethod("WorldClient#rayTraceBlocks"), mappings.getDescriptor("WorldClient#rayTraceBlocks"), false));
		
		return searchList;
	}
	
	@Override
	public void transform(MethodNode method, Mappings mappings, int offset)
	{
		// net/minecraft/client/renderer/EntityRenderer.orientCamera:653
		// InjectionDelegation.getRayTraceResult(this.mc.world, Vec3d, Vec3d);
		
		MethodInsnNode instruction = new MethodInsnNode(INVOKESTATIC, "com/teamderpy/shouldersurfing/asm/InjectionDelegation", "getRayTraceResult", mappings.getDescriptor("InjectionDelegation#getRayTraceResult"), false);
		method.instructions.set(method.instructions.get(offset), instruction);
	}
}
