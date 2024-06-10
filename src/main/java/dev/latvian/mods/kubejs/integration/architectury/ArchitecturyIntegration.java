package dev.latvian.mods.kubejs.integration.architectury;

import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.script.WrapperRegistry;
import dev.latvian.mods.rhino.Context;

public class ArchitecturyIntegration implements KubeJSPlugin {
	public static dev.architectury.fluid.FluidStack wrapArchFluid(Context cx, Object o) {
		return FluidStackHooksForge.fromForge(FluidWrapper.wrap(cx, o));
	}

	@Override
	public void registerTypeWrappers(WrapperRegistry registry) {
		registry.register(dev.architectury.fluid.FluidStack.class, ArchitecturyIntegration::wrapArchFluid);
	}
}